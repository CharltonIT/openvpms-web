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

package org.openvpms.web.component.workflow;

import org.openvpms.web.component.util.ErrorHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation of the {@link Workflow} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class WorkflowImpl extends AbstractTask implements Workflow {

    /**
     * The tasks to execute.
     */
    private final List<Task> tasks = new ArrayList<Task>();

    /**
     * The task context.
     */
    private TaskContext initial;

    /**
     * The current task index.
     */
    private int taskIndex;

    /**
     * Determines if the workflow should cancel.
     */
    private boolean cancel;

    /**
     * Determines if cancelling a task terminates the workflow.
     */
    private boolean breakOnCancel = true;

    /**
     * Determines if skipping a task terminates the workflow.
     */
    private boolean breakOnSkip;

    /**
     * The listener to handle task events.
     */
    private final TaskListener taskListener;

    /**
     * The current task.
     */
    private Task current;


    /**
     * Constructs a new <code>WorkflowImpl</code>.
     */
    public WorkflowImpl() {
        taskListener = new TaskListener() {
            public void taskEvent(TaskEvent event) {
                onEvent(event);
            }
        };
    }

    /**
     * Adds a task to the workflow.
     *
     * @param task the task to add
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /**
     * Determines if cancelling a task should cause the workflow to terminate.
     * This only applies to tasks that have been successfully started.
     *
     * @param breakOnCancel if <tt>true</tt> terminate the workflow if a task
     *                      is cancelled. Defaults to <tt>true</tt>
     */
    public void setBreakOnCancel(boolean breakOnCancel) {
        this.breakOnCancel = breakOnCancel;
    }

    /**
     * Determines if skipping a task should cause the workflow to terminate.
     *
     * @param breakOnSkip if <code>true</code> terminate the workflow if a task
     *                    is skipped
     */
    public void setBreakOnSkip(boolean breakOnSkip) {
        this.breakOnSkip = breakOnSkip;
    }

    /**
     * Starts the workflow.
     */
    public void start() {
        start(new DefaultTaskContext());
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     */
    public void start(TaskContext context) {
        cancel = false;
        taskIndex = 0;
        this.initial = context;
        next();
    }

    /**
     * Cancels the workflow.
     */
    public void cancel() {
        this.cancel = true;
    }

    /**
     * Returns the current task.
     *
     * @return the current task, or <tt>null</tt> if there is none
     */
    public Task getCurrent() {
        return current;
    }

    /**
     * Executes the next task.
     */
    protected void next() {
        if (cancel) {
            current = null;
            notifyCancelled();
        } else if (taskIndex < tasks.size()) {
            current = tasks.get(taskIndex++);
            try {
                current.addTaskListener(taskListener);
                current.start(initial);
            } catch (Throwable throwable) {
                cancel = true;
                ErrorHelper.show(throwable);
            }
        } else {
            current = null;
            notifyCompleted();
        }
    }

    /**
     * Invoked when a task generates an event.
     *
     * @param event the event
     */
    protected void onEvent(TaskEvent event) {
        switch (event.getType()) {
            case SKIPPED:
                if (event.getTask().isRequired()) {
                    ErrorHelper.show("Required task skipped");
                    cancel = true;
                    notifyCancelled();
                } else if (breakOnSkip) {
                    notifySkipped();
                } else {
                    next();
                }
                break;
            case CANCELLED:
                if (breakOnCancel) {
                    notifyCancelled();
                } else {
                    next();
                }
                break;
            case COMPLETED:
                next();
                break;
        }
    }

}
