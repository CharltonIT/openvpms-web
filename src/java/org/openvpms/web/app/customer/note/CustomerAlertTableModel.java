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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.customer.note;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.collections.Transformer;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.alert.Alerts;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;
import org.openvpms.web.component.util.ColourHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;
import java.util.Map;


/**
 * Table model for <em>act.customerAlert</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerAlertTableModel extends AbstractActTableModel {

    /**
     * The priority column index.
     */
    private int priorityIndex;

    /**
     * The alert colunm index.
     */
    private int alertIndex;

    /**
     * Cache of priority lookup names, keyed on code.
     */
    private Map<String, String> priorities;


    /**
     * Constructs a <tt>CustomerAlertTableModel</tt>.
     *
     * @param shortNames the act archetype short names
     */
    public CustomerAlertTableModel(String[] shortNames) {
        super(shortNames);
    }

    /**
     * Returns the sort criteria.
     * <p/>
     * This implementation returns {@link VirtualNodeSortConstraint}s for the priority and alert columns.
     *
     * @param column    the primary sort column
     * @param ascending if <tt>true</tt> sort in ascending order; otherwise sort in <tt>descending</tt> order
     * @return the sort criteria, or <tt>null</tt> if the column isn't sortable
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        if (column == priorityIndex) {
            return new SortConstraint[]{new VirtualNodeSortConstraint("alertType.priority", ascending,
                                                                      PriorityTransformer.INSTANCE)};
        } else if (column == alertIndex) {
            return new SortConstraint[]{new VirtualNodeSortConstraint("alertType", ascending)};
        }
        return super.getSortConstraints(column, ascending);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(Act object, TableColumn column, int row) {
        int index = column.getModelIndex();
        if (index == priorityIndex) {
            return getPriority(object);
        } else if (index == alertIndex) {
            return getAlert(object);
        }
        return super.getValue(object, column, row);
    }

    /**
     * Creates a column model.
     * <p/>
     * This splits the <em>startTime</em> node into date and time columns.
     *
     * @param archetypes the archetypes
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        DefaultTableColumnModel model = (DefaultTableColumnModel) super.createColumnModel(archetypes, context);

        priorityIndex = getNextModelIndex(model);
        alertIndex = priorityIndex + 1;
        TableColumn priority = createTableColumn(priorityIndex, "alert.priority");
        TableColumn alert = createTableColumn(alertIndex, "alert.name");

        model.addColumn(priority);
        model.moveColumn(model.getColumnCount() - 1, 0);
        model.addColumn(alert);
        model.moveColumn(model.getColumnCount() - 1, 1);
        return model;
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return new String[]{"startTime", "endTime", "status", "reason"};
    }

    /**
     * Returns the priority of the act.
     *
     * @param act the act
     * @return a label representing the act's priority
     */
    private Label getPriority(Act act) {
        Label result = LabelFactory.create();
        Lookup lookup = getLookup(act);
        if (lookup != null) {
            IMObjectBean bean = new IMObjectBean(lookup);
            result.setText(getPriorityName(bean.getString("priority")));
            Color value = ColourHelper.getColor(bean.getString("colour"));
            if (value != null) {
                TableLayoutData layout = new TableLayoutData();
                result.setLayoutData(layout);
                layout.setBackground(value);
                if (ColourHelper.isCloserToBlackThanWhite(value)) {
                    result.setForeground(Color.WHITE);
                } else {
                    result.setForeground(Color.BLACK);
                }
            }
        }
        return result;
    }

    /**
     * Returns the alert name.
     *
     * @param act the act
     * @return a label containing the alert name
     */
    private Label getAlert(Act act) {
        Label result = LabelFactory.create();
        Lookup lookup = getLookup(act);
        if (lookup != null) {
            result.setText(lookup.getName());
        }
        return result;
    }

    /**
     * Returns the alert type associated with an act.
     *
     * @param act the act
     * @return the alert type, or <tt>null</tt> if none is found
     */
    private Lookup getLookup(Act act) {
        return ServiceHelper.getLookupService().getLookup(act, "alertType");
    }

    /**
     * Returns a priority name given its code.
     *
     * @param code the priority code.
     * @return the priority name, or <tt>code</tt> if none is found
     */
    private String getPriorityName(String code) {
        if (priorities == null) {
            priorities = LookupNameHelper.getLookupNames("lookup.patientAlertType", "priority");
        }
        String name = priorities.get(code);
        if (name == null) {
            name = code;
        }
        return name;
    }

    /**
     * Transformer that returns the priority of an <em>lookup.alertType</em> associated with an act.
     */
    private static class PriorityTransformer implements Transformer {

        /**
         * The singleton instance.
         */
        private static final Transformer INSTANCE = new PriorityTransformer();

        /**
         * Transforms the input object (leaving it unchanged) into some output object.
         *
         * @param input the object to be transformed, should be left unchanged
         * @return a transformed object
         * @throws ClassCastException       (runtime) if the input is the wrong class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws org.apache.commons.collections.FunctorException
         *                                  (runtime) if the transform cannot be completed
         */
        public Object transform(Object input) {
            Act act = (Act) input;
            Lookup lookup = LookupServiceHelper.getLookupService().getLookup(act, "alertType");
            if (lookup != null) {
                IMObjectBean bean = new IMObjectBean(lookup);
                String priority = bean.getString("priority");
                return priority != null ? Alerts.Priority.valueOf(priority) : null;
            }
            return null;
        }
    }

}