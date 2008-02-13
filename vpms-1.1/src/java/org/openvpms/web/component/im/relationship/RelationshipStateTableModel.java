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
 *
 *  $Id$
 */

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;


/**
 * A table model for {@link RelationshipState} instances, that renders
 * the source or target name and description, and the relationship description.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipStateTableModel
        extends AbstractIMTableModel<RelationshipState> {

    /**
     * Dummy node name for the 'name' column.
     */
    public static final String NAME_NODE = "name";

    /**
     * Dummy node name for the 'description' column.
     */
    public static final String DESCRIPTION_NODE = "description";

    /**
     * Dummy node name for the 'detail' column.
     */
    public static final String DETAIL_NODE = "detail";

    /**
     * The layout context.
     */
    private final LayoutContext layoutContext;

    /**
     * If <tt>true</tt> displays the target of the relationship; otherwise
     * displays the source.
     */
    private final boolean displayTarget;

    /**
     * Name column index.
     */
    private static final int NAME_INDEX = 1;

    /**
     * Description column index.
     */
    private static final int DESCRIPTION_INDEX = 2;

    /**
     * Entity relationship description index.
     */
    private static final int DETAIL_INDEX = 3;


    /**
     * Construct a new <tt>RelationshipStateTableModel</tt>.
     *
     * @param context       layout context
     * @param displayTarget if <tt>true</tt> display the relationship target,
     *                      otherwise display the source
     */
    public RelationshipStateTableModel(LayoutContext context,
                                       boolean displayTarget) {
        layoutContext = context;
        this.displayTarget = displayTarget;
        setTableColumnModel(createTableColumnModel());
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <tt>true</tt> sort in ascending order; otherwise
     *                  sort in <tt>descending</tt> order
     * @return the sort criteria, or <tt>null</tt> if the column isn't
     *         sortable
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint result = null;
        TableColumn col = getColumn(column);
        if (col.getModelIndex() == NAME_INDEX) {
            result = new NodeSortConstraint(NAME_NODE, ascending);
        } else if (col.getModelIndex() == DESCRIPTION_INDEX) {
            result = new NodeSortConstraint(DESCRIPTION_NODE, ascending);
        } else if (col.getModelIndex() == DETAIL_INDEX) {
            result = new NodeSortConstraint(DETAIL_NODE, ascending);
        }
        return (result != null) ? new SortConstraint[]{result} : null;
    }

    /**
     * Determines if selection should be enabled. This implementation returns
     * <tt>true</tt> if in edit mode.
     *
     * @return <tt>true</tt> if selection should be enabled; otherwise
     *         <tt>false</tt>
     */
    @Override
    public boolean getEnableSelection() {
        return layoutContext.isEdit();
    }

    /**
     * Indicates whether to display the target or source of the relationship.
     *
     * @return <tt>true</tt> to display the target of the relationship, or
     *         <tt>false</tt> to display the source.
     */
    protected boolean displayTarget() {
        return displayTarget;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(RelationshipState object, TableColumn column,
                              int row) {
        Object result = null;
        switch (column.getModelIndex()) {
            case NAME_INDEX:
                result = getEntityViewer(object);
                break;
            case DESCRIPTION_INDEX:
                result = getDescription(object);
                break;
            case DETAIL_INDEX:
                result = object.getRelationship().getDescription();
                break;
        }
        return result;
    }

    /**
     * Returns a viewer for the source or target entity of the relationship,
     * depending on the {@link #displayTarget} flag.
     *
     * @param state the relationship state
     * @return a viewer for the entity
     */
    protected Component getEntityViewer(RelationshipState state) {
        IMObjectReference ref;
        String name;
        if (displayTarget) {
            ref = state.getTarget();
            name = state.getTargetName();
        } else {
            ref = state.getSource();
            name = state.getSourceName();
        }

        boolean hyperlink = !getEnableSelection();
        return new IMObjectReferenceViewer(ref, name, hyperlink).getComponent();
    }

    /**
     * Returns the description of the source or target entity of the
     * relationship, depending on the {@link #displayTarget} flag.
     *
     * @param state the relationship
     * @return the source or target description
     */
    protected Object getDescription(RelationshipState state) {
        return displayTarget ? state.getTargetDescription()
                : state.getSourceDescription();
    }

    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(NAME_INDEX, "table.imobject.name"));
        model.addColumn(createTableColumn(DESCRIPTION_INDEX,
                                          "table.imobject.description"));
        model.addColumn(createTableColumn(DETAIL_INDEX,
                                          "table.entityrelationship.details"));
        return model;
    }

}
