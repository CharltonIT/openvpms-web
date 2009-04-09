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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.patient.mr;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.SelectField;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.reporting.reminder.ReminderGenerator;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ResendReminderDialog extends PopupDialog {

    /**
     * The reminder.
     */
    private final Act reminder;

    /**
     * The current reminder count.
     */
    private final int reminderCount;

    /**
     * The reminder processor.
     */
    private ReminderProcessor processor;

    /**
     * The reminder count selector.
     */
    private SelectField countSelector;

    /**
     * The email and location contact selector.
     */
    private SelectField contactSelector;

    /**
     * Error dialog title.
     */
    private static final String ERROR_TITLE = "patient.reminder.resend.error.title";


    /**
     * Creates a new <tt>ResendReminderDialog</tt>.
     *
     * @param reminder       the reminder
     * @param contacts       the customer's email and location contacts
     * @param reminderCounts the reminder counts that may be (re)sent
     * @param reminderCount  the current reminder count
     * @param processor      the reminder processor
     */
    private ResendReminderDialog(Act reminder, List<Contact> contacts, List<Integer> reminderCounts,
                                 int reminderCount, ReminderProcessor processor) {
        super(Messages.get("patient.reminder.resend.title"), OK_CANCEL);
        this.reminder = reminder;
        this.reminderCount = reminderCount;
        this.processor = processor;
        setModal(true);
        Grid grid = GridFactory.create(2);
        grid.add(LabelFactory.create("patient.reminder.resend.contacts"));
        IMObjectListModel model = new IMObjectListModel(contacts, false, false);
        contactSelector = SelectFieldFactory.create(model);
        contactSelector.setCellRenderer(IMObjectListCellRenderer.DESCRIPTION);
        contactSelector.setSelectedIndex(0);
        grid.add(contactSelector);

        grid.add(LabelFactory.create("patient.reminder.resend.reminderCount"));
        countSelector = SelectFieldFactory.create(reminderCounts);
        countSelector.setSelectedItem(reminderCount);
        if (countSelector.getSelectedIndex() < 0) {
            countSelector.setSelectedIndex(0);
        }
        grid.add(countSelector);
        getLayout().add(grid);
    }

    /**
     * Creates a new <tt>ResendReminderDialog</tt> for the supplied reminder.
     *
     * @param reminder the reminder
     * @return a new resend dialog, or <tt>null</tt> if the reminder can't be resent
     */
    public static ResendReminderDialog create(Act reminder) {
        ResendReminderDialog result = null;
        IMObjectBean bean = new IMObjectBean(reminder);
        int reminderCount = bean.getInt("reminderCount");
        ReminderProcessor processor = new ReminderProcessor(reminder.getActivityStartTime(),
                                                            reminder.getActivityEndTime(),
                                                            reminder.getActivityStartTime(),
                                                            ServiceHelper.getArchetypeService());
        ReminderEvent event = processor.process(reminder, reminderCount);
        Party customer = event.getCustomer();
        if (customer != null) {
            List<Contact> contacts = getContacts(customer);
            if (contacts != null && !contacts.isEmpty()) {
                List<Integer> counts = getReminderCounts(event.getReminderType(), reminderCount);
                if (!counts.isEmpty()) {
                    result = new ResendReminderDialog(reminder, contacts, counts, reminderCount, processor);
                } else {
                    ErrorHelper.show(Messages.get(ERROR_TITLE), Messages.get("patient.reminder.resend.notemplates",
                                                                             event.getReminderType().getName(),
                                                                             reminderCount));
                }
            } else {
                ErrorHelper.show(Messages.get(ERROR_TITLE), Messages.get("patient.reminder.resend.nocontacts"));
            }
        } else {
            ErrorHelper.show(Messages.get(ERROR_TITLE), Messages.get("patient.reminder.resend.nocustomer"));
        }
        return result;
    }

    /**
     * Invoked when the OK button is pressed.
     * <p/>
     * If a reminder count and contact have been selected, generates the reminder.
     */
    @Override
    protected void onOK() {
        Integer count = (Integer) countSelector.getSelectedItem();
        Contact contact = (Contact) contactSelector.getSelectedItem();
        if (count != null && contact != null) {
            generate(count, contact);
        }
    }

    /**
     * Generates a reminder.
     *
     * @param reminderCount the reminder count to use
     * @param contact       the contact to use
     */
    private void generate(final int reminderCount, Contact contact) {
        try {
            ReminderEvent event = processor.process(reminder, reminderCount);
            if (event.getDocumentTemplate() != null) {
                if (!ObjectUtils.equals(contact, event.getContact())) {
                    ReminderEvent.Action action = event.getAction();
                    if (TypeHelper.isA(contact, ContactArchetypes.LOCATION)) {
                        action = ReminderEvent.Action.PRINT;
                    } else if (TypeHelper.isA(contact, ContactArchetypes.EMAIL)) {
                        action = ReminderEvent.Action.EMAIL;
                    }
                    event = new ReminderEvent(action, event.getReminder(), event.getReminderType(),
                                              event.getCustomer(), contact, event.getDocumentTemplate());
                }
                final ReminderGenerator generator = new ReminderGenerator(event, GlobalContext.getInstance());
                generator.setUpdateOnCompletion(false);
                generator.setListener(new BatchProcessorListener() {
                    public void completed() {
                        if (generator.getProcessed() > 0) {
                            onGenerated(reminderCount);
                        }
                    }

                    public void error(Throwable exception) {
                        ErrorHelper.show(exception);
                    }
                });
                generator.process();
            } else {
                ErrorHelper.show(Messages.get(ERROR_TITLE),
                                 Messages.get("patient.reminder.resend.notemplate",
                                              event.getReminderType().getName(), reminderCount));
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when a reminder is generated.
     * <p/>
     * If the selected reminder count is the same as the original reminder count, a dialog will be displayed prompting
     * the user to update the reminder.
     *
     * @param count the count that the reminder was generated for
     */
    private void onGenerated(int count) {
        if (count == reminderCount) {
            final ConfirmationDialog dialog = new ConfirmationDialog(
                    Messages.get("patient.reminder.resend.update.title"),
                    Messages.get("patient.reminder.resend.update"));
            dialog.addWindowPaneListener(new PopupDialogListener() {
                public void onOK() {
                    update();
                }

                @Override
                protected void onAction(PopupDialog dialog) {
                    super.onAction(dialog);
                    close(); // close the ResendReminderDialog dialog
                }

            });
            dialog.show();
        } else {
            close();
        }
    }

    /**
     * Updates the reminder.
     */
    private void update() {
        try {
            ReminderRules rules = new ReminderRules();
            rules.updateReminder(reminder, new Date());
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Returns the location and email contacts for a customer.
     *
     * @param customer the customer
     * @return a list of location and email contacts
     */
    private static List<Contact> getContacts(Party customer) {
        List<Contact> result = new ArrayList<Contact>();
        for (Contact contact : customer.getContacts()) {
            if (TypeHelper.isA(contact, ContactArchetypes.LOCATION, ContactArchetypes.EMAIL)) {
                result.add(contact);
            }
        }
        return result;
    }

    /**
     * Returns an ordered list of reminder counts up to and including the current reminder count, that have an
     * associated document template.
     *
     * @param reminderType  the reminder type
     * @param reminderCount the current reminder count
     * @return the reminder counts
     */
    private static List<Integer> getReminderCounts(ReminderType reminderType, int reminderCount) {
        EntityBean bean = new EntityBean(reminderType.getEntity());
        Set<Integer> counts = new TreeSet<Integer>();

        for (EntityRelationship relationship : bean.getNodeRelationships("templates")) {
            IMObjectBean relBean = new IMObjectBean(relationship);
            int count = relBean.getInt("reminderCount");
            if (count <= reminderCount) {
                counts.add(count);
            }
        }
        return new ArrayList<Integer>(counts);
    }
}
