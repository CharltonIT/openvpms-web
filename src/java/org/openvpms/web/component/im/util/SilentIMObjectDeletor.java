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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Implementation of {@link IMObjectDeletor} that doesn't prompt for confirmation.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SilentIMObjectDeletor extends IMObjectDeletor {

    /**
     * Invoked to remove an object.
     *
     * @param object   the object to remove
     * @param listener the listener to notify
     */
    protected <T extends IMObject> void remove(T object, IMObjectDeletionListener<T> listener) {
        doRemove(object, listener);
    }

    /**
     * Invoked to remove anobject that has {@link EntityRelationship}s to other objects.
     *
     * @param object   the object to remove
     * @param listener the listener to notify
     */
    protected <T extends IMObject> void removeWithRelationships(T object, IMObjectDeletionListener<T> listener) {
        doRemove(object, listener);
    }

    /**
     * Invoked to deactivate an object.
     *
     * @param object   the object to deactivate
     * @param listener the listener
     */
    protected <T extends IMObject> void deactivate(T object, IMObjectDeletionListener<T> listener) {
        doDeactivate(object, listener);
    }

    /**
     * Invoked when an object cannot be de deleted, and has already been deactivated.
     *
     * @param object the object
     */
    protected <T extends IMObject> void deactivated(T object) {
    }
}
