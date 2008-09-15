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

package org.openvpms.web.app.workflow.scheduling;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Appointment table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SingleScheduleTableModel extends AbstractIMTableModel<ObjectSet> {

    /**
     * The current schedule.
     */
    private Party schedule;

    /**
     * The start time index.
     */
    private static final int START_TIME_INDEX = 0;

    /**
     * The end time index.
     */
    private static final int END_TIME_INDEX = 1;

    /**
     * The status index.
     */
    private static final int STATUS_INDEX = 2;

    /**
     * The appointment name index.
     */
    private static final int APPOINTMENT_INDEX = 3;

    /**
     * The customer name index.
     */
    private static final int CUSTOMER_INDEX = 4;

    /**
     * The patient name index.
     */
    private static final int PATIENT_INDEX = 5;

    /**
     * The reason index.
     */
    private static final int REASON_INDEX = 6;

    /**
     * The description index.
     */
    private static final int DESCRIPTION_INDEX = 7;

    /**
     * The nodes to display.
     */
    public static final String[][] NODE_NAMES = new String[][]{
            {"startTime", Appointment.ACT_START_TIME},
            {"endTime", Appointment.ACT_END_TIME},
            {"status", Appointment.ACT_STATUS},
            {"appointmentType", Appointment.APPOINTMENT_TYPE_REFERENCE},
            {"customer", Appointment.CUSTOMER_REFERENCE},
            {"patient", Appointment.PATIENT_REFERENCE},
            {"reason", Appointment.ACT_REASON},
            {"description", Appointment.ACT_DESCRIPTION}};

    /**
     * The colunm names.
     */
    private String[] columnNames;

    /**
     * Cached status lookup names.
     */
    private Map<String, String> statuses;

    /**
     * Cached reason lookup names.
     */
    private Map<String, String> reasons;


    /**
     * Creates a new <code>TableModel</code>.
     */
    public SingleScheduleTableModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        for (int i = 0; i < NODE_NAMES.length; ++i) {
            model.addColumn(new TableColumn(i));
        }
        setTableColumnModel(model);

        columnNames = new String[NODE_NAMES.length];
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(
                "act.customerAppointment");
        if (archetype != null) {
            for (int i = 0; i < NODE_NAMES.length; ++i) {
                NodeDescriptor descriptor = archetype.getNodeDescriptor(
                        NODE_NAMES[i][0]);
                if (descriptor != null) {
                    columnNames[i] = descriptor.getDisplayName();
                }
            }
        }
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    public List<Party> getSchedules() {
        return (schedule != null) ? Arrays.asList(schedule)
                : Collections.<Party>emptyList();
    }

    /**
     * Sets the schedules to display.
     *
     * @param schedules the schedules
     */
    public void setSchedules(List<Party> schedules) {
        schedule = (!schedules.isEmpty()) ? schedules.get(0) : null;
    }

    public void setAppointments(Date day,
                                Map<Party, List<ObjectSet>> appointments) {

    }

    public Date getStartTime(int row) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the appointment at the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the appointment, or <tt>null</tt> if none is found
     */
    public ObjectSet getAppointment(int column, int row) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Party getSchedule(int column) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param set    the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    protected Object getValue(ObjectSet set, TableColumn column, int row) {
        Object result = null;
        int index = column.getModelIndex();
        Object value = set.get(NODE_NAMES[index][1]);
        switch (index) {
            case START_TIME_INDEX:
            case END_TIME_INDEX:
                Date date = (Date) value;
                Label label = LabelFactory.create();
                if (date != null) {
                    label.setText(DateHelper.formatTime(date, false));
                }
                result = label;
                break;
            case STATUS_INDEX:
                if (value instanceof String) {
                    result = getStatus(set, (String) value);
                }
                break;
            case REASON_INDEX:
                if (value instanceof String) {
                    result = getReason((String) value);
                }
                break;
            case DESCRIPTION_INDEX:
                result = value;
                break;
            case APPOINTMENT_INDEX:
                result = getViewer(set, Appointment.APPOINTMENT_TYPE_REFERENCE,
                                   Appointment.APPOINTMENT_TYPE_NAME, false);
                break;
            case CUSTOMER_INDEX:
                result = getViewer(set, Appointment.CUSTOMER_REFERENCE,
                                   Appointment.CUSTOMER_NAME, true);
                break;
            case PATIENT_INDEX:
                result = getViewer(set, Appointment.PATIENT_REFERENCE,
                                   Appointment.PATIENT_NAME, true);
                break;
        }
        return result;
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <code>true</code> sort in ascending order; otherwise
     *                  sort in <code>descending</code> order
     * @return the sort criteria, or <code>null</code> if the column isn't
     *         sortable
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        return null;
    }

    /**
     * Returns a status name given its code.
     *
     * @param set  the object set
     * @param code the status code
     * @return the status name
     */
    private String getStatus(ObjectSet set, String code) {
        String status = null;

        if (AppointmentStatus.CHECKED_IN.equals(code)) {
            Date arrival = set.getDate(Appointment.ARRIVAL_TIME);
            if (arrival != null) {
                String diff = DateHelper.formatTimeDiff(arrival, new Date());
                status = Messages.get("appointmenttablemodel.waiting", diff);
            }
        }
        if (status == null) {
            if (statuses == null) {
                statuses = LookupNameHelper.getLookupNames(
                        "act.customerAppointment", "status");
            }
            if (statuses != null) {
                status = statuses.get(code);
            }
        }
        return status;
    }

    /**
     * Returns a reason name given its code.
     *
     * @param code the reason code
     * @return the reason name
     */
    private String getReason(String code) {
        if (reasons == null) {
            reasons = LookupNameHelper.getLookupNames("act.customerAppointment",
                                                      "reason");
        }
        return (reasons != null) ? reasons.get(code) : null;
    }

    /**
     * Returns a viewer for an object reference.
     *
     * @param set     the object set
     * @param refKey  the object reference key
     * @param nameKey the entity name key
     * @param link    if <code>true</code> enable an hyperlink to the object
     * @return a new component to view the object reference
     */
    private Component getViewer(ObjectSet set, String refKey, String nameKey,
                                boolean link) {
        IMObjectReference ref = set.getReference(refKey);
        String name = set.getString(nameKey);
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(
                ref, name, link);
        return viewer.getComponent();
    }
}