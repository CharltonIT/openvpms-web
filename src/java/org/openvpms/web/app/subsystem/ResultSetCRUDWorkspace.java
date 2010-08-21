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
package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.DoubleClickMonitor;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * A CRUD workspace that provides a {@link org.openvpms.web.component.im.query.QueryBrowser} to display objects, and a {@link ResultSetCRUDWindow}
 * to view/edit the objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ResultSetCRUDWorkspace<T extends IMObject> extends BrowserCRUDWorkspace<T, T> {

    private DoubleClickMonitor click = new DoubleClickMonitor();


    /**
     * Constructs a <tt>ResultSetCRUDWorkspace</tt>.
     * <p/>
     * The {@link #setArchetypes} method must be invoked to set archetypes that the workspace supports, before
     * performing any operations.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     */
    public ResultSetCRUDWorkspace(String subsystemId, String workspaceId) {
        super(subsystemId, workspaceId, false);
    }

    /**
     * Creates the workspace component.
     *
     * @return a new workspace
     */
    @Override
    protected Component createWorkspace() {
        Component window = getCRUDWindow().getComponent();
        Component browser = ColumnFactory.create("Inset", getBrowser().getComponent());
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, "SplitPaneWithButtonRow",
                                       window, browser);
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<T> createCRUDWindow() {
        QueryBrowser<T> browser = getBrowser();
        return new ResultSetCRUDWindow<T>(getArchetypes(), browser.getResultSet());
    }

    /**
     * Returns the CRUD window, creating it if it doesn't exist.
     *
     * @return the CRUD window
     */
    @Override
    protected ResultSetCRUDWindow<T> getCRUDWindow() {
        return (ResultSetCRUDWindow<T>) super.getCRUDWindow();
    }

    /**
     * Returns the browser.
     *
     * @return the browser, or <tt>null</tt> if none has been registered
     */
    @Override
    protected QueryBrowser<T> getBrowser() {
        return (QueryBrowser<T>) super.getBrowser();
    }

    /**
     * Invoked when a browser object is selected.
     * <p/>
     * This implementation sets the object in the CRUD window and pops up a viewer if it has been double clicked.
     *
     * @param object the selected object
     */
    @Override
    protected void onBrowserSelected(T object) {
        updateResultSet();
        super.onBrowserSelected(object);
        if (click.isDoubleClick(object.getId())) {
            getCRUDWindow().view();
        }
    }

    /**
     * Invoked when a browser object is viewed (aka 'browsed').
     * <p/>
     * This implementation sets the object in the CRUD window.
     *
     * @param object the selected object
     */
    @Override
    protected void onBrowserViewed(T object) {
        updateResultSet();
        super.onBrowserViewed(object);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(T object, boolean isNew) {
        super.onSaved(object, isNew);
        updateResultSet();
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(T object) {
        super.onDeleted(object);
        updateResultSet();
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    @Override
    protected void onRefresh(T object) {
        super.onRefresh(object);
        updateResultSet();
    }

    /**
     * Invoked when the browser is queried.
     */
    @Override
    protected void onBrowserQuery() {
        super.onBrowserQuery();
        updateResultSet();
    }

    /**
     * Determines if the parent object is optional (i.e may be <tt>null</tt>,
     * when laying out the workspace.
     * <p/>
     * This implementation always returns <tt>true</tt>.
     *
     * @return <tt>true</tt>
     */
    @Override
    protected boolean isParentOptional() {
        return true;
    }

    /**
     * Sets the archetypes that this operates on.
     *
     * @param archetypes the archetypes
     */
    @Override
    protected void setArchetypes(Archetypes<T> archetypes) {
        super.setArchetypes(archetypes);
        setChildArchetypes(archetypes);
    }

    /**
     * Updates the CRUD window with the current result set.
     */
    private void updateResultSet() {
        QueryBrowser<T> browser = getBrowser();
        getCRUDWindow().setResultSet(browser.getResultSet());
    }

}
