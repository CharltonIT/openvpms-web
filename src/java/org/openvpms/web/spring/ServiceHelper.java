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

package org.openvpms.web.spring;

import nextapp.echo2.app.ApplicationInstance;
import org.springframework.context.ApplicationContext;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;


/**
 * Helper for accessing services managed by Spring.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $Revision$ $Date$
 */
public final class ServiceHelper {

    /**
     * Helper to get the archetype service.
     *
     * @return the archetype service
     */
    public static IArchetypeService getArchetypeService() {
        return (IArchetypeService) getContext().getBean("archetypeService");
    }

    /**
     * Helper to get the lookup service.
     *
     * @return the lookup service
     */
    public static ILookupService getLookupService() {
        return (ILookupService) getContext().getBean("lookupService");
    }

    /**
     * Helper to return the application context associated with the current
     * thread.
     *
     * @return the application context associated with the current thread.
     */
    public static ApplicationContext getContext() {
        SpringApplicationInstance app
                = (SpringApplicationInstance) ApplicationInstance.getActive();
        return app.getApplicationContext();
    }

}
