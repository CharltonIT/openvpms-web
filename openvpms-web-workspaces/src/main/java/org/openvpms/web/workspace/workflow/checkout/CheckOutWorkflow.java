/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.workflow.ConditionalCreateTask;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.ConditionalUpdateTask;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.EvalTask;
import org.openvpms.web.component.workflow.NodeConditionTask;
import org.openvpms.web.component.workflow.RetryableUpdateIMObjectTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.Variable;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.GetClinicalEventTask;
import org.openvpms.web.workspace.workflow.GetInvoiceTask;
import org.openvpms.web.workspace.workflow.payment.PaymentWorkflow;

import java.util.Date;

import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_EVENT;


/**
 * Check-out workflow.
 *
 * @author Tim Anderson
 */
public class CheckOutWorkflow extends WorkflowImpl {

    /**
     * The initial context.
     */
    private TaskContext initial;

    /**
     * The external context to access and update.
     */
    private final Context external;


    /**
     * Constructs a {@code CheckOutWorkflow} from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
     *
     * @param act     the act
     * @param context the external context to access and update
     * @param help    the help context
     */
    public CheckOutWorkflow(Act act, Context context, HelpContext help) {
        super(help.topic("workflow/checkout"));
        if (context.getPractice() == null) {
            throw new IllegalStateException("Context has no practice");
        }
        external = context;
        initialise(act, getHelpContext());

        // update the act status
        TaskProperties appProps = new TaskProperties();
        appProps.add("status", ActStatus.COMPLETED);
        if (TypeHelper.isA(act, "act.customerTask")) {
            // update the end-time of the task
            appProps.add(new Variable("endTime") {
                public Object getValue(TaskContext context) {
                    return new Date();
                }
            });
        }
        addTask(new UpdateIMObjectTask(act, appProps));

        // add a task to update the context at the end of the workflow
        addTask(new SynchronousTask() {
            public void execute(TaskContext context) {
                external.setCustomer(context.getCustomer());
                external.setPatient(context.getPatient());
                external.setTill(context.getTill());
                external.setClinician(context.getClinician());
            }
        });
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    /**
     * Initialise the workflow.
     *
     * @param act  the act
     * @param help the help context
     */
    private void initialise(Act act, HelpContext help) {
        ActBean bean = new ActBean(act);
        Party customer = (Party) bean.getParticipant("participation.customer");
        Party patient = (Party) bean.getParticipant("participation.patient");
        User clinician = external.getClinician();

        initial = new DefaultTaskContext(help);
        initial.setCustomer(customer);
        initial.setPatient(patient);
        initial.setClinician(clinician);

        initial.setUser(external.getUser());
        initial.setPractice(external.getPractice());
        initial.setLocation(external.getLocation());

        // get the latest invoice, or create one if none is available, and edit it
        addTask(new GetInvoiceTask());
        addTask(new ConditionalCreateTask(CustomerAccountArchetypes.INVOICE));
        addTask(createEditInvoiceTask());

        // on save, determine if the user wants to post the invoice, but
        // only if its not already posted
        NodeConditionTask<String> notPosted = new NodeConditionTask<String>(
                CustomerAccountArchetypes.INVOICE, "status", false, FinancialActStatus.POSTED);
        addTask(new ConditionalTask(notPosted, getPostTask()));

        // if the invoice is posted, prompt to pay the account
        NodeConditionTask<String> posted = new NodeConditionTask<String>(
                CustomerAccountArchetypes.INVOICE, "status", FinancialActStatus.POSTED);

        PaymentWorkflow payWorkflow = createPaymentWorkflow(initial);
        payWorkflow.setRequired(false);
        addTask(new ConditionalTask(posted, payWorkflow));

        // add the most recent clinical event to the context
        addTask(new GetClinicalEventTask(act.getActivityStartTime()));

        // print acts and documents created since the visit or invoice was
        // created
        addTask(new PrintTask(act, help.subtopic("print")));

        // update the most recent act.patientClinicalEvent, setting it status to COMPLETED and endTime to now, if one
        // is present. Use a retryable task to handle concurrent update conflicts
        TaskProperties eventProperties = new TaskProperties();
        eventProperties.add("status", ActStatus.COMPLETED);
        eventProperties.add(new Variable("endTime") {
            public Object getValue(TaskContext context) {
                return new Date();
            }
        });
        addTask(new ConditionalUpdateTask(CLINICAL_EVENT,
                                          new RetryableUpdateIMObjectTask(CLINICAL_EVENT, eventProperties)));
        addTask(new DischargeTask());
    }

    /**
     * Creates a new task to edit the invoice.
     *
     * @return a new task
     */
    protected EditIMObjectTask createEditInvoiceTask() {
        return new EditIMObjectTask(CustomerAccountArchetypes.INVOICE);
    }

    /**
     * Creates a new payment workflow.
     *
     * @param context the context
     * @return a new payment workflow
     */
    protected PaymentWorkflow createPaymentWorkflow(TaskContext context) {
        return new PaymentWorkflow(context, external, context.getHelpContext().subtopic("pay"));
    }

    /**
     * Returns a condition task to determine if the invoice should be posted.
     *
     * @return a new condition
     */
    protected EvalTask<Boolean> getPostCondition() {
        String invoiceTitle = Messages.get("workflow.checkout.postinvoice.title");
        String invoiceMsg = Messages.get("workflow.checkout.postinvoice.message");
        return new ConfirmationTask(invoiceTitle, invoiceMsg, getHelpContext().subtopic("post"));
    }

    /**
     * Returns a task to post the invoice.
     *
     * @return a task to post the invoice
     */
    private Task getPostTask() {
        Tasks postTasks = new Tasks(getHelpContext().subtopic("post"));
        postTasks.addTask(new PostInvoiceTask());
        postTasks.setRequired(false);

        EvalTask<Boolean> condition = getPostCondition();
        ConditionalTask post = new ConditionalTask(condition, postTasks);
        post.setRequired(false);
        return post;
    }

    /**
     * Task to post an invoice.
     * <p/>
     * This uses an editor to ensure that the any HL7 Pharmacy Orders associated with the invoice are discontinued.
     * <p/>
     * This is to work-around thefact that
     */
    private class PostInvoiceTask extends EditIMObjectTask {

        /**
         * Constructs a {@link PostInvoiceTask}.
         */
        public PostInvoiceTask() {
            super(CustomerAccountArchetypes.INVOICE, false, false);
            setShowEditorOnError(false);
        }

        /**
         * Edits an object in the background.
         *
         * @param editor  the editor
         * @param context the task context
         */
        @Override
        protected void edit(IMObjectEditor editor, TaskContext context) {
            ActEditor actEditor = (ActEditor) editor;
            actEditor.setStatus(ActStatus.POSTED);
            actEditor.setStartTime(new Date()); // for OVPMS-734 - TODO
        }
    }

    /**
     * Prints all unprinted acts and documents for the customer and patient.
     * This uses the minimum startTime of the <em>act.patientClinicalEvent</em>,
     * <em>act.customerAccountChargesInvoice</em> and time now to select the
     * objects to print.
     */
    private class PrintTask extends SynchronousTask {

        /**
         * The minimum of the act start time and the time the task was created.
         */
        private final Date startTime;

        /**
         * The help context.
         */
        private final HelpContext help;

        /**
         * Creates a new {@code PrintTask}.
         *
         * @param act  the act. Either an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
         * @param help the help context
         */
        public PrintTask(Act act, HelpContext help) {
            startTime = getMin(new Date(), act.getActivityStartTime());
            this.help = help;
        }

        /**
         * Executes the task.
         *
         * @throws OpenVPMSException for any error
         */
        public void execute(TaskContext context) {
            Date min = getMinStartTime(CustomerAccountArchetypes.INVOICE, startTime, context);
            min = getMinStartTime(CLINICAL_EVENT, min, context);
            min = DateRules.getDate(min);  // print all documents done on or after the min date
            PrintDocumentsTask printDocs = new PrintDocumentsTask(min, help);
            printDocs.setRequired(false);
            addTask(printDocs);
        }

        /**
         * Returns the minimum of two start times, one obtained from act
         * identified by short name in the task context, the other supplied.
         *
         * @param shortName the act short name
         * @param startTime the start time to compare with
         * @param context   the task context
         * @return the minimum of the two start times
         */
        private Date getMinStartTime(String shortName, Date startTime, TaskContext context) {
            Act act = (Act) context.getObject(shortName);
            if (act != null) {
                startTime = getMin(startTime, act.getActivityStartTime());
            }
            return startTime;
        }

        /**
         * Returns the minimum of two dates.
         *
         * @param date1 the first date
         * @param date2 the second date
         * @return the minimum of the two dates
         */
        private Date getMin(Date date1, Date date2) {
            Date min = date1;
            if (date1.getTime() > date2.getTime()) {
                min = date2;
            }
            return min;
        }
    }

    private class DischargeTask extends SynchronousTask {

        /**
         * Executes the task.
         *
         * @throws OpenVPMSException for any error
         */
        @Override
        public void execute(TaskContext context) {
            PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
            Act visit = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
            PatientContext pc = factory.createContext(context.getPatient(), context.getCustomer(), visit,
                                                      context.getLocation(), context.getClinician());
            PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
            service.discharged(pc, context.getUser());
        }
    }

}
