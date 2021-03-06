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

package org.openvpms.web.workspace.workflow.appointment;

import echopointng.KeyStrokes;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.TabbedBrowserListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.Selection;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.Workflow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.info.PatientContextHelper;
import org.openvpms.web.workspace.workflow.LocalClinicianContext;
import org.openvpms.web.workspace.workflow.WorkflowFactory;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleCRUDWindow;

import java.util.Date;
import java.util.List;


/**
 * Appointment CRUD window.
 *
 * @author Tim Anderson
 */
public class AppointmentCRUDWindow extends ScheduleCRUDWindow {

    /**
     * The browser.
     */
    private final AppointmentBrowser browser;

    /**
     * The rules.
     */
    private final AppointmentRules rules;

    /**
     * The original status of the appointment being edited.
     */
    private String oldStatus;

    /**
     * Check-in button identifier.
     */
    private static final String CHECKIN_ID = "checkin";


    /**
     * Constructs an {@link AppointmentCRUDWindow}.
     *
     * @param browser the browser
     * @param context the context
     * @param help    the help context
     */
    public AppointmentCRUDWindow(AppointmentBrowser browser, Context context, HelpContext help) {
        this(browser, AppointmentActions.INSTANCE, context, help);
    }

    /**
     * Constructs an {@link AppointmentCRUDWindow}.
     *
     * @param browser the browser
     * @param context the context
     * @param help    the help context
     */
    protected AppointmentCRUDWindow(AppointmentBrowser browser, AppointmentActions actions, Context context,
                                    HelpContext help) {
        super(Archetypes.create("act.customerAppointment", Act.class, Messages.get("workflow.scheduling.createtype")),
              actions, context, help);
        this.browser = browser;
        browser.setListener(new TabbedBrowserListener() {
            @Override
            public void onBrowserChanged() {
                enableButtons(getButtons(), getObject() != null);
            }
        });
        rules = ServiceHelper.getBean(AppointmentRules.class);
    }

    /**
     * Creates and edits a new appointment, if a slot has been selected.
     */
    @Override
    public void create() {
        if (canCreateAppointment()) {
            super.create();
        }
    }

    /**
     * Determines the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected AppointmentActions getActions() {
        return (AppointmentActions) super.getActions();
    }

    /**
     * Edits an object.
     *
     * @param object the object to edit
     * @param path   the selection path. May be {@code null}
     */
    @Override
    protected void edit(Act object, List<Selection> path) {
        oldStatus = object.getStatus();
        super.edit(object, path);
    }

    /**
     * Edits an object.
     *
     * @param editor the object editor
     * @param path   the selection path. May be {@code null}
     * @return the edit dialog
     */
    @Override
    protected EditDialog edit(IMObjectEditor editor, List<Selection> path) {
        Date startTime = browser.getSelectedTime();
        if (startTime != null && editor.getObject().isNew() && editor instanceof AppointmentActEditor) {
            ((AppointmentActEditor) editor).setStartTime(startTime);
        }
        return super.edit(editor, path);
    }


    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(Act object, boolean isNew) {
        super.onSaved(object, isNew);
        String newStatus = object.getStatus();
        User user = getContext().getUser();
        if (!AppointmentStatus.CANCELLED.equals(oldStatus) && AppointmentStatus.CANCELLED.equals(newStatus)) {
            PatientContext context = getPatientContext(object);
            if (context != null) {
                PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
                service.admissionCancelled(context, user);
            }
        } else if (!isAdmitted(oldStatus) && isAdmitted(newStatus)) {
            PatientContext context = getPatientContext(object);
            if (context != null) {
                PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
                service.admitted(context, user);
            }
        } else if (isAdmitted(oldStatus) && !isAdmitted(newStatus)) {
            PatientContext context = getPatientContext(object);
            if (context != null) {
                PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
                service.discharged(context, user);
            }
        }
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        Button checkIn = ButtonFactory.create(CHECKIN_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onCheckIn();
            }
        });
        buttons.add(checkIn);
        buttons.add(createConsultButton());
        buttons.add(createCheckOutButton());
        buttons.add(createOverTheCounterButton());
        buttons.addKeyListener(KeyStrokes.CONTROL_MASK | 'C', new ActionListener() {
            public void onAction(ActionEvent event) {
                onCopy();
            }
        });
        buttons.addKeyListener(KeyStrokes.CONTROL_MASK | 'X', new ActionListener() {
            public void onAction(ActionEvent event) {
                onCut();
            }
        });
        buttons.addKeyListener(KeyStrokes.CONTROL_MASK | 'V', new ActionListener() {
            public void onAction(ActionEvent event) {
                onPaste();
            }
        });
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        enable = browser.isAppointmentsSelected() && enable;
        super.enableButtons(buttons, enable);
        boolean checkInEnabled = false;
        boolean checkoutConsultEnabled = false;
        if (enable) {
            Act act = getObject();
            AppointmentActions actions = getActions();
            if (actions.canCheckIn(act)) {
                checkInEnabled = true;
                checkoutConsultEnabled = false;
            } else if (actions.canCheckoutOrConsult(act)) {
                checkInEnabled = false;
                checkoutConsultEnabled = true;
            }
        }
        buttons.setEnabled(NEW_ID, canCreateAppointment());
        buttons.setEnabled(CHECKIN_ID, checkInEnabled);
        buttons.setEnabled(CONSULT_ID, checkoutConsultEnabled);
        buttons.setEnabled(CHECKOUT_ID, checkoutConsultEnabled);
        buttons.setEnabled(OVER_THE_COUNTER_ID, browser.isAppointmentsSelected());
    }

    /**
     * Creates a layout context for editing an object.
     *
     * @param help the help context
     * @return a new layout context.
     */
    @Override
    protected LayoutContext createLayoutContext(HelpContext help) {
        // create a local context - don't want to pick up the current clinician
        Context local = new LocalClinicianContext(getContext());
        return new DefaultLayoutContext(true, local, help);
    }

    /**
     * Creates a new edit dialog.
     *
     * @param editor the editor
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return new AppointmentEditDialog(editor, getContext());
    }

    /**
     * Determines if an appointment can be created.
     *
     * @return {@code true} if a schedule and slot has been selected
     */
    private boolean canCreateAppointment() {
        return browser.isAppointmentsSelected() && browser.getSelectedSchedule() != null
               && browser.getSelectedTime() != null;
    }

    /**
     * Invoked when the 'check-in' button is pressed.
     */
    private void onCheckIn() {
        Act act = IMObjectHelper.reload(getObject());
        // make sure the act is still available and can be checked in prior to beginning workflow
        if (act != null && getActions().canCheckIn(act)) {
            WorkflowFactory factory = ServiceHelper.getBean(WorkflowFactory.class);
            Workflow workflow = factory.createCheckInWorkflow(act, getContext(), getHelpContext());
            workflow.addTaskListener(new DefaultTaskListener() {
                public void taskEvent(TaskEvent event) {
                    onRefresh(getObject());
                }
            });
            workflow.start();
        } else {
            onRefresh(getObject());
        }
    }

    /**
     * Invoked to copy an appointment.
     */
    private void onCopy() {
        if (browser.isAppointmentsSelected()) {
            browser.clearMarked();
            PropertySet selected = browser.getSelected();
            Act appointment = browser.getAct(selected);
            if (appointment != null) {
                browser.setMarked(selected, false);
            } else {
                InformationDialog.show(Messages.get("workflow.scheduling.appointment.copy.title"),
                                       Messages.get("workflow.scheduling.appointment.copy.select"));
            }
        }
    }

    /**
     * Invoked to cut an appointment.
     */
    private void onCut() {
        if (browser.isAppointmentsSelected()) {
            browser.clearMarked();
            PropertySet selected = browser.getSelected();
            Act appointment = browser.getAct(selected);
            if (appointment != null) {
                if (AppointmentStatus.PENDING.equals(appointment.getStatus())) {
                    browser.setMarked(selected, true);
                } else {
                    InformationDialog.show(Messages.get("workflow.scheduling.appointment.cut.title"),
                                           Messages.get("workflow.scheduling.appointment.cut.pending"));
                }
            } else {
                InformationDialog.show(Messages.get("workflow.scheduling.appointment.cut.title"),
                                       Messages.get("workflow.scheduling.appointment.cut.select"));
            }
        }
    }

    /**
     * Invoked to paste an appointment.
     * <p/>
     * For the paste to be successful:
     * <ul>
     * <li>the appointment must still exist
     * <li>for cut appointments, the appointment must be PENDING
     * <li>a schedule must be selected
     * <li>a time slot must be selected
     * </ul>
     */
    private void onPaste() {
        if (browser.isAppointmentsSelected()) {
            if (browser.getMarked() == null) {
                InformationDialog.show(Messages.get("workflow.scheduling.appointment.paste.title"),
                                       Messages.get("workflow.scheduling.appointment.paste.select"));
            } else {
                Act appointment = browser.getAct(browser.getMarked());
                Entity schedule = browser.getSelectedSchedule();
                Date startTime = browser.getSelectedTime();
                if (appointment == null) {
                    InformationDialog.show(Messages.get("workflow.scheduling.appointment.paste.title"),
                                           Messages.get("workflow.scheduling.appointment.paste.noexist"));
                    onRefresh(appointment); // force redraw
                    browser.clearMarked();
                } else if (browser.isCut() && !AppointmentStatus.PENDING.equals(appointment.getStatus())) {
                    InformationDialog.show(Messages.get("workflow.scheduling.appointment.paste.title"),
                                           Messages.get("workflow.scheduling.appointment.paste.pending"));
                    onRefresh(appointment); // force redraw
                    browser.clearMarked();
                } else if (schedule == null || startTime == null) {
                    InformationDialog.show(Messages.get("workflow.scheduling.appointment.paste.title"),
                                           Messages.get("workflow.scheduling.appointment.paste.noslot"));
                } else if (browser.isCut()) {
                    cut(appointment, schedule, startTime);
                } else {
                    copy(appointment, schedule, startTime);
                }
            }
        }
    }

    /**
     * Cuts an appointment and pastes it to the specified schedule and start time.
     *
     * @param appointment the appointment
     * @param schedule    the new schedule
     * @param startTime   the new start time
     */
    private void cut(Act appointment, Entity schedule, Date startTime) {
        paste(appointment, schedule, startTime);
        browser.clearMarked();
    }

    /**
     * Copies an appointment and pastes it to the specified schedule and start time.
     *
     * @param appointment the appointment
     * @param schedule    the new schedule
     * @param startTime   the new start time
     */
    private void copy(Act appointment, Entity schedule, Date startTime) {
        appointment = rules.copy(appointment);
        ActBean bean = new ActBean(appointment);
        bean.setValue("status", AppointmentStatus.PENDING);
        bean.setValue("arrivalTime", null);
        bean.setParticipant(UserArchetypes.AUTHOR_PARTICIPATION, getContext().getUser());
        paste(appointment, schedule, startTime);
    }

    /**
     * Pastes an appointment to the specified schedule and start time.
     *
     * @param appointment the appointment
     * @param schedule    the new schedule
     * @param startTime   the new start time
     */
    private void paste(Act appointment, Entity schedule, Date startTime) {
        HelpContext edit = createEditTopic(appointment);
        DefaultLayoutContext context = new DefaultLayoutContext(getContext(), edit);
        AppointmentActEditor editor = new AppointmentActEditor(appointment, null, context);
        EditDialog dialog = edit(editor, null);  // NOTE: need to update the start time after dialog is created
        editor.setSchedule(schedule);            //       See AppointmentEditDialog.timesModified().
        editor.setStartTime(startTime); // will recalc end time
        dialog.save(true);              // checks for overlapping appointments
        browser.setSelected(browser.getEvent(appointment));
    }

    /**
     * Returns the patient context for an appointment.
     *
     * @param appointment the appointment
     * @return the patient context, or {@code null} if the patient can't be found, or has no current visit
     */
    private PatientContext getPatientContext(Act appointment) {
        return PatientContextHelper.getAppointmentContext(appointment, getContext());
    }

    /**
     * Determines if an appointment status indicates the patient has been admitted.
     *
     * @param status the appointment status
     * @return {@code true} if the patient has been admitted
     */
    private boolean isAdmitted(String status) {
        return AppointmentStatus.CHECKED_IN.equals(status) || AppointmentStatus.ADMITTED.equals(status)
               || AppointmentStatus.IN_PROGRESS.equals(status) || AppointmentStatus.BILLED.equals(status);
    }

    protected static class AppointmentActions extends ScheduleActions {

        public static AppointmentActions INSTANCE = new AppointmentActions();

        /**
         * Determines if an appointment can be checked in.
         *
         * @param act the appointment
         * @return {@code true} if it can be checked in
         */
        public boolean canCheckIn(Act act) {
            return AppointmentStatus.PENDING.equals(act.getStatus());
        }

        /**
         * Determines if a consultation or checkout can be performed on an act.
         *
         * @param act the act
         * @return {@code true} if consultation can be performed
         */
        @Override
        public boolean canCheckoutOrConsult(Act act) {
            String status = act.getStatus();
            return AppointmentStatus.CHECKED_IN.equals(status)
                   || AppointmentStatus.IN_PROGRESS.equals(status)
                   || AppointmentStatus.COMPLETED.equals(status)
                   || AppointmentStatus.BILLED.equals(status);
        }
    }
}
