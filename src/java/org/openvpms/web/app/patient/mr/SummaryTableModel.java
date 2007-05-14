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

package org.openvpms.web.app.patient.mr;

import echopointng.LabelEx;
import echopointng.layout.TableLayoutDataEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.AbstractIMObjectTableModel;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.DateFormatter;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Patient medical history summary table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SummaryTableModel extends AbstractIMObjectTableModel<Act> {

    /**
     * A map of jxpath expressions, keyed on archetype short name,
     * used to format the text column.
     */
    private Map<String, String> expressions = new HashMap<String, String>();

    /**
     * The event column model index.
     */
    private static final int EVENT_INDEX = 0;

    /**
     * The date column model index.
     */
    private static final int DATE_INDEX = 1;

    /**
     * The archettype column model index.
     */
    private static final int TYPE_INDEX = 2;

    /**
     * The text column model index.
     */
    private static final int TEXT_INDEX = 3;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(SummaryTableModel.class);


    /**
     * Creates a new <tt>SummaryTableModel</tt>.
     */
    public SummaryTableModel() {
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(new TableColumn(EVENT_INDEX));
        model.addColumn(new TableColumn(DATE_INDEX));
        model.addColumn(new TableColumn(TYPE_INDEX));
        model.addColumn(new TableColumn(TEXT_INDEX));
        setTableColumnModel(model);
    }

    /**
     * A map of jxpath expressions, keyed on archetype short name,
     * used to format the text column.
     *
     * @param expressions the expressions
     */
    public void setExpressions(Map<String, String> expressions) {
        this.expressions = expressions;
    }

    /**
     * Returns a map of jxpath expressions, keyed on archetype short name,
     * used to format the text column.
     *
     * @return the expressions
     */
    public Map<String, String> getExpressions() {
        return expressions;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param act    the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    protected Object getValue(Act act, TableColumn column, int row) {
        Object result = null;
        try {
            if (TypeHelper.isA(act, PatientRecordTypes.CLINICAL_EVENT)) {
                result = formatEvent(act, column);
            } else {
                result = formatItem(act, column, row);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
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
     * Returns a component for an <em>act.patientClinicalEvent</em>.
     *
     * @param act    the event
     * @param column the column
     * @return a component representing the act
     * @throws OpenVPMSException for any error
     */
    private Component formatEvent(Act act, TableColumn column) {
        if (column.getModelIndex() == EVENT_INDEX) {
            ActBean bean = new ActBean(act);
            String completed = null;
            String clinician = null;
            String reason = getValue(bean, "reason");
            String status = ArchetypeServiceFunctions.lookup(act, "status");

            Date date = bean.getDate("endTime");
            if (date != null) {
                completed = getValue(DateFormatter.formatDate(date, false),
                                     bean, "completedTime");
            }

            IMObjectReference clinicianRef
                    = bean.getParticipantRef("participation.clinician");

            NodeSet name = IMObjectHelper.getNodes(clinicianRef, "name");
            if (name != null) {
                clinician = (String) name.get("name");
            }
            clinician = getValue(clinician, bean, "clinician");

            String text;
            if (completed == null
                    || ActStatus.IN_PROGRESS.equals(bean.getStatus())) {
                text = Messages.get("patient.record.summary.incomplete",
                                    reason, clinician, status);
            } else {
                text = Messages.get("patient.record.summary.complete",
                                    reason, clinician, completed);
            }
            Label summary = LabelFactory.create(null, "PatientSummary");
            summary.setText(text);
            summary.setLayoutData(new TableLayoutDataEx(4, 1));
            return summary;
        }
        return null;
    }

    /**
     * Returns a component for an act item.
     *
     * @param act the act item
     * @return a component representing the act
     * @throws OpenVPMSException for any error
     */
    private Component formatItem(Act act, TableColumn column, int row) {
        Component result = null;
        switch (column.getModelIndex()) {
            case EVENT_INDEX:
                result = RowFactory.create("Inset", new Label(""));
                break;
            case DATE_INDEX:
                result = getDate(act, row);
                break;
            case TYPE_INDEX:
                Label type = new Label(DescriptorHelper.getDisplayName(act));
                TableLayoutDataEx layout2 = new TableLayoutDataEx();
                layout2.setAlignment(
                        new Alignment(Alignment.DEFAULT, Alignment.TOP));
                type.setLayoutData(layout2);
                result = type;
                break;
            case TEXT_INDEX:
                result = getText(act);
                break;
        }
        return result;
    }

    /**
     * Returns a component to represent the act date. If the date is the same
     * as a prior date for the same parent act, an empty label is returned.
     *
     * @param act the act
     * @param row the act row
     * @return a component to represent the date
     */
    private Component getDate(Act act, int row) {
        Label date = null;
        boolean showDate = true;
        if (row > 0) {
            Act prev = getObject(row - 1);
            if (!TypeHelper.isA(prev, PatientRecordTypes.CLINICAL_EVENT)
                    && ObjectUtils.equals(act.getActivityStartTime(),
                                          prev.getActivityStartTime())) {
                // act belongs to the same parent act as the prior row,
                // and has the same date, so don't display it again
                showDate = false;
            }
        }
        if (showDate) {
            date = new Label(DateFormatter.formatDate(
                    act.getActivityStartTime(), false));
            TableLayoutDataEx layout = new TableLayoutDataEx();
            layout.setAlignment(
                    new Alignment(Alignment.DEFAULT, Alignment.TOP));
            date.setLayoutData(layout);
        }
        return date;
    }

    /**
     * Returns a component to represent act text.
     * If a jxpath expression is registered, this will be evaluated, otherwise
     * the act description will be used.
     *
     * @param act the act
     * @return a component
     */
    private Component getText(Act act) {
        Label result = null;
        String text = null;
        String shortName = act.getArchetypeId().getShortName();
        String expr = expressions.get(shortName);
        if (!StringUtils.isEmpty(expr)) {
            try {
                JXPathContext context = JXPathHelper.newContext(act);
                Object value = context.getValue(expr);
                if (value != null) {
                    text = value.toString();
                }
            } catch (Throwable exception) {
                log.error(exception, exception);
                text = exception.getMessage();
            }
        } else {
            text = act.getDescription();
        }
        if (text != null) {
            LabelEx label = new LabelEx(text);
            label.setIntepretNewlines(true);
            label.setLineWrap(true);
            result = label;
        }
        return result;
    }

    /**
     * Helper to return a value from a bean, or a formatted string if the value
     * is null.
     *
     * @param bean the bean
     * @param node the node
     * @return the value, or a formatting string indicating if the value was
     *         null
     */
    private String getValue(ActBean bean, String node) {
        return getValue(bean.getString(node), bean, node);
    }

    /**
     * Helper to return a value, or a formatted string if the value is null
     * or empty.
     *
     * @param value the value
     * @param bean  the bean the value came from
     * @param node  the value node
     * @return the value, or a formatting string indicating if the value was
     *         null
     */
    private String getValue(String value, ActBean bean, String node) {
        if (StringUtils.isEmpty(value)) {
            return Messages.get("patient.record.summary.novalue",
                                bean.getDisplayName(node));
        }
        return value;
    }
}
