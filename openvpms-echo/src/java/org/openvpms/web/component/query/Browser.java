package org.openvpms.web.component.query;

import java.util.EventListener;
import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.IMObjectTable;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.SelectFieldFactory;
import org.openvpms.web.component.SplitPaneFactory;
import org.openvpms.web.component.TableNavigator;
import org.openvpms.web.component.TextComponentFactory;
import org.openvpms.web.component.model.ArchetypeShortNameListModel;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Browser of IMObject instances. In the left pane, a table displays IMObjects
 * matching the specified criteria. When an object is selected from the table, a
 * summary of it is displayed in the right pane.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class Browser extends SplitPane {

    /**
     * The selected action command.
     */
    public static final String SELECTED = "selected";

    /**
     * Archetype short names to match on.
     */
    private final String[] _shortNames;

    /**
     * The instance name. If the text is <code>null</code> or empty, indicates
     * to query all instances.
     */
    private TextField _instanceName;

    /**
     * The deactived check box. If selected, deactived instances will be
     * returned along with the active ones.
     */
    private CheckBox _deactived;

    /**
     * The selected archetype short name. If <code>null</code>, or {@link
     * ArchetypeShortNameListModel#ALL}, indicates to query using all matching
     * short names.
     */
    private String _shortName;

    /**
     * The table to display results.
     */
    private IMObjectTable _table;

    /**
     * The selected object.
     */
    private IMObject _selected;

    /**
     * Split pane for laying out the table and navigation control.
     */
    private SplitPane _layout;

    /**
     * Style name for this.
     */
    private static final String STYLE = "Browser";

    /**
     * Query button id.
     */
    private static final String QUERY_ID = "query";

    /**
     * Type label id.
     */
    private static final String TYPE_ID = "type";

    /**
     * Name label id.
     */
    private static final String NAME_ID = "name";

    /**
     * Deactivated label id.
     */
    private static final String DEACTIVATED_ID = "deactivated";

    /**
     * Button row style name.
     */
    private static final String ROW_STYLE = "ControlRow";

    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified short names.
     */
    public Browser(String[] shortNames) {
        super(ORIENTATION_VERTICAL);
        _shortNames = shortNames;
        doLayout();
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public Browser(String refModelName, String entityName,
                   String conceptName) {
        this(getShortNames(refModelName, entityName, conceptName));
    }

    /**
     * Sets the archetype instance name to query.
     *
     * @param name the archetype instance name. If <code>null</code> indicates
     *             to query all instances
     */
    public void setInstanceName(String name) {
        _instanceName.setText(name);
    }

    /**
     * Returns the archetype instance name, including wildcards.
     *
     * @return the archetype instance name. Nay be <code>null</code>
     */
    public String getInstanceName() {
        final String wildcard = "*";
        String name = _instanceName.getText();
        if (!StringUtils.isEmpty(name)) {
            boolean start = name.startsWith(wildcard);
            boolean end = name.endsWith(wildcard);
            if (!start) {
                name = wildcard + name;
            }
            if (!end) {
                name = name + wildcard;
            }
        }
        return name;
    }

    /**
     * Determines if deactived instances should be returned.
     *
     * @return <code>true</code> if deactived instances should be retured;
     *         <code>false</code>
     */
    public boolean includeDeactived() {
        return _deactived.isSelected();
    }

    /**
     * Returns the selected archetype short name.
     *
     * @return the archetype short name. May be <code>null</code>
     */
    public String getShortName() {
        return _shortName;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    public IMObject getSelected() {
        return _selected;
    }

    /**
     * Adds an <code>ActionListener</code> to receive notification of selection
     * actions.
     *
     * @param listener the listener to add
     */
    public void addActionListener(ActionListener listener) {
        getEventListenerList().addListener(ActionListener.class, listener);
    }

    /**
     * Set the archetype short name.
     *
     * @param name the archetype short name. If <code>null</code>, indicates to
     *             query using all matching short names.
     */
    protected void setShortName(String name) {
        _shortName = name;
    }

    /**
     * Layout this component.
     */
    protected void doLayout() {
        setStyleName(STYLE);

        Row row = RowFactory.create(ROW_STYLE);

        // set up the short names select field, iff there is more than
        // one matching short name.
        if (_shortNames.length > 1) {
            final ArchetypeShortNameListModel model
                    = new ArchetypeShortNameListModel(_shortNames, true);
            final SelectField shortNameSelector = SelectFieldFactory.create(model);
            shortNameSelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    int index = shortNameSelector.getSelectedIndex();
                    String shortName = model.getShortName(index);
                    setShortName(shortName);
                }
            });

            Label typeLabel = LabelFactory.create(TYPE_ID);
            row.add(typeLabel);
            row.add(shortNameSelector);
        }

        // instance name text field
        _instanceName = TextComponentFactory.create();
        Label nameLabel = LabelFactory.create(NAME_ID);

        _deactived = new CheckBox();
        _deactived.setSelected(false);
        Label deactivedLabel = LabelFactory.create(DEACTIVATED_ID);

        // query button
        Button query = ButtonFactory.create(QUERY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onQuery();
            }
        });

        row.add(nameLabel);
        row.add(_instanceName);
        row.add(deactivedLabel);
        row.add(_deactived);
        row.add(query);

        _table = new IMObjectTable();
        _table.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSelect();
            }
        });

        add(row);

        TableNavigator navigator = new TableNavigator(_table);
        _layout = SplitPaneFactory.create(ORIENTATION_VERTICAL,
                navigator, _table);
        _layout.setSeparatorPosition(new Extent(0, Extent.PX));
        add(_layout);

        onQuery();
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    protected void onQuery() {
        String type = getShortName();
        String name = getInstanceName();
        boolean activeOnly = !includeDeactived();

        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<IMObject> result = null;
        if (type == null || type.equals(ArchetypeShortNameListModel.ALL)) {
            for (String shortName : _shortNames) {
                List<IMObject> matches
                        = get(shortName, name, activeOnly, service);
                if (result == null) {
                    result = matches;
                } else {
                    result.addAll(matches);
                }
            }
        } else {
            result = get(type, name, activeOnly, service);
        }

        if (result != null) {
            _table.setObjects(result);
        }
        if (result != null && result.size() <= _table.getRowsPerPage()) {
            _layout.setSeparatorPosition(new Extent(0, Extent.PX));
        } else {
            _layout.setSeparatorPosition(new Extent(32, Extent.PX));
        }
    }

    /**
     * Notifies all listeners that have registered for this event type.
     *
     * @param event the <code>ActionEvent</code> to send
     */
    protected void fireActionPerformed(ActionEvent event) {
        EventListener[] listeners = getEventListenerList().getListeners(
                ActionListener.class);
        for (int index = 0; index < listeners.length; ++index) {
            ((ActionListener) listeners[index]).actionPerformed(event);
        }
    }

    /**
     * Returns all matching objects for the specified criteria
     *
     * @param shortName    the archetype shortname to match on
     * @param instanceName the object instance name
     * @param activeOnly   if <code>true</code>, only include active objects
     */
    private List<IMObject> get(String shortName, String instanceName,
                               boolean activeOnly, IArchetypeService service) {
        ArchetypeDescriptor descriptor
                = service.getArchetypeDescriptor(shortName);
        ArchetypeId type = descriptor.getType();
        return service.get(type.getRmName(), type.getEntityName(),
                type.getConcept(), instanceName, true, activeOnly);
    }

    /**
     * Updates the selected IMObject from the table, and notifies any
     * listeners.
     */
    private void onSelect() {
        _selected = _table.getSelected();
        fireActionPerformed(new ActionEvent(this, SELECTED));
    }

    /**
     * Helper to return short names matching certain criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @return a list of short names matching the criteria
     */
    private static String[] getShortNames(String refModelName, String entityName, String conceptName) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<String> names = service.getArchetypeShortNames(refModelName,
                entityName, conceptName, true);
        return names.toArray(new String[0]);
    }


}
