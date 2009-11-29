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

package org.openvpms.web.app.workflow.scheduling;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.app.patient.CustomerPatientSummary;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Scheduling workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class SchedulingWorkspace
        extends AbstractViewWorkspace<Entity> {

    /**
     * The workspace.
     */
    private Component workspace;

    /**
     * The schedule event browser.
     */
    private ScheduleBrowser browser;

    /**
     * The CRUD window.
     */
    private ScheduleCRUDWindow window;

    /**
     * The current practice location.
     */
    private Party location;

    /**
     * Listener for practice location changes whilst the workspace is visible.
     */
    private ContextListener locationListener;


    /**
     * Creates a new <tt>SchedulingWorkspace</tt>.
     * <p/>
     * If no archetypes are supplied, the {@link #setArchetypes} method must
     * before performing any operations.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     * @param archetypes  the archetype that this operates on.
     *                    May be <tt>null</tt>
     */
    public SchedulingWorkspace(String subsystemId, String workspaceId,
                               Archetypes<Entity> archetypes) {
        super(subsystemId, workspaceId, archetypes, false);
        locationListener = new ContextListener() {
            public void changed(String key, IMObject value) {
                if (Context.LOCATION_SHORTNAME.equals(key)) {
                    locationChanged((Party) value);
                }
            }
        };
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Entity object) {
        location = GlobalContext.getInstance().getLocation();
        super.setObject(object);
        layoutWorkspace(object);
        initQuery(object);
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <code>null</code> if there is no summary
     */
    @Override
    public Component getSummary() {
        if (window != null) {
            Act act = window.getObject();
            return CustomerPatientSummary.getSummary(act);
        }
        return null;
    }

    /**
     * Invoked when the workspace is displayed.
     */
    @Override
    public void show() {
        // listen for context change events
        GlobalContext.getInstance().addListener(locationListener);
    }

    /**
     * Invoked when the workspace is hidden.
     */
    @Override
    public void hide() {
        GlobalContext.getInstance().removeListener(locationListener);
    }

    /**
     * Creates a new browser.
     *
     * @return a new browser
     */
    protected abstract ScheduleBrowser createBrowser();

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    protected abstract ScheduleCRUDWindow createCRUDWindow();

    /**
     * Returns the default schedule view for the specified practice location.
     *
     * @param location the practice location
     * @return the default schedule view, or <tt>null</tt> if there is no
     *         default
     */
    protected abstract Entity getDefaultView(Party location);

    /**
     * Determines if the workspace should be refreshed.
     * This implementation always returns <code>true</code>.
     *
     * @return <code>true</code>
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(IMObject object, boolean isNew) {
        browser.query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
        browser.query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(IMObject object) {
        browser.query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when an event is selected.
     *
     * @param event the event. May be <tt>null</tt>
     */
    protected void eventSelected(PropertySet event) {
        Act act = browser.getAct(event);
        window.setObject(act);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked to edit an event.
     *
     * @param event the event
     */
    protected void onEdit(PropertySet event) {
        Act act = browser.getAct(event);
        window.setObject(act);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
        if (act != null) {
            window.edit();
        }
    }

    /**
     * Lays out the workspace.
     *
     * @param view the schedule view
     */
    protected void layoutWorkspace(Entity view) {
        setBrowser(createBrowser());
        setCRUDWindow(createCRUDWindow());
        setWorkspace(createWorkspace());
    }

    /**
     * Creates the workspace split pane.
     *
     * @return a new workspace split pane
     */
    protected Component createWorkspace() {
        Component acts = browser.getComponent();
        Component window = getCRUDWindow().getComponent();
        return SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "SchedulingWorkspace.Layout", window, acts);
    }

    /**
     * Registers a new browser.
     *
     * @param browser the new browser
     */
    protected void setBrowser(ScheduleBrowser browser) {
        this.browser = browser;
        browser.addScheduleBrowserListener(new ScheduleBrowserListener() {
            public void query() {
                onQuery();
            }

            public void selected(PropertySet object) {
                eventSelected(object);
            }

            public void edit(PropertySet set) {
                onEdit(set);
            }

            public void create() {
                window.create();
            }
        });
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    protected ScheduleBrowser getBrowser() {
        return browser;
    }

    /**
     * Registers a new workspace.
     *
     * @param workspace the workspace
     */
    protected void setWorkspace(Component workspace) {
        SplitPane root = getRootComponent();
        if (this.workspace != null) {
            root.remove(this.workspace);
        }
        this.workspace = workspace;
        root.add(this.workspace);
    }

    /**
     * Registers a new CRUD window.
     *
     * @param window the window
     */
    protected void setCRUDWindow(ScheduleCRUDWindow window) {
        this.window = window;
        this.window.setListener(new CRUDWindowListener<Act>() {
            public void saved(Act object, boolean isNew) {
                onSaved(object, isNew);
            }

            public void deleted(Act object) {
                onDeleted(object);
            }

            public void refresh(Act object) {
                onRefresh(object);
            }
        });
    }

    /**
     * Returns the CRUD window.
     *
     * @return the CRUD window
     */
    protected CRUDWindow<Act> getCRUDWindow() {
        return window;
    }

    /**
     * Perform an initial query, selecting the first available act.
     *
     * @param view the party
     */
    protected void initQuery(Entity view) {
        browser.setScheduleView(view);
        browser.query();
        onQuery();
    }

    /**
     * Returns the workspace.
     *
     * @return the workspace. May be <tt>null</tt>
     */
    protected Component getWorkspace() {
        return workspace;
    }

    /**
     * Invoked when events are queried.
     * <p/>
     * Should be overridden to update the global context.
     * <p/>
     * This implementation refreshes the summary.
     */
    protected void onQuery() {
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Entity latest = getLatest();
        if (latest != getObject()) {
            setObject(latest);
        } else if (browser == null) {
            layoutWorkspace(null);
            latest = browser.getScheduleView();
            setObject(latest);
        } else {
            browser.query();

            // need to add the existing workspace to the container
            Component workspace = getWorkspace();
            if (workspace != null) {
                container.add(workspace);
            }
        }
    }

    /**
     * Invoked when the practice location changes. Updates the schedule view.
     *
     * @param newLocation the new location. May be <tt>null</tt>
     */
    private void locationChanged(Party newLocation) {
        if (newLocation == null) {
            setObject(null);
        } else if (!ObjectUtils.equals(location, newLocation)) {
            setObject(getDefaultView(newLocation));
        }
    }

}
