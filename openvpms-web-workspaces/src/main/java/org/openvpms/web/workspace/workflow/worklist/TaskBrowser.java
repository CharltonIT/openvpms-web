/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

import echopointng.TableEx;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.workspace.workflow.scheduling.Cell;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleBrowser;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Task browser.
 *
 * @author Tim Anderson
 */
public class TaskBrowser extends ScheduleBrowser {

    /**
     * Table cell renderer.
     */
    private final TaskTableCellRenderer renderer;

    /**
     * Constructs a {@link TaskBrowser}.
     *
     * @param context the context
     */
    public TaskBrowser(Context context) {
        super(new TaskQuery(context), context);
        renderer = new TaskTableCellRenderer();
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    @Override
    public void query() {
        doQuery(false);
    }

    /**
     * Attempts to select a task.
     *
     * @param task the task
     * @return {@code true} if the task was selected, {@code false} if it was not found
     */
    public boolean setSelected(Act task) {
        ActBean bean = new ActBean(task);
        PropertySet selected = null;
        IMObjectReference worklist = bean.getNodeParticipantRef("worklist");
        if (worklist != null) {
            IMObjectReference taskRef = task.getObjectReference();
            ScheduleTableModel model = getModel();
            Cell cell = model.getCell(worklist, taskRef);
            if (cell != null) {
                model.setSelected(cell);
                selected = model.getEvent(cell);
            }
        }
        setSelected(selected);
        return selected != null;
    }


    /**
     * Creates a new grid for a set of events.
     *
     * @param date   the query date
     * @param events the events
     */
    protected ScheduleEventGrid createEventGrid(Date date, Map<Entity, List<PropertySet>> events) {
        return new TaskGrid(getScheduleView(), date, events);
    }

    /**
     * Creates a new table model.
     *
     * @param grid the schedule event grid
     * @return the table model
     */
    protected ScheduleTableModel createTableModel(ScheduleEventGrid grid) {
        if (grid.getSchedules().size() == 1) {
            return new SingleScheduleTaskTableModel((TaskGrid) grid, getContext());
        }
        return new MultiScheduleTaskTableModel((TaskGrid) grid, getContext());
    }

    /**
     * Initialises a table.
     *
     * @param table the table
     */
    @Override
    protected void initTable(TableEx table) {
        table.setDefaultRenderer(renderer);
    }

}
