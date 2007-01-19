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
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.print.IMObjectPrinterFactory;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterListener;
import org.openvpms.web.component.im.print.InteractiveIMObjectPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Task to allow the user to selectively print any unprinted documents.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class PrintDocumentsTask extends AbstractTask {

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     */
    public void start(TaskContext context) {
        List<IMObject> unprinted = new ArrayList<IMObject>();
        unprinted.addAll(getCustomerActs(context));
        unprinted.addAll(getPatientActs(context));
        if (unprinted.isEmpty()) {
            notifyCompleted();
        } else {
            String title = Messages.get("workflow.checkout.print.title");
            String[] buttons = isRequired()
                    ? PopupDialog.OK_CANCEL : PopupDialog.OK_SKIP_CANCEL;
            final BatchPrintDialog dialog = new BatchPrintDialog(title, buttons,
                                                                 unprinted);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    String action = dialog.getAction();
                    if (action.equals(BatchPrintDialog.OK_ID)) {
                        print(dialog.getSelected());
                    } else if (action.equals(BatchPrintDialog.SKIP_ID)) {
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
     */
    private void print(List<IMObject> objects) {
        BatchPrinter printer = new BatchPrinter(objects);
        printer.print();
    }

    /**
     * Returns a list of unprinted customer charges and payments.
     *
     * @param context the task context
     * @return a list of unprinted customer charges and payments
     */
    private List<IMObject> getCustomerActs(TaskContext context) {
        String[] shortNames = {"act.customerAccountCharges*",
                               "act.customerAccountPayment"};
        Party customer = context.getCustomer();
        String node = "customer";
        String participation = "participation.customer";
        return getUnprintedActs(shortNames, customer, node, participation);
    }

    /**
     * Returns a list of unprinted patient documents.
     *
     * @param context the task context
     * @return a list of unprinted patient documents
     */
    private List<IMObject> getPatientActs(TaskContext context) {
        String[] shortNames = {"act.patientDocument*"};
        Party patient = context.getPatient();
        String node = "patient";
        String participation = "participation.patient";
        return getUnprintedActs(shortNames, patient, node, participation);
    }

    /**
     * Returns a list of unprinted acts for a party.
     *
     * @param shortNames    the act short names to query
     * @param party         the party to query
     * @param node          the participation node to query
     * @param participation the participation short name to query
     */
    private List<IMObject> getUnprintedActs(String[] shortNames,
                                            Party party,
                                            String node,
                                            String participation) {
        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, true);
        query.setFirstResult(0);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        CollectionNodeConstraint participations
                = new CollectionNodeConstraint(node, participation,
                                               false, true);
        participations.add(new ObjectRefNodeConstraint(
                "entity", party.getObjectReference()));

        query.add(participations);
        query.add(new NodeConstraint("printed", false));

        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        return service.get(query).getResults();

    }

    /**
     * Batch printer. Note that printing currently occurs interactively
     * due to limitations in downloading multiple pdf files to the client
     * browser.
     */
    class BatchPrinter implements IMPrinterListener {

        /**
         * Iterator over the objects to  print.
         */
        private Iterator<IMObject> iterator;

        /**
         * The object being printed.
         */
        private IMObject object;

        /**
         * Constructs a new <code>BatchPrinter</code>.
         *
         * @param objects the objects to print
         */
        public BatchPrinter(List<IMObject> objects) {
            iterator = objects.iterator();
        }

        /**
         * Initiates printing of the objects.
         */
        public void print() {
            if (iterator.hasNext()) {
                object = iterator.next();
                IMPrinter<IMObject> printer
                        = IMObjectPrinterFactory.create(object);
                InteractiveIMPrinter<IMObject> iPrinter
                        = new InteractiveIMObjectPrinter<IMObject>(printer);
                iPrinter.setListener(this);
                iPrinter.print();
            } else {
                notifyCompleted();
            }
        }

        /**
         * Invoked when an object has been successfully printed.
         */
        public void printed() {
            boolean next = false;
            try {
                // update the print flag, if it exists
                IMObjectBean bean = new IMObjectBean(object);
                if (bean.hasNode("printed")) {
                    bean.setValue("printed", true);
                    bean.save();
                }
                next = true;
            } catch (OpenVPMSException exception) {
                failed(exception);
            }
            if (next) {
                print(); // print the next available object
            }
        }

        /**
         * Notifies that the print was cancelled.
         */
        public void cancelled() {
            notifyCancelled();
        }

        /**
         * Notifies that the print was skipped.
         */
        public void skipped() {
            notifySkipped();
        }

        /**
         * Invoked when an object fails to print.
         *
         * @param cause the reason for the failure
         */
        public void failed(Throwable cause) {
            ErrorHelper.show(cause, new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    notifyCancelled();
                }
            });
        }
    }
}
