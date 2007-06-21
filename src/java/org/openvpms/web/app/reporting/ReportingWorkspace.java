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

package org.openvpms.web.app.reporting;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ParameterType;
import org.openvpms.report.Report;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.dialog.PrintDialog;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.IMObjectTableBrowserFactory;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.subsystem.AbstractWorkspace;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.servlet.DownloadServlet;
import org.openvpms.web.system.ServiceHelper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Reporting workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportingWorkspace extends AbstractWorkspace<Entity> {

    /**
     * The current user. May be <tt>null</tt>.
     */
    private User user;

    /**
     * The selected report. May be <tt>null</tt>.
     */
    private Entity object;

    /**
     * The workspace component.
     */
    private SplitPane workspace;

    /**
     * The entity browser.
     */
    private Browser<Entity> browser;

    /**
     * The action button row.
     */
    private Row buttons;

    /**
     * The run button.
     */
    private Button run;

    /**
     * Run button identifier.
     */
    private static final String RUN_ID = "run";

    /**
     * Button row style.
     */
    private static final String ROW_STYLE = "ControlRow";

    /**
     * Construct a new <tt>MessagingWorkspace</tt>.
     */
    public ReportingWorkspace() {
        super("reporting", "reports");
        user = GlobalContext.getInstance().getUser();
    }

    /**
     * Determines if the workspace supports an archetype.
     *
     * @param shortName the archetype's short name
     * @return <tt>true</tt> if the workspace can handle the archetype;
     *         otherwise <tt>false</tt>
     */
    public boolean canHandle(String shortName) {
        // don't want this workspace participating in context changes, so
        // return false
        return false;
    }

    /**
     * Sets the object to be viewed/edited by the workspace.
     *
     * @param object the object. May be <tt>null</tt>
     */
    public void setObject(Entity object) {
        this.object = object;
        if (object != null) {
            enableButtons(true);
        } else {
            enableButtons(false);
        }
    }

    /**
     * Returns the object to to be run by the workspace.
     *
     * @return the the object. May be <oode>null</tt>
     */
    public Entity getObject() {
        return object;
    }

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <tt>null</tt>
     */
    public void setIMObject(IMObject object) {
        if (object == null || object instanceof Entity) {
            setObject((Entity) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + Entity.class.getName());
        }
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        Component root = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL,
                "MessagingWorkspace.MainLayout");
        Component heading = super.doLayout();
        root.add(heading);
        if (user != null) {
            layoutWorkspace(user, root);
        }
        return root;
    }

    /**
     * Determines if the workspace should be refreshed. This implementation
     * returns true if the current user has changed.
     *
     * @return <tt>true</tt> if the workspace should be refreshed, otherwise
     *         <tt>false</tt>
     */
    @Override
    protected boolean refreshWorkspace() {
        User user = GlobalContext.getInstance().getUser();
        user = IMObjectHelper.reload(user);
        return IMObjectHelper.isSame(this.user, user);
    }

    /**
     * Lays out the workspace.
     *
     * @param user      the user
     * @param container the container
     */
    protected void layoutWorkspace(User user, Component container) {
        ReportQuery query = createQuery(user);
        browser = createBrowser(query);
        browser.addQueryListener(new QueryBrowserListener<Entity>() {
            public void query() {
                selectFirst();
            }

            public void selected(Entity object) {
                setObject(object);
            }
        });

        buttons = RowFactory.create(ROW_STYLE);
        layoutButtons(buttons);
        enableButtons(false);
        if (workspace != null) {
            container.remove(workspace);
        }
        workspace = createWorkspace(browser);
        container.add(workspace);
        if (!query.isAuto()) {
            browser.query();
        }
    }

    /**
     * Creates the workspace.
     *
     * @param browser the entity browser
     * @return a new split pane representing the workspace
     */
    private SplitPane createWorkspace(Browser<Entity> browser) {
        Column entities = ColumnFactory.create("Inset", browser.getComponent());
        return SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "ReportingWorkspace.Layout", getButtons(), entities);
    }

    /**
     * Creates the Entity browser.
     *
     * @param query the entity query
     * @return a new act browser
     */
    private TableBrowser<Entity> createBrowser(ReportQuery query) {
        return IMObjectTableBrowserFactory.create(query);
    }

    /**
     * Creates a new query.
     *
     * @param user the user to query
     * @return a new query
     */
    private ReportQuery createQuery(User user) {
        return new ReportQuery(user);
    }

    /**
     * Selects the first available report.
     */
    private void selectFirst() {
        List<Entity> objects = browser.getObjects();
        if (!objects.isEmpty()) {
            Entity current = objects.get(0);
            browser.setSelected(current);
            setObject(current);
        } else {
            setObject(null);
        }
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    protected void layoutButtons(Row buttons) {
        buttons.add(getRunButton());
    }

    /**
     * Returns the run button.
     *
     * @return the run button
     */
    protected Button getRunButton() {
        if (run == null) {
            run = ButtonFactory.create(RUN_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onRun();
                }
            });
        }
        return run;
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    protected void enableButtons(boolean enable) {
        if (enable) {
            if (buttons.indexOf(run) == -1) {
                buttons.add(run);
            }
        } else {
            buttons.remove(run);
        }
    }

    /**
     * Returns the button row.
     *
     * @return the button row
     */
    protected Row getButtons() {
        return buttons;
    }

    /**
     * Invoked when the run button is pressed. Runs the
     * selected report.
     */
    protected void onRun() {
        try {
            TemplateHelper helper = new TemplateHelper();
            Document doc = helper.getDocumentFromTemplate(getObject());
            if (doc != null) {
                final Report report = ReportFactory.createReport(
                        doc, ArchetypeServiceHelper.getArchetypeService(),
                        ServiceHelper.getDocumentHandlers());
                ParameterType connectionParam = getConnectionParameter(report);
                if (connectionParam == null) {
                    ErrorHelper.show(Messages.get("reporting.noconnection"));
                } else {
                    doReport(report, connectionParam);
                }
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    private void doReport(final Report report, ParameterType connectionParam)
            throws SQLException {
        final Map<String, Object> params
                = new HashMap<String, Object>();
        Set<ParameterType> types = report.getParameterTypes();
        DataSource ds = ServiceHelper.getDataSource();
        final Connection connection = ds.getConnection();
        params.put(connectionParam.getName(), connection);
        List<Property> properties = getProperties(types);
        final ReportDialog dialog = new ReportDialog(properties) {
            @Override
            protected void doPreview() {
                params.putAll(this.getValues());
                doPrintPreview(report, params, connection);
            }
        };
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                String action = dialog.getAction();
                if (PrintDialog.OK_ID.equals(action)) {
                    params.putAll(dialog.getValues());
                    doPrintPreview(report, params, connection);
                }
            }
        });
        dialog.show();

    }

    /**
     * Returns the user-configurable report properties.
     *
     * @param types the report parameter types
     * @return the corresponding list of user-configurable properties
     */
    private List<Property> getProperties(Set<ParameterType> types) {
        List<Property> result = new ArrayList<Property>();
        for (ParameterType type : types) {
            if (!type.isSystem()) {
                Property property = new SimpleProperty(type.getName(),
                                                       type.getType());
                if (property.isBoolean() || property.isString()
                        || property.isNumeric() || property.isDate()) {
                    result.add(property);
                }
            }
        }
        return result;
    }

    private ParameterType getConnectionParameter(Report report) {
        for (ParameterType type : report.getParameterTypes()) {
            if (Connection.class.equals(type.getType())) {
                return type;
            }
        }
        return null;
    }

    private void doPrintPreview(Report report, Map<String, Object> params,
                                Connection connection) {
        try {
            Document d = report.generate(params,
                                         new String[]{DocFormats.PDF_TYPE});
            DownloadServlet.startDownload(d);
            connection.close();
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

}
