/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.checkout;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.workflow.GetClinicalEventTask;
import static org.openvpms.web.app.workflow.GetClinicalEventTask.EVENT_SHORTNAME;
import org.openvpms.web.app.workflow.GetInvoiceTask;
import static org.openvpms.web.app.workflow.GetInvoiceTask.INVOICE_SHORTNAME;
import org.openvpms.web.app.workflow.payment.PaymentWorkflow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workflow.ConditionalCreateTask;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.EvalTask;
import org.openvpms.web.component.workflow.NodeConditionTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.Variable;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;


/**
 * Check-out workflow.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * Constructs a new <tt>CheckOutWorkflow</tt> from an
     * <em>act.customerAppointment</em> or <em>act.customerTask</em>.
     *
     * @param act     the act
     * @param context the external context to access and update
     */
    public CheckOutWorkflow(Act act, Context context) {
        external = context;
        initialise(act);

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
     * @param act the act
     */
    private void initialise(Act act) {
        ActBean bean = new ActBean(act);
        Party customer = (Party) bean.getParticipant("participation.customer");
        Party patient = (Party) bean.getParticipant("participation.patient");
        User clinician = external.getClinician();

        initial = new DefaultTaskContext(false);
        initial.setCustomer(customer);
        initial.setPatient(patient);
        initial.setClinician(clinician);

        initial.setUser(external.getUser());
        initial.setPractice(external.getPractice());
        initial.setLocation(external.getLocation());

        // get the latest invoice, or create one if none is available, and edit it
        addTask(new GetInvoiceTask());
        addTask(new ConditionalCreateTask(INVOICE_SHORTNAME));
        addTask(createEditInvoiceTask());

        // on save, determine if the user wants to post the invoice, but
        // only if its not already posted
        NodeConditionTask<String> notPosted = new NodeConditionTask<String>(
                INVOICE_SHORTNAME, "status", false, FinancialActStatus.POSTED);
        addTask(new ConditionalTask(notPosted, getPostTask()));

        // if the invoice is posted, prompt to pay the account
        NodeConditionTask<String> posted = new NodeConditionTask<String>(
                INVOICE_SHORTNAME, "status", FinancialActStatus.POSTED);

        PaymentWorkflow payWorkflow = createPaymentWorkflow(initial);
        payWorkflow.setRequired(false);
        addTask(new ConditionalTask(posted, payWorkflow));

        // add the most recent clinicial event to the context
        addTask(new GetClinicalEventTask());

        // print acts and documents created since the visit or invoice was
        // created
        addTask(new PrintTask(act));

        // update the most recent act.patientClinicalEvent, setting it status
        // to COMPLETED, if one is present
        // TODO:  Removed this as causing version issues on common consulting room workflow.   
        // After consulting workflow and completing billing reception woudl do checkout and update visit
        // but clinician would also add more clinical records causing version conflict when saved.
        //addTask(new GetClinicalEventTask());
        //TaskProperties eventProperties = new TaskProperties();
        //eventProperties.add("status", ActStatus.COMPLETED);
        //addTask(new ConditionalUpdateTask(EVENT_SHORTNAME, eventProperties));
    }

    /**
     * Creates a new task to edit the invoice.
     *
     * @return a new task
     */
    protected EditIMObjectTask createEditInvoiceTask() {
        return new EditIMObjectTask(INVOICE_SHORTNAME);
    }

    /**
     * Creates a new payment workflow.
     *
     * @param context the context
     * @return a new payment workflow
     */
    protected PaymentWorkflow createPaymentWorkflow(TaskContext context) {
        return new PaymentWorkflow(context, external);
    }

    /**
     * Returns a condition task to determine if the invoice should be posted.
     *
     * @return a new condition
     */
    protected EvalTask<Boolean> getPostCondition() {
        String invoiceTitle = Messages.get("workflow.checkout.postinvoice.title");
        String invoiceMsg = Messages.get("workflow.checkout.postinvoice.message");
        return new ConfirmationTask(invoiceTitle, invoiceMsg);
    }

    /**
     * Returns a task to post the invoice.
     *
     * @return a task to post the invoice
     */
    private Task getPostTask() {
        Tasks postTasks = new Tasks();
        TaskProperties invoiceProps = new TaskProperties();
        invoiceProps.add("status", FinancialActStatus.POSTED);
        invoiceProps.add(new Variable("startTime") {
            public Object getValue(TaskContext context) {
                return new Date(); // workaround for OVPMS-734. todo
            }
        });
        postTasks.addTask(new UpdateIMObjectTask(INVOICE_SHORTNAME, invoiceProps));
        postTasks.setRequired(false);

        EvalTask<Boolean> condition = getPostCondition();
        ConditionalTask post = new ConditionalTask(condition, postTasks);
        post.setRequired(false);
        return post;
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
         * Creates a new <tt>PrintTask</tt>.
         *
         * @param act the act. Either an <em>act.customerAppointment</em> or
         *            <em>act.customerTask</em>.
         */
        public PrintTask(Act act) {
            startTime = getMin(new Date(), act.getActivityStartTime());
        }

        /**
         * Executes the task.
         *
         * @throws OpenVPMSException for any error
         */
        public void execute(TaskContext context) {
            Date min = getMinStartTime(INVOICE_SHORTNAME, startTime, context);
            min = getMinStartTime(EVENT_SHORTNAME, min, context);
            PrintDocumentsTask printDocs = new PrintDocumentsTask(min);
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
        private Date getMinStartTime(String shortName, Date startTime,
                                     TaskContext context) {
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

}
