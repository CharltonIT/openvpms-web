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

package org.openvpms.web.system;

import nextapp.echo2.app.ApplicationInstance;
import org.springframework.context.ApplicationContext;

/**
 * <code>ApplicationInstance</code> for integrating Echo with Spring.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class SpringApplicationInstance extends ApplicationInstance {

    /**
     * The application context.
     */
    private ApplicationContext _context;

    /**
     * Sets the application context.
     *
     * @param context the application context
     */
    public void setApplicationContext(ApplicationContext context) {
        _context = context;
    }

    /**
     * Returns the application context.
     *
     * @return the application context
     */
    public ApplicationContext getApplicationContext() {
        return _context;
    }

}