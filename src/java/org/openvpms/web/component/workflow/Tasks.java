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


/**
 * A collection of {@link Task}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Tasks extends AbstractTask {

    private WorkflowImpl workflow = new WorkflowImpl();

    /**
     * Adds a task to execute.
     *
     * @param task the task to execute
     */
    public void addTask(Task task) {
        workflow.addTask(task);
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
        workflow.setTaskListener(new TaskListener() {
            public void taskEvent(TaskEvent event) {
                switch (event.getType()) {
                    case CANCELLED:
                        if (isRequired()) {
                            notifyCancelled();
                        } else {
                            notifyCompleted();
                        }
                        break;
                    case COMPLETED:
                        notifyCompleted();
                }
            }
        });
        workflow.start(context);
    }

}
