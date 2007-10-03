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

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.relationship.RelationshipState;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class PatientRelationshipState extends RelationshipState {

    /**
     * Determines if the patient is deceased.
     */
    private boolean deceased;


    /**
     * Creates a new <tt>PatientRelationshipState</tt>.
     *
     * @param relationship      the relationship
     * @param sourceUID         the source entity UID
     * @param sourceName        the source entity name
     * @param sourceDescription the source entity description
     * @param targetUID         the target entity UID
     * @param targetName        the target entity name
     * @param targetDescription the target entity description
     * @param active            determines if the relationship and entities are
     *                          active. This may be independent of their
     *                          respective active flags
     */
    public PatientRelationshipState(EntityRelationship relationship,
                                    long sourceUID, String sourceName,
                                    String sourceDescription,
                                    long targetUID, String targetName,
                                    String targetDescription,
                                    boolean active) {
        super(relationship, sourceUID, sourceName, sourceDescription, targetUID,
              targetName, targetDescription, active);
    }

    /**
     * Creates a new <tt>PatientRelationshipState</tt>.
     *
     * @param entity       the parent entity
     * @param relationship the relationship
     * @param source       determines if entity is the source or target of the
     *                     relationship
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public PatientRelationshipState(Entity entity,
                                    EntityRelationship relationship,
                                    boolean source) {
        super(entity, relationship, source);
    }

    /**
     * Determines if the patient is deceased.
     *
     * @return <tt>true</tt> if the patient is deceased
     */
    public boolean isDeceased() {
        return deceased;
    }

    /**
     * Sets the deceased state of the patient.
     *
     * @param deceased if <tt>true</tt> indicates the patient is deceased
     */
    public void setDeceased(boolean deceased) {
        this.deceased = deceased;
    }
}