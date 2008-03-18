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

package org.openvpms.web.app.workflow.messaging;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.app.subsystem.BrowserCRUDWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.Query;


/**
 * Messaging workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MessagingWorkspace extends BrowserCRUDWorkspace<User, Act> {

    /**
     * Construct a new <tt>MessagingWorkspace</tt>.
     */
    public MessagingWorkspace() {
        super("workflow", "messaging", false);
        setArchetypes(User.class, "security.user");
        setChildArchetypes(Act.class, "act.userMessage");
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
     * Determines if the workspace should be refreshed.
     * This implementation always returns <tt>true</tt>.
     *
     * @return <tt>true</tt>
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

    /**
     * Returns the latest version of the current context object.
     *
     * @return the latest version of the context object, or {@link #getObject()}
     *         if they are the same
     */
    @Override
    protected User getLatest() {
        return super.getLatest(GlobalContext.getInstance().getUser());
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    @Override
    protected Query<Act> createQuery() {
        return new MessageQuery(getObject());
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Act> createCRUDWindow() {
        return new MessagingCRUDWindow(getChildArchetypes());
    }

}
