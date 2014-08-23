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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ObjectSet;


/**
 * Factory for {@link RelationshipState} instances.
 *
 * @author Tim Anderson
 */
public class RelationshipStateFactory {

    /**
     * Creates a new {@link RelationshipState}.
     *
     * @param parent       the parent object
     * @param relationship the relationship
     * @param sourceId     the source id
     * @param sourceName   the source name
     * @param sourceDesc   the source description
     * @param targetId     the target id
     * @param targetName   the target name
     * @param targetDesc   the target description
     * @param active       determines the objects are active
     * @param set          the set
     * @return a new {@link RelationshipState}
     */
    public RelationshipState create(IMObject parent, IMObjectRelationship relationship,
                                    long sourceId, String sourceName,
                                    String sourceDesc, long targetId,
                                    String targetName, String targetDesc,
                                    boolean active, ObjectSet set) {
        return new RelationshipState(relationship, sourceId, sourceName,
                                     sourceDesc, targetId, targetName,
                                     targetDesc, active);
    }

    /**
     * Creates a new {@link RelationshipState}.
     *
     * @param parent       the parent object
     * @param relationship the relationship
     * @param source       determines if parent is the source or target of the relationship
     * @return a new {@link RelationshipState}
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public RelationshipState create(IMObject parent, IMObjectRelationship relationship, boolean source) {
        return new RelationshipState(parent, relationship, source);
    }

}
