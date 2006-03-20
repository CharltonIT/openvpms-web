package org.openvpms.web.component.im.table;

import java.util.List;

import nextapp.echo2.app.Table;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.table.EvenOddTableCellRenderer;
import org.openvpms.web.component.table.SortableTableHeaderRenderer;


/**
 * Paged, sortable table of {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectTable extends Table {

    /**
     * Construct a new <code>IMObjectTable</code>.
     */
    public IMObjectTable() {
        this(new IMObjectTableModel());
    }

    /**
     * Construct a new <code>IMObjectTable</code>.
     *
     * @param model the table model
     */
    public IMObjectTable(IMObjectTableModel model) {
        setStyleName("default");
        setModel(model);
        setColumnModel(model.getColumnModel());
        setSelectionEnabled(true);
        setDefaultRenderer(Object.class, new EvenOddTableCellRenderer());
        setDefaultHeaderRenderer(new SortableTableHeaderRenderer());
    }

    /**
     * Sets the objects to display in the table.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<IMObject> objects) {
        ((IMObjectTableModel) getModel()).setObjects(objects);
    }

    /**
     * Returns the objects displayed in the table.
     *
     * @return the object being displayed.
     */
    public List<IMObject> getObjects() {
        return ((IMObjectTableModel) getModel()).getObjects();
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if no object is
     *         selected
     */
    public IMObject getSelected() {
        IMObject result = null;
        int index = getSelectionModel().getMinSelectedIndex();
        if (index != -1) {
            IMObjectTableModel model = (IMObjectTableModel) getModel();
            int row = model.getAbsRow(index);
            if (row < model.getObjects().size()) {
                result = model.getObject(row);
            }
        }
        return result;
    }

    /**
     * Sets the selected object.
     *
     * @param object the object to select
     */
    public void setSelected(IMObject object) {
        IMObjectTableModel model = (IMObjectTableModel) getModel();
        int index = getObjects().indexOf(object);
        int minRow = model.getPage() * model.getRowsPerPage();
        int maxRow = minRow + model.getRowsPerPage();
        if (index >= minRow && index < maxRow) {
            int offset = index - minRow;
            getSelectionModel().setSelectedIndex(offset, true);
        }
    }

    /**
     * Adds an object to the table.
     *
     * @param object the object to add
     */
    public void add(IMObject object) {
        ((IMObjectTableModel) getModel()).add(object);
    }

    /**
     * Removes an object from the table.
     *
     * @param object the object to remove
     */
    public void remove(IMObject object) {
        ((IMObjectTableModel) getModel()).remove(object);
    }

    /**
     * Returns the no. of rows per page.
     *
     * @return the no. of rows per page
     */
    public int getRowsPerPage() {
        return ((IMObjectTableModel) getModel()).getRowsPerPage();
    }

}
