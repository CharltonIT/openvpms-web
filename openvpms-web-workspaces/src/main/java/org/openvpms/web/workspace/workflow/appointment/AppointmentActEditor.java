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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.i18n.time.DateDurationFormatter;
import org.openvpms.archetype.i18n.time.DurationFormatter;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientParticipationEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.alert.AlertSummary;
import org.openvpms.web.workspace.customer.CustomerSummary;
import org.openvpms.web.workspace.patient.summary.CustomerPatientSummaryFactory;
import org.openvpms.web.workspace.workflow.scheduling.AbstractScheduleActEditor;
import org.openvpms.web.workspace.workflow.scheduling.SchedulingHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.openvpms.web.echo.style.Styles.BOLD;
import static org.openvpms.web.echo.style.Styles.CELL_SPACING;
import static org.openvpms.web.echo.style.Styles.INSET;


/**
 * An editor for <em>act.customerAppointment</em>s.
 *
 * @author Tim Anderson
 */
public class AppointmentActEditor extends AbstractScheduleActEditor {

    /**
     * The alerts row.
     */
    private Row alerts;

    /**
     * Listener notified when the patient changes.
     */
    private ModifiableListener patientListener;

    /**
     * The appointment slot size.
     */
    private int slotSize;

    /**
     * The appointment rules.
     */
    private final AppointmentRules rules;

    /**
     * The appointment duration.
     */
    private Label duration = LabelFactory.create();

    /**
     * The appointment duration formatter.
     */
    private static DurationFormatter formatter = DateDurationFormatter.create(true, true, true, true, true, true);

    /**
     * Constructs an {@link AppointmentActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public AppointmentActEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        rules = ServiceHelper.getBean(AppointmentRules.class);
        initParticipant("schedule", context.getContext().getSchedule());

        Entity appointmentType = (Entity) getParticipant("appointmentType");
        if (appointmentType == null) {
            // set the appointment type to the default for the schedule
            Party schedule = (Party) getParticipant("schedule");
            if (schedule != null) {
                appointmentType = getDefaultAppointmentType(schedule);
                setParticipant("appointmentType", appointmentType);
            }
        }

        Date startTime = getStartTime();
        if (startTime == null) {
            Date scheduleDate = context.getContext().getScheduleDate();
            if (scheduleDate != null) {
                startTime = getDefaultStartTime(scheduleDate);
                setStartTime(startTime, true);
            }
        }

        getProperty("status").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStatusChanged();
            }
        });
        addStartEndTimeListeners();
        updateRelativeDate();
        updateDuration();
        updateAlerts();
    }

    /**
     * Sets the schedule.
     *
     * @param schedule the schedule
     */
    public void setSchedule(Entity schedule) {
        setParticipant("schedule", schedule);
        onScheduleChanged(schedule);
        calculateEndTime();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AppointmentLayoutStrategy();
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();

        Party schedule = (Party) getParticipant("schedule");
        AppointmentTypeParticipationEditor editor = onScheduleChanged(schedule);
        editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onAppointmentTypeChanged();
            }
        });
        getPatientEditor().addModifiableListener(getPatientListener());

        if (getEndTime() == null) {
            calculateEndTime();
        }
    }

    /**
     * Invoked when the start time changes. Calculates the end time.
     */
    @Override
    protected void onStartTimeChanged() {
        Date start = getStartTime();
        if (start != null && slotSize != 0) {
            Date rounded = SchedulingHelper.getSlotTime(start, slotSize, false);
            if (DateRules.compareTo(start, rounded) != 0) {
                setStartTime(rounded, true);
            }
        }

        try {
            calculateEndTime();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        updateRelativeDate();
        updateDuration();
    }

    /**
     * Invoked when the end time changes. Recalculates the end time if it is less than the start time.
     */
    @Override
    protected void onEndTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (end.compareTo(start) < 0) {
                calculateEndTime();
            } else if (slotSize != 0) {
                Date rounded = SchedulingHelper.getSlotTime(end, slotSize, true);
                if (DateRules.compareTo(end, rounded) != 0) {
                    setEndTime(rounded, true);
                }
            }
        }
        updateDuration();
    }

    /**
     * Invoked when the customer changes. Sets the patient to null if no
     * relationship exists between the two.
     * <p/>
     * The alerts will be updated.
     */
    @Override
    protected void onCustomerChanged() {
        PatientParticipationEditor editor = getPatientEditor();
        editor.removeModifiableListener(patientListener);
        super.onCustomerChanged();
        editor.addModifiableListener(patientListener);
        updateAlerts();
    }

    /**
     * Updates the end-time editor to base relative dates on the start time.
     */
    protected void updateRelativeDate() {
        getEndTimeEditor().setRelativeDate(getStartTime());
    }

    /**
     * Updates the appointment duration display.
     */
    private void updateDuration() {
        Date startTime = getStartTime();
        Date endTime = getEndTime();
        if (startTime != null && endTime != null) {
            duration.setText(formatter.format(startTime, endTime));
        } else {
            duration.setText(null);
        }
    }

    /**
     * Invoked when the schedule is updated. This propagates it to the appointment type editor, and gets the new slot
     * size.
     *
     * @param schedule the schedule. May be {@code null}
     * @return the appointment type editor
     */
    private AppointmentTypeParticipationEditor onScheduleChanged(Entity schedule) {
        AppointmentTypeParticipationEditor editor = getAppointmentTypeEditor();
        editor.setSchedule(schedule);
        if (schedule != null) {
            slotSize = rules.getSlotSize((Party) schedule);
        }
        return editor;
    }

    /**
     * Invoked when the patient changes. This updates the alerts.
     */
    private void onPatientChanged() {
        updateAlerts();
    }

    /**
     * Updates the alerts associated with the custopmer and patient.
     */
    private void updateAlerts() {
        Component container = getAlertsContainer();
        container.removeAll();
        Component alerts = createAlerts();
        if (alerts != null) {
            container.add(alerts);
        }
    }

    /**
     * Creates a component representing the customerr and patient alerts.
     *
     * @return the alerts component or {@code null} if neither has alerts
     */
    private Component createAlerts() {
        Component result = null;
        Component customerSummary = null;
        Component patientSummary = null;
        Party customer = getCustomer();
        Party patient = getPatient();
        if (customer != null) {
            customerSummary = getCustomerAlerts(customer);
        }
        if (patient != null) {
            patientSummary = getPatientAlerts(patient);
        }
        if (customerSummary != null || patientSummary != null) {
            result = RowFactory.create(CELL_SPACING);
            if (customerSummary != null) {
                result.add(customerSummary);
            }
            if (patientSummary != null) {
                result.add(patientSummary);
            }
            result = RowFactory.create(INSET, result);
        }
        return result;
    }

    /**
     * Returns any alerts associated with the customer.
     *
     * @param customer the customer
     * @return any alerts associated with the customer, or {@code null} if the customer has no alerts
     */
    private Component getCustomerAlerts(Party customer) {
        Component result = null;
        LayoutContext context = getLayoutContext();
        CustomerSummary summary = new CustomerSummary(context.getContext(), context.getHelpContext());
        AlertSummary alerts = summary.getAlertSummary(customer);
        if (alerts != null) {
            result = ColumnFactory.create("AppointmentActEditor.Alerts", LabelFactory.create("alerts.customer", BOLD),
                                          alerts.getComponent());
        }
        return result;
    }

    /**
     * Returns any alerts associated with the patient.
     *
     * @param patient the patient
     * @return any alerts associated with the patient, or {@code null} if the patient has no alerts
     */
    private Component getPatientAlerts(Party patient) {
        Component result = null;
        LayoutContext layout = getLayoutContext();
        Context context = layout.getContext();
        HelpContext help = layout.getHelpContext();
        CustomerPatientSummaryFactory factory = ServiceHelper.getBean(CustomerPatientSummaryFactory.class);
        AlertSummary alerts = factory.createPatientSummary(context, help).getAlertSummary(patient);
        if (alerts != null) {
            result = ColumnFactory.create("AppointmentActEditor.Alerts", LabelFactory.create("alerts.patient", BOLD),
                                          alerts.getComponent());
        }
        return result;
    }

    /**
     * Returns the patient listener.
     *
     * @return the patient listener
     */
    private ModifiableListener getPatientListener() {
        if (patientListener == null) {
            patientListener = new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onPatientChanged();
                }
            };
        }
        return patientListener;
    }

    /**
     * Invoked when the appointment type changes. Calculates the end time
     * if the start time is set.
     */
    private void onAppointmentTypeChanged() {
        try {
            calculateEndTime();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the status changes. Sets the arrivalTime to now if
     * the status is CHECKED_IN.
     */
    private void onStatusChanged() {
        String status = (String) getProperty("status").getValue();
        if (AppointmentStatus.CHECKED_IN.equals(status)) {
            getProperty("arrivalTime").setValue(new Date());
        }
    }

    /**
     * Calculates the default start time of an appointment, using the supplied
     * date and current time.
     * The start time is rounded to the next nearest 'slot-size' interval.
     *
     * @param date the start date
     * @return the start time
     */
    private Date getDefaultStartTime(Date date) {
        int slotSize = 0;
        Party schedule = (Party) getParticipant("schedule");
        if (schedule != null) {
            slotSize = rules.getSlotSize(schedule);
        }

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        Calendar timeCal = new GregorianCalendar();
        timeCal.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (slotSize != 0) {
            int mins = calendar.get(Calendar.MINUTE);
            mins = ((mins / slotSize) * slotSize) + slotSize;
            calendar.set(Calendar.MINUTE, mins);
        }
        return calendar.getTime();
    }

    /**
     * Calculates the end time if the start time and appointment type are set.
     *
     * @throws OpenVPMSException for any error
     */
    private void calculateEndTime() {
        Date start = getStartTime();
        Party schedule = (Party) getParticipant("schedule");
        AppointmentTypeParticipationEditor editor
                = getAppointmentTypeEditor();
        Entity appointmentType = editor.getEntity();
        if (start != null && schedule != null && appointmentType != null) {
            Date end = rules.calculateEndTime(start, schedule, appointmentType);
            setEndTime(end);
        }
    }

    /**
     * Returns the appointment type editor.
     *
     * @return the appointment type editor
     */
    private AppointmentTypeParticipationEditor getAppointmentTypeEditor() {
        ParticipationEditor<Entity> result = getParticipationEditor("appointmentType", true);
        return (AppointmentTypeParticipationEditor) result;
    }

    /**
     * Returns the default appointment type associated with a schedule.
     *
     * @param schedule the schedule
     * @return the default appointment type, or the the first appointment type
     *         if there is no default, or {@code null} if none is found
     */
    private Entity getDefaultAppointmentType(Party schedule) {
        return rules.getDefaultAppointmentType(schedule);
    }

    /**
     * Returns the alerts container.
     *
     * @return the alerts container
     */
    private Component getAlertsContainer() {
        if (alerts == null) {
            alerts = new Row();
        }
        return alerts;
    }

    private class AppointmentLayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Constructs an {@link AppointmentLayoutStrategy}.
         */
        public AppointmentLayoutStrategy() {
            addComponent(new ComponentState(getStartTimeEditor()));
            addComponent(new ComponentState(getEndTimeEditor()));
        }

        /**
         * Lays out components in a grid.
         *
         * @param object     the object to lay out
         * @param properties the properties
         * @param context    the layout context
         */
        @Override
        protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context) {
            ComponentGrid grid = super.createGrid(object, properties, context);
            ComponentState state = new ComponentState(duration, null, null,
                                                      Messages.get("workflow.scheduling.appointment.duration"));
            grid.add(state);
            return grid;
        }

        /**
         * Lay out out the object in the specified container.
         *
         * @param object     the object to lay out
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param container  the container to use
         * @param context    the layout context
         */
        @Override
        protected void doLayout(IMObject object, PropertySet properties, IMObject parent, Component container,
                                LayoutContext context) {
            super.doLayout(object, properties, parent, container, context);
            container.add(getAlertsContainer());
        }

        /**
         * Returns the default focus component.
         * <p/>
         * This implementation returns the customer component.
         *
         * @param components the components
         * @return the customer component, or {@code null} if none is found
         */
        @Override
        protected Component getDefaultFocus(ComponentSet components) {
            return components.getFocusable("customer");
        }
    }
}
