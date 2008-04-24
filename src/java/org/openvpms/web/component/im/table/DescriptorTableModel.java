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

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.TableComponentFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Table model created from an {@link ArchetypeDescriptor} or {@link
 * NodeDescriptor}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class DescriptorTableModel<T extends IMObject>
        extends BaseIMObjectTableModel<T> {

    /**
     * The layout context.
     */
    private final LayoutContext context;


    /**
     * Creates a new <code>DescriptorTableModel</code>.
     * The column model must be set using {@link #setTableColumnModel}.
     */
    public DescriptorTableModel() {
        this((LayoutContext) null);
    }


    /**
     * Creates a new <code>DescriptorTableModel</code>.
     * The column model must be set using {@link #setTableColumnModel}.
     *
     * @param context the layout context. May be <tt>null</tt>
     */
    public DescriptorTableModel(LayoutContext context) {
        if (context == null) {
            context = new DefaultLayoutContext();
            TableComponentFactory factory = new TableComponentFactory(context);
            context.setComponentFactory(factory);
        }
        this.context = context;
    }

    /**
     * Creates a new <code>DescriptorTableModel</code>.
     *
     * @param shortNames the archetype short names
     */
    public DescriptorTableModel(String[] shortNames) {
        this(shortNames, null);
    }

    /**
     * Creates a new <code>DescriptorTableModel</code>.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context. May be <tt>null</tt>
     */
    public DescriptorTableModel(String[] shortNames, LayoutContext context) {
        if (context == null) {
            context = new DefaultLayoutContext();
            TableComponentFactory factory = new TableComponentFactory(context);
            context.setComponentFactory(factory);
        }
        this.context = context;
        setTableColumnModel(createColumnModel(shortNames, context));
    }

    /**
     * @see TableModel#getColumnName
     */
    @Override
    public String getColumnName(int column) {
        String result;
        TableColumn col = getColumn(column);
        if (col instanceof DescriptorTableColumn) {
            NodeDescriptor descriptor
                    = ((DescriptorTableColumn) col).getDescriptor();
            result = descriptor.getDisplayName();
        } else {
            result = super.getColumnName(column);
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
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] result;
        TableColumn col = getColumn(column);
        if (col instanceof DescriptorTableColumn) {
            DescriptorTableColumn descCol = (DescriptorTableColumn) col;
            if (descCol.isSortable()) {
                SortConstraint sort = descCol.createSortConstraint(ascending);
                result = new SortConstraint[]{sort};
            } else {
                result = null;
            }
        } else {
            result = super.getSortConstraints(column, ascending);
        }
        return result;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(T object, TableColumn column, int row) {
        Object result;
        if (column instanceof DescriptorTableColumn) {
            result = getValue(object, (DescriptorTableColumn) column);
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns a value for a given column.
     *
     * @param object the object to operate on
     * @param column the column
     * @return the value for the column
     */
    protected Object getValue(T object, DescriptorTableColumn column) {
        return column.getValue(object, context);
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return context;
    }

    /**
     * Returns the component factory.
     *
     * @return the component factory
     */
    protected IMObjectComponentFactory getFactory() {
        return context.getComponentFactory();
    }

    /**
     * Creates a column model for a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new column model
     */
    protected TableColumnModel createColumnModel(String[] shortNames,
                                                 LayoutContext context) {
        List<ArchetypeDescriptor> archetypes
                = DescriptorHelper.getArchetypeDescriptors(shortNames);
        if (archetypes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Argument 'shortNames' doesn't refer to a valid archetype");
        }
        return createColumnModel(archetypes, context);
    }

    /**
     * Creates a column model for one or more archetypes.
     * If there are multiple archetypes, the intersection of the descriptors
     * will be used.
     *
     * @param archetypes the archetypes
     * @param context    the layout context
     * @return a new column model
     */
    protected TableColumnModel createColumnModel(
            List<ArchetypeDescriptor> archetypes,
            LayoutContext context) {
        List<String> names = getDescriptorNames(archetypes, context);
        TableColumnModel columns = new DefaultTableColumnModel();

        if (showArchetypeColumn(archetypes)) {
            addColumns(archetypes, names, columns);
            int index = getArchetypeColumnIndex();
            TableColumn column = createTableColumn(
                    ARCHETYPE_INDEX, "table.imobject.archetype");
            columns.addColumn(column);
            columns.moveColumn(columns.getColumnCount() - 1, index);
        } else {
            addColumns(archetypes, names, columns);
        }
        return columns;
    }

    /**
     * Add columns to a column model.
     *
     * @param archetypes the archetypes
     * @param names      the node descriptor names
     * @param columns    the columns to add to
     */
    protected void addColumns(List<ArchetypeDescriptor> archetypes,
                              List<String> names, TableColumnModel columns) {
        // determine a unique starting index for the columns
        int index = getNextModelIndex(columns);

        for (String name : names) {
            addColumn(archetypes, name, index, columns);
            ++index;
        }
    }

    /**
     * Adds a column for a node.
     *
     * @param archetype the archetypes
     * @param name      the node name
     * @param columns   the columns to add to
     * @return the new column, or <tt>null</tt> if the node is not found in
     *         the archetypes
     */
    protected TableColumn addColumn(ArchetypeDescriptor archetype,
                                    String name, TableColumnModel columns) {
        return addColumn(Arrays.asList(archetype), name,
                         getNextModelIndex(columns), columns);
    }

    /**
     * Adds a column for a node.
     *
     * @param archetypes the archetypes
     * @param name       the node name
     * @param index      the index to assign the column
     * @param columns    the columns to add to
     * @return the new column, or <tt>null</tt> if the node is not found in
     *         the archetypes
     */
    protected TableColumn addColumn(List<ArchetypeDescriptor> archetypes,
                                    String name, int index,
                                    TableColumnModel columns) {
        TableColumn column = createColumn(archetypes, name, index);
        if (column != null) {
            columns.addColumn(column);
        }
        return column;
    }

    /**
     * Creates a new column for a node.
     *
     * @param archetypes the archetypes
     * @param name       the node name
     * @param index      the index to assign the column
     * @return a new column, or <tt>null</tt> if the node is not found in
     *         the archetypes
     */
    protected TableColumn createColumn(List<ArchetypeDescriptor> archetypes,
                                       String name, int index) {
        Map<String, NodeDescriptor> descriptors
                = new HashMap<String, NodeDescriptor>();
        for (ArchetypeDescriptor archetype : archetypes) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(name);
            if (descriptor != null) {
                descriptors.put(archetype.getShortName(), descriptor);
            }
        }
        return descriptors.isEmpty()
                ? null : new DescriptorTableColumn(index, descriptors);
    }


    /**
     * Returns the intersection of descriptor names for a set of archetypes.
     *
     * @param archetypes the archetype descriptors
     * @param context    the layout context
     * @return the intersection of descriptors names for a set of archetypes
     */
    protected List<String> getDescriptorNames(
            List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        List<String> common = null;
        for (ArchetypeDescriptor archetype : archetypes) {
            List<String> nodes = getDescriptorNames(archetype, context);
            if (common == null) {
                common = nodes;
            } else {
                common = getIntersection(common, nodes);
            }
        }
        return common;
    }

    /**
     * Returns a list of descriptor names to include in the table.
     * This implementation returns <code>null</code> to indicate that the
     * intersection should be calculated from all descriptors.
     *
     * @return the list of descriptor names to include in the table
     */
    protected String[] getDescriptorNames() {
        return null;
    }

    /**
     * Returns a filtered list of descriptor names for an archetype.
     *
     * @param archetype the archetype
     * @param context   the layout context
     * @return a filtered list of descriptor names for the archetype
     */
    protected List<String> getDescriptorNames(ArchetypeDescriptor archetype,
                                              LayoutContext context) {
        List<String> result = new ArrayList<String>();
        String[] names = getDescriptorNames();
        if (names != null && names.length != 0) {
            for (String name : names) {
                NodeDescriptor descriptor = archetype.getNodeDescriptor(name);
                if (descriptor != null) {
                    result.add(descriptor.getName());
                }
            }
        } else {
            List<NodeDescriptor> descriptors
                    = filter(archetype.getSimpleNodeDescriptors(), context);
            for (NodeDescriptor descriptor : descriptors) {
                result.add(descriptor.getName());
            }
        }
        return result;
    }

    /**
     * Determines if the archetype column should be displayed.
     * <p/>
     * This implementation returns true if there is more than one archetype.
     *
     * @param archetypes the archetypes
     * @return <tt>true</tt> if the archetype column should be displayed
     */
    protected boolean showArchetypeColumn(
            List<ArchetypeDescriptor> archetypes) {
        return archetypes.size() > 1;
    }

    /**
     * Returns the index to insert the archetype column.
     *
     * @return the index to insert the archetype column
     */
    protected int getArchetypeColumnIndex() {
        return 0;
    }

    /**
     * Filters descriptors using the context's default node filter.
     *
     * @param descriptors the column descriptors
     * @param context     the layout context
     * @return the filtered descriptors
     */
    protected List<NodeDescriptor> filter(List<NodeDescriptor> descriptors,
                                          LayoutContext context) {
        return FilterHelper.filter(null, context.getDefaultNodeFilter(),
                                   descriptors);
    }

    /**
     * Helper to return the intersection of two lists of strings, maintaining
     * insertion order.
     *
     * @param first  the first list
     * @param second the second list
     * @return the intersection of the two lists
     */
    private List<String> getIntersection(List<String> first,
                                         List<String> second) {
        List<String> result = new ArrayList<String>();
        for (String a : first) {
            for (String b : second) {
                if (a.equals(b)) {
                    result.add(a);
                    break;
                }
            }
        }
        return result;
    }
}
