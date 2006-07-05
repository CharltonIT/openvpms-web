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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;

import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;

import java.util.List;


/**
 * IMObjectTableModel that delegates to another.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class DelegatingIMObjectTableModel
        extends AbstractTableModel
        implements IMObjectTableModel {

    /**
     * The model to delegate to.
     */
    private IMObjectTableModel _model;

    /**
     * Constructs a new <code>DelegatingIMObjectTableModel</code>.
     */
    public DelegatingIMObjectTableModel() {
    }

    /**
     * Constructs a new <code>DelegatingIMObjectTableModel</code>
     *
     * @param model the model to delegate to
     */
    public DelegatingIMObjectTableModel(IMObjectTableModel model) {
        setModel(model);
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return _model.getColumnCount();
    }

    /**
     * Returns the most-specific class of objects found in a given table
     * column.  Every object in the specified column must be an instance
     * of the returned class.
     *
     * @see TableModel#getColumnClass(int)
     */
    @Override
    public Class getColumnClass(int column) {
        return _model.getColumnClass(column);
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    @Override
    public String getColumnName(int column) {
        return _model.getColumnName(column);
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    public int getRowCount() {
        return _model.getRowCount();
    }

    /**
     * Returns the value found at the given coordinate within the table.
     * Column and row values are 0-based.
     * <strong>WARNING: Take note that the column is the first parameter
     * passed to this method, and the row is the second parameter.</strong>
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     */
    public Object getValueAt(int column, int row) {
        return _model.getValueAt(column, row);
    }

    /**
     * @see TableModel#addTableModelListener(TableModelListener)
     */
    public void addTableModelListener(TableModelListener l) {
        _model.addTableModelListener(l);
    }

    /**
     * @see TableModel#removeTableModelListener(TableModelListener)
     */
    public void removeTableModelListener(TableModelListener l) {
        _model.removeTableModelListener(l);
    }

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    public List<IMObject> getObjects() {
        return _model.getObjects();
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<IMObject> objects) {
        _model.setObjects(objects);
    }

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    public TableColumnModel getColumnModel() {
        return _model.getColumnModel();
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
        return _model.getSortConstraints(column, ascending);
    }

    /**
     * Determines if selection should be enabled.
     *
     * @return <code>true</code> if selection should be enabled; otherwise
     *         <code>false</code>
     */
    public boolean getEnableSelection() {
        return _model.getEnableSelection();
    }

    /**
     * Sets the model to delegate to.
     *
     * @param model the model to delegate to
     */
    protected void setModel(IMObjectTableModel model) {
        _model = model;
        _model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
                notifyListeners(event);
            }
        });
    }

    /**
     * Returns the model to delegate to.
     *
     * @return the model
     */
    protected IMObjectTableModel getModel() {
        return _model;
    }

    /**
     * Notify listeners of an update to the underlying table.
     *
     * @param event the event
     */
    protected void notifyListeners(TableModelEvent event) {
        if (event.getType() == TableModelEvent.STRUCTURE_CHANGED) {
            fireTableStructureChanged();
        } else {
            fireTableDataChanged();
        }
    }

}
