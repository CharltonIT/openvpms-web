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

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.util.Date;
import java.util.Iterator;


/**
 * Contains the details required to render an {@link EntityRelationship}.
 * This is used to reduce database accesses.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipState {

    /**
     * The relationship.
     */
    private final EntityRelationship relationship;

    /**
     * The source entity id.
     */
    private long sourceId;

    /**
     * The source entity name.
     */
    private String sourceName;

    /**
     * The source entity description.
     */
    private String sourceDescription;

    /**
     * The target entity id.
     */
    private long targetId;

    /**
     * The target entity name.
     */
    private String targetName;

    /**
     * The target entity description.
     */
    private String targetDescription;

    /**
     * Determines if the relationship and corresponding entities are active.
     */
    private boolean active;


    /**
     * Creates a new <tt>RelationshipState</tt>.
     *
     * @param relationship      the relationship
     * @param sourceId          the source entity id
     * @param sourceName        the source entity name
     * @param sourceDescription the source entity description
     * @param targetId          the target entity id
     * @param targetName        the target entity name
     * @param targetDescription the target entity description
     * @param active            determines the entities are active
     */
    public RelationshipState(EntityRelationship relationship,
                             long sourceId, String sourceName,
                             String sourceDescription,
                             long targetId, String targetName,
                             String targetDescription,
                             boolean active) {
        this.relationship = relationship;
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.sourceDescription = sourceDescription;
        this.targetId = targetId;
        this.targetName = targetName;
        this.targetDescription = targetDescription;
        this.active = active;
    }

    /**
     * Creates a new <tt>RelationshipState</tt>.
     *
     * @param entity       the parent entity
     * @param relationship the relationship
     * @param source       determines if entity is the source or target of the
     *                     relationship
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public RelationshipState(Entity entity, EntityRelationship relationship,
                             boolean source) {
        this.relationship = relationship;
        IMObjectReference reference = (source)
                ? relationship.getTarget() : relationship.getSource();
        if (reference != null) {
            ObjectRefConstraint constraint
                    = new ObjectRefConstraint("o", reference);
            ArchetypeQuery query = new ArchetypeQuery(constraint);
            query.add(new NodeSelectConstraint("o.id"));
            query.add(new NodeSelectConstraint("o.name"));
            query.add(new NodeSelectConstraint("o.description"));
            query.add(new NodeSelectConstraint("o.active"));
            query.setMaxResults(1);
            Iterator<ObjectSet> iter = new ObjectSetQueryIterator(query);
            if (iter.hasNext()) {
                ObjectSet set = iter.next();
                long id = set.getLong("o.id");
                String name = set.getString("o.name");
                String desc = set.getString("o.description");
                active = set.getBoolean("o.active");
                if (source) {
                    sourceId = entity.getId();
                    sourceName = entity.getName();
                    sourceDescription = entity.getDescription();
                    targetId = id;
                    targetName = name;
                    targetDescription = desc;
                } else {
                    sourceId = id;
                    sourceName = name;
                    sourceDescription = desc;
                    targetId = entity.getId();
                    targetName = entity.getName();
                    targetDescription = entity.getDescription();
                }
            }
        }
    }

    /**
     * Returns the source entity id.
     *
     * @return the source entity id
     */
    public long getSourceId() {
        return sourceId;
    }

    /**
     * Returns the source entity name.
     *
     * @return the source entity name. May be <tt>null</tt>
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Returns the source entity description.
     *
     * @return the source entity description. May be <tt>null</tt>
     */
    public String getSourceDescription() {
        return sourceDescription;
    }

    /**
     * Returns the target entity id.
     *
     * @return the target entity id
     */
    public long getTargetId() {
        return targetId;
    }

    /**
     * Returns the target entity name.
     *
     * @return the target entity name. May be <tt>null</tt>
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * Returns the target entity description.
     *
     * @return the target entity description. May be <tt>null</tt>
     */
    public String getTargetDescription() {
        return targetDescription;
    }

    /**
     * Determines if the relationship is active.
     * It is active if:
     * <ul>
     * <li>the underlying {@link EntityRelationship} is active
     * <li>the underlying entities are active
     * <li>{@link EntityRelationship#getActiveEndTime} is null or greater than
     * the current time
     * </ul>
     *
     * @return <tt>true</tt> if this is active; otherwise <tt>false</tt>
     */
    public boolean isActive() {
        boolean result = false;
        if (active && relationship.isActive()) {
            Date endTime = relationship.getActiveEndTime();
            if (endTime == null
                    || endTime.getTime() > System.currentTimeMillis()) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns the source entity reference.
     *
     * @return the source entity reference
     */
    public IMObjectReference getSource() {
        return relationship.getSource();
    }

    /**
     * Returns the target entity reference.
     *
     * @return the target entity reference
     */
    public IMObjectReference getTarget() {
        return relationship.getTarget();
    }

    /**
     * Returns the relationship.
     *
     * @return the relationship
     */
    public EntityRelationship getRelationship() {
        return relationship;
    }

}
