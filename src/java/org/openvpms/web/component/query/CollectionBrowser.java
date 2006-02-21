package org.openvpms.web.component.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.TableNavigator;


/**
 * Browser for a collection of {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class CollectionBrowser extends Column {

    /**
     * The object to browse.
     */
    private final IMObject _object;

    /**
     * Collection to browse.
     */
    private IMObjectTable _table;

    /**
     * The node descriptor.
     */
    private final NodeDescriptor _descriptor;


    /**
     * Construct a new <code>CollectionEditor</code>.
     *
     * @param descriptor the node descriptor
     */
    public CollectionBrowser(IMObject object, NodeDescriptor descriptor) {
        _object = object;
        _descriptor = descriptor;
        doLayout();
    }

    /**
     * Lays out the component.
     */
    protected void doLayout() {
        _table = new IMObjectTable();
        _table.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onBrowse();
            }
        });
        add(_table);

        populate();
    }

    /**
     * Populates the table.
     */
    protected void populate() {
        Collection values = (Collection) _descriptor.getValue(_object);
        int size = values.size();
        if (size != 0) {
            List<IMObject> objects = new ArrayList<IMObject>();
            for (Object value : values) {
                objects.add((IMObject) value);
            }
            _table.setObjects(objects);

            int rowsPerPage = _table.getRowsPerPage();
            if (size > rowsPerPage) {
                // display the navigator before the table
                TableNavigator _navigator = new TableNavigator(_table);
                add(_navigator);
            }
            add(_table);
        } else {
            _table.setObjects(new ArrayList<IMObject>());
        }
    }

    /**
     * Browses the selected object.
     */
    protected void onBrowse() {
        IMObject object = _table.getSelected();
        if (object != null) {
            IMObjectBrowser browser
                    = new IMObjectBrowser(object);
            IMObjectBrowserDialog dialog = new IMObjectBrowserDialog(browser);
            dialog.show();
        }
    }

}
