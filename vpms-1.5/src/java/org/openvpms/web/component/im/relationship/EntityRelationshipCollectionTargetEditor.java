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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * A editor for collections of {@link EntityRelationship}s, where the
 * target of the relationship is the object being edited.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipCollectionTargetEditor
        extends RelationshipCollectionTargetEditor {

    /**
     * Construct a new <tt>EntityRelationshipCollectionTargetEditor</tt>.
     *
     * @param property the collection property
     * @param entity   the parent entity
     * @param context  the layout context
     */
    public EntityRelationshipCollectionTargetEditor(CollectionProperty property,
                                                    Entity entity,
                                                    LayoutContext context) {
        super(new EntityRelationshipCollectionTargetPropertyEditor(property,
                                                                   entity),
              entity, context);
    }
}
