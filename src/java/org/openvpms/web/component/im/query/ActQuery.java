package org.openvpms.web.component.im.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import echopointng.DateField;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.search.IPage;
import org.openvpms.component.system.common.search.SortCriteria;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;


/**
 * Query component for {@link Act} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActQuery implements Query {

    /**
     * The id of the entity to search for.
     */
    private IMObjectReference _entityId;

    /**
     * The archetype entity name.
     */
    private final String _entityName;

    /**
     * The archetype conceptName.
     */
    private final String _conceptName;

    /**
     * The component representing the query.
     */
    private Component _component;

    /**
     * The status dropdown.
     */
    private SelectField _statusSelector;

    /**
     * The selected status.
     */
    private String _status;

    /**
     * The act status lookups.
     */
    private final List<Lookup> _statusLookups;

    /**
     * Status to exclude. May be <code>null</code>
     */
    private final String _excludeStatus;

    /**
     * Indicates if all start dates should be queried. If so, the startFrom and
     * startTo dates are ignored.
     */
    private CheckBox _startAll;

    /**
     * The start-from label.
     */
    private Label _startFromLabel;

    /**
     * The start-from date.
     */
    private DateField _startFrom;

    /**
     * The start-to label.
     */
    private Label _startToLabel;

    /**
     * The start-to date.
     */
    private DateField _startTo;

    /**
     * Cell spacing row style.
     */
    private static final String CELLSPACING_STYLE = "CellSpacing";

    /**
     * Row style name.
     */
    private static final String ROW_STYLE = "ControlRow";


    /**
     * Construct a new <code>ActQuery</code>.
     *
     * @param entity        the entity to search for
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param statusLookups the act status lookups
     */
    public ActQuery(Entity entity, String entityName, String conceptName,
                    List<Lookup> statusLookups) {
        this(entity, entityName, conceptName, statusLookups, null);
    }

    /**
     * Construct a new <code>ActQuery</code>.
     *
     * @param entity        the entity to search for
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param statusLookups the act status lookups
     * @param excludeStatus to exclude. May be <code>null</code>
     */
    public ActQuery(Entity entity, String entityName, String conceptName,
                    List<Lookup> statusLookups,
                    String excludeStatus) {
        setEntity(entity);
        _entityName = entityName;
        _conceptName = conceptName;
        _excludeStatus = excludeStatus;
        if (_excludeStatus != null) {
            _statusLookups = new ArrayList<Lookup>(statusLookups);
            for (ListIterator<Lookup> iterator = _statusLookups.listIterator();
                 iterator.hasNext();) {
                Lookup lookup = iterator.next();
                if (lookup.getCode().equals(_excludeStatus)) {
                    iterator.remove();
                }
            }
        } else {
            _statusLookups = statusLookups;
        }
    }

    /**
     * Sets the entity to search for.
     *
     * @param entity the entity to search for. May be <code>null</code>
     */
    public void setEntity(Entity entity) {
        _entityId = (entity != null) ? entity.getObjectReference() : null;
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (_component == null) {
            doLayout();
        }
        return _component;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwie <code>false</code>
     */
    public boolean isAuto() {
        return false;
    }

    /**
     * Add a listener for query events.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryListener listener) {
        // no-op
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeQueryListener(QueryListener listener) {
        // no-op
    }

    /**
     * Lays out the component.
     */
    protected void doLayout() {
        LookupListModel model = new LookupListModel(_statusLookups, true);
        _statusSelector = SelectFieldFactory.create(model);
        _statusSelector.setCellRenderer(new LookupListCellRenderer());
        _statusSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStatusChanged();
            }
        });

        _startAll = CheckBoxFactory.create("actquery.all", true);
        _startAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStartAllChanged();
            }
        });

        _startFromLabel = LabelFactory.create("actquery.from");
        _startFrom = DateFieldFactory.create();
        _startFrom.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onStartFromChanged();
                    }
                });

        _startToLabel = LabelFactory.create("actquery.to");
        _startTo = DateFieldFactory.create();
        _startTo.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onStartToChanged();
                    }
                });
        Row startRange = RowFactory.create(
                CELLSPACING_STYLE,
                _startFromLabel, _startFrom,
                _startToLabel, _startTo);
        Row startRow = RowFactory.create(ROW_STYLE, _startAll, startRange);

        onStartAllChanged();

        _component = RowFactory.create(CELLSPACING_STYLE,
                                       LabelFactory.create("actquery.status"),
                                       _statusSelector, startRow);
    }

    /**
     * Performs the query.
     *
     * @param rows      the maxiomum no. of rows per page
     * @param node      the node to sort on. May be <code>null</code>
     * @param ascending if <code>true</code> sort the rows inascending order;
     *                  otherwise sort them in <code>descebding</code> order
     * @return the query result set
     */
    public ResultSet query(int rows, String node, boolean ascending) {
        ResultSet<Act> result = null;
        if (_entityId != null) {
            Date startFrom = getStartFrom();
            Date startTo = getStartTo();
            SortCriteria order = null;
            if (node != null) {
                order = new SortCriteria(node, ascending);
            }
            result = new ActResultSet(_entityId, _entityName, _conceptName,
                                      startFrom, startTo, _status, rows, order);
            if (_excludeStatus != null) {
                result = filter(result, _excludeStatus);
            }
        }
        return result;
    }

    /**
     * Invoked when a status is selected.
     */
    private void onStatusChanged() {
        _status = (String) _statusSelector.getSelectedItem();
    }

    /**
     * Invoked when the start-all check box changes.
     */
    private void onStartAllChanged() {
        boolean enabled = !_startAll.isSelected();
        enable(_startFromLabel, enabled);
        enable(_startFrom, enabled);
        enable(_startToLabel, enabled);
        enable(_startTo, enabled);
    }

    /**
     * Enable/disable a component.
     *
     * @param component the component to update
     * @param enabled   if <code>true</code> enable the component; otherwise
     *                  disable it
     */
    private void enable(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (enabled) {
            component.setForeground(Color.BLACK);
        } else {
            component.setForeground(Color.LIGHTGRAY);
        }
    }

    /**
     * Enable/disable a date field.
     *
     * @param field   the field to update
     * @param enabled if <code>true</code> enable the field; otherwise disable
     *                it
     */
    private void enable(DateField field, boolean enabled) {
        enable(field.getTextField(), enabled);
        field.getDateChooser().setEnabled(enabled);
        field.setEnabled(enabled);
        if (!enabled) {
            field.setExpanded(false);
        }
    }

    /**
     * Sets the start-from date.
     *
     * @param date the start-from date
     */
    private void setStartFrom(Date date) {
        setDate(_startFrom, date);
    }


    /**
     * Returns the start-from date.
     *
     * @return the start-from date
     */
    private Date getStartFrom() {
        return _startAll.isSelected() ? null : getDate(_startFrom);
    }

    /**
     * Sets the start-to date.
     *
     * @param date the start-to date
     */
    private void setStartTo(Date date) {
        setDate(_startTo, date);
    }

    /**
     * Returns the start-to date.
     *
     * @return the start-to date
     */
    private Date getStartTo() {
        return _startAll.isSelected() ? null : getDate(_startTo);
    }

    /**
     * Invoked when the start-from date changes. Sets the start-to date =
     * start-from if start-from is greater.
     */
    private void onStartFromChanged() {
        Date from = getStartFrom();
        Date to = getStartTo();
        if (from != null && from.compareTo(to) > 0) {
            setStartTo(from);
        }
    }

    /**
     * Invoked when the start-to date changes. Sets the start-from date =
     * start-to if start-from is greater.
     */
    private void onStartToChanged() {
        Date from = getStartFrom();
        Date to = getStartTo();
        if (from != null && from.compareTo(to) > 0) {
            setStartFrom(to);
        }
    }

    /**
     * Returns the date of the given field.
     *
     * @param field the date field
     * @return the selected date from <code>field</code>
     */
    private Date getDate(DateField field) {
        return field.getDateChooser().getSelectedDate().getTime();
    }

    /**
     * Sets the date of the given field.
     *
     * @param field the date field
     * @param date  the date to set
     */
    private void setDate(DateField field, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        field.getDateChooser().setSelectedDate(calendar);
    }

    /**
     * Filters a result set to remove acts with a particular status.
     *
     * @param set    the result set to filter
     * @param status the status to exclude
     * @return the filtered result set
     * @todo this is a workaround for OVPMS-254
     */
    private ResultSet<Act> filter(ResultSet<Act> set, String status) {
        List<Act> acts = new ArrayList<Act>(set.getRows());
        while (set.hasNext()) {
            IPage<Act> page = set.next();
            for (Act act : page.getRows()) {
                if (!status.equals(act.getStatus())) {
                    acts.add(act);
                }
            }
        }
        return new PreloadedResultSet<Act>(acts, set.getRowsPerPage());
    }

}


