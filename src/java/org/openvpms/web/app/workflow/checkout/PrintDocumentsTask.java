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

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.doc.DocumentTemplatePrinter;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.print.BatchPrintDialog;
import org.openvpms.web.component.print.BatchPrinter;
import org.openvpms.web.component.print.PrintHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Task to allow the user to selectively print any unprinted documents
 * from a particular time.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class PrintDocumentsTask extends AbstractTask {

    /**
     * The time to select unprinted documents from.
     */
    private final Date startTime;

    /**
     * The charge acts to print.
     */
    private static final String[] CHARGES = {"act.customerAccountCharges*"};

    /**
     * The printable patient documents.
     */
    private static final String[] DOCUMENTS
            = {"act.patientDocumentLetter", "act.patientDocumentForm"};


    /**
     * Constructs a <tt>PrintDocumentsTask</tt>.
     *
     * @param startTime the act start time.
     */
    public PrintDocumentsTask(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     */
    public void start(final TaskContext context) {
        Map<IMObject, Boolean> unprinted = new LinkedHashMap<IMObject, Boolean>();
        unprinted.putAll(getCustomerActs(context));
        unprinted.putAll(getPatientActs(context));
        if (unprinted.isEmpty()) {
            notifyCompleted();
        } else {
            String title = Messages.get("workflow.checkout.print.title");
            String[] buttons = isRequired() ? PopupDialog.OK_CANCEL : PopupDialog.OK_SKIP_CANCEL;
            final BatchPrintDialog dialog = new BatchPrintDialog(title, buttons, unprinted);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    String action = dialog.getAction();
                    if (BatchPrintDialog.OK_ID.equals(action)) {
                        print(dialog.getSelected(), context);
                    } else if (BatchPrintDialog.SKIP_ID.equals(action)) {
                        notifySkipped();
                    } else {
                        notifyCancelled();
                    }
                }
            });
            dialog.show();
        }
    }

    /**
     * Prints a list of objects.
     *
     * @param objects the objects to print
     * @param context the task context
     */
    private void print(List<IMObject> objects, TaskContext context) {
        Printer printer = new Printer(objects, context);
        printer.print();
    }

    /**
     * Returns a map of unprinted customer charges.
     *
     * @param context the task context
     * @return a map of unprinted customer charges
     */
    private Map<IMObject, Boolean> getCustomerActs(TaskContext context) {
        Party customer = context.getCustomer();
        String node = "customer";
        String participation = "participation.customer";
        return getUnprintedActs(CHARGES, customer, node, participation, context);
    }

    /**
     * Returns a map of unprinted patient documents.
     *
     * @param context the task context
     * @return a map of unprinted patient documents
     */
    private Map<IMObject, Boolean> getPatientActs(TaskContext context) {
        Party patient = context.getPatient();
        String node = "patient";
        String participation = "participation.patient";
        return getUnprintedActs(DOCUMENTS, patient, node, participation, context);
    }

    /**
     * Returns a map of unprinted acts for a party.
     * <p/>
     * The corresponding boolean flag if <tt>true</tt> indicates if the act should be selected for printing.
     * If <tt>false</tt>, it indicates that t5he act should be displayed, but not selected.
     *
     * @param shortNames    the act short names to query. May include wildcards
     * @param party         the party to query
     * @param node          the participation node to query
     * @param participation the participation short name to query
     * @param context       the context
     * @return the unprinted acts
     */
    private Map<IMObject, Boolean> getUnprintedActs(String[] shortNames, Party party, String node,
                                                    String participation, Context context) {
        Map<IMObject, Boolean> result = new LinkedHashMap<IMObject, Boolean>();
        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, true);
        query.setFirstResult(0);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        CollectionNodeConstraint participations
                = new CollectionNodeConstraint(node, participation,
                                               false, true);
        participations.add(new ObjectRefNodeConstraint("entity", party.getObjectReference()));

        query.add(participations);
        query.add(new NodeConstraint("startTime", RelationalOp.GTE, startTime));
        query.add(new NodeConstraint("printed", false));

        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        for (IMObject object : service.get(query).getResults()) {
            boolean select = selectForPrinting((Act) object, context);
            result.put(object, select);
        }
        return result;
    }

    /**
     * Determines if an act should be selected for printing.
     *
     * @param act     the act
     * @param context the context
     * @return <tt>true</tt> if the act should be selected, <tt>false</tt> if it should be displayed but not selected
     */
    private boolean selectForPrinting(Act act, Context context) {
        ActBean bean = new ActBean(act);
        boolean result = true;
        if (bean.hasNode("documentTemplate")) {
            Entity template = bean.getNodeParticipant("documentTemplate");
            if (template != null) {
                DocumentTemplatePrinter rel = PrintHelper.getDocumentTemplatePrinter(template, context);
                if (rel != null && !rel.getPrintAtCheckout()) {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * Batch printer.
     */
    class Printer extends BatchPrinter {

        /**
         * The task context.
         */
        private final TaskContext context;


        /**
         * Constructs a <tt>Printer</tt>.
         *
         * @param objects the objects to print
         * @param context the task context
         */
        public Printer(List<IMObject> objects, TaskContext context) {
            super(objects);
            this.context = context;
        }

        /**
         * Invoked when a print is cancelled. This restarts the task.
         */
        public void cancelled() {
            start(context);
        }

        /**
         * Notifies that the print was skipped. This restarts the task.
         */
        public void skipped() {
            start(context);
        }

        /**
         * Invoked when an object fails to print. This restarts the task.
         *
         * @param cause the reason for the failure
         */
        public void failed(Throwable cause) {
            ErrorHelper.show(cause, new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    start(context);
                }
            });
        }

        /**
         * Invoked when printing completes.
         */
        @Override
        protected void completed() {
            notifyCompleted();
        }
    }
}
