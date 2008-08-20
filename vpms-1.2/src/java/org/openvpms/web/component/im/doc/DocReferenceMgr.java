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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.LinkedList;


/**
 * Helper to manage a set of document references.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class DocReferenceMgr {

    /**
     * The set of references to manage.
     */
    private LinkedList<IMObjectReference> references
            = new LinkedList<IMObjectReference>();

    /**
     * Constructs a new <tt>DocReferenceMgr</ttt>.
     *
     * @param original the original reference. May be <tt>null</tt>
     */
    public DocReferenceMgr(IMObjectReference original) {
        if (original != null) {
            references.add(original);
        }
    }

    /**
     * Adds a new document reference.
     *
     * @param reference the reference to add
     */
    public void add(IMObjectReference reference) {
        references.add(reference);
    }

    /**
     * Commits the changes. Every document bar the most recent will be removed.
     *
     * @throws ArchetypeServiceException for any error
     */
    public void commit() {
        while (references.size() > 1) {
            remove(references.removeFirst());
        }
    }

    /**
     * Rolls back the changes. Every document bar the oldest will be removed.
     *
     * @throws ArchetypeServiceException for any error
     */
    public void rollback() {
        while (references.size() > 1) {
            remove(references.removeLast());
        }
    }

    /**
     * Deletes all documents.
     *
     * @throws ArchetypeServiceException for any error
     */
    public void delete() {
        while (!references.isEmpty()) {
            remove(references.removeLast());
        }
    }

    /**
     * Removes the document associated with a document reference.
     *
     * @param reference the reference of the document to remove
     * @throws ArchetypeServiceException for any error
     */
    private void remove(IMObjectReference reference) {
        IMObject object = IMObjectHelper.getObject(reference);
        if (object != null) {
            ArchetypeServiceHelper.getArchetypeService().remove(object);
        }
    }

}
