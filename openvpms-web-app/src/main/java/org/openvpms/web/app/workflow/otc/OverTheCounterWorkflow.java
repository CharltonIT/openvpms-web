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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.workflow.otc;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.PracticeMailContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.DefaultIMObjectDeletionListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.Variable;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.help.HelpContext;

import java.util.Date;


/**
 * Over-the-counter workflow.
 *
 * @author Tim Anderson
 */
public class OverTheCounterWorkflow extends WorkflowImpl {

    /**
     * The initial context.
     */
    private TaskContext initial;

    /**
     * Customer account charges counter act short name.
     */
    private static final String CHARGES_COUNTER
        = "act.customerAccountChargesCounter";

    /**
     * Customer account payment act short name.
     */
    private static final String ACCOUNT_PAYMENT = "act.customerAccountPayment";

    /**
     * location over-the-counter entity relationship short name.
     */
    private static final String LOCATION_OTC = "entityRelationship.locationOTC";


    /**
     * Constructs an {@code OverTheCounterWorkflow}.
     *
     * @param parent the parent context
     * @param help   the help context
     * @throws ArchetypeServiceException for any archetype service error
     * @throws OTCException              for any OTC error
     */
    public OverTheCounterWorkflow(final Context parent, HelpContext help) {
        super(help);
        initial = new DefaultTaskContext(null, help);
        Party location = parent.getLocation();
        if (location == null) {
            throw new OTCException(OTCException.ErrorCode.NoLocation);
        }
        EntityBean bean = new EntityBean(location);
        Party otc = (Party) bean.getTargetEntity(LOCATION_OTC);
        if (otc == null) {
            throw new OTCException(OTCException.ErrorCode.NoOTC,
                                   location.getName());
        }
        initial.setCustomer(otc);
        initial.setTill(parent.getTill());
        initial.setLocation(parent.getLocation());
        initial.setPractice(parent.getPractice());
        initial.setUser(parent.getUser());

        EditIMObjectTask sale = new EditIMObjectTask(CHARGES_COUNTER, true);
        sale.setDeleteOnCancelOrSkip(true);
        addTask(sale);
        TaskProperties properties = new TaskProperties();
        properties.add("status", ActStatus.POSTED);
        properties.add(new Variable("startTime") {
            public Object getValue(TaskContext context) {
                return new Date(); // workaround for OVPMS-734. todo
            }
        });
        UpdateIMObjectTask postSale = new UpdateIMObjectTask(CHARGES_COUNTER,
                                                             properties);
        addTask(postSale);

        EditIMObjectTask payment = new OTCPaymentTask();
        payment.setDeleteOnCancelOrSkip(true);
        payment.addTaskListener(new DefaultTaskListener() {
            @Override
            public void taskEvent(TaskEvent event) {
                if (event.getType().equals(TaskEvent.Type.CANCELLED)) {
                    cancelSale();
                }
            }
        });
        addTask(payment);
        UpdateIMObjectTask postPayment = new UpdateIMObjectTask(
            ACCOUNT_PAYMENT, properties);
        addTask(postPayment);

        // optionally select and print the act.customerAccountChargesCounter
        PrintIMObjectTask printSale = new PrintIMObjectTask(CHARGES_COUNTER, new PracticeMailContext(parent));
        printSale.setRequired(false);

        // task to save the act.customerAccountChargesCounter, setting its
        // 'printed' flag, if the document is printed.
        TaskProperties saveProperties = new TaskProperties();
        saveProperties.add("printed", true);
        UpdateIMObjectTask saveSale = new UpdateIMObjectTask(ACCOUNT_PAYMENT,
                                                             saveProperties,
                                                             true);
        addTask(printSale);
        addTask(saveSale);

        // add a task to update the global context at the end of the workflow
        addTask(new SynchronousTask() {
            public void execute(TaskContext context) {
                parent.setTill(context.getTill());
                parent.setClinician(context.getClinician());
            }
        });

        setBreakOnSkip(true);
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    /**
     * Removes the <em>act.customerAccountChargesCounter</em> when the
     * payment is cancelled.
     */
    private void cancelSale() {
        IMObject sale = initial.getObject(CHARGES_COUNTER);
        sale = IMObjectHelper.reload(sale);
        if (sale != null) {
            // Workaround for OVPMS-1109. Note that this could lead to incorrect balances in the OTC account,
            // as the POSTED charge may have had other payments allocated against it.
            SaveHelper.delete(sale, new DefaultIMObjectDeletionListener());
//            try {
//                IMObjectEditor editor = IMObjectEditorFactory.create(sale, new DefaultLayoutContext(true));
//                editor.delete();
//            } catch (OpenVPMSException exception) {
//                String title = Messages.get("imobject.delete.failed.title");
//                ErrorHelper.show(title, exception);
//            }
        }
    }

}
