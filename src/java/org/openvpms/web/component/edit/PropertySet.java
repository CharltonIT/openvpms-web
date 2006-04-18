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

package org.openvpms.web.component.edit;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.util.DescriptorHelper;


/**
 * Set of {@link Property} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PropertySet {

    /**
     * The properties.
     */
    private Map<String, Property> _properties = new HashMap<String, Property>();

    /**
     * Construct a new <code>PropertySet</code>.
     *
     * @param object the object
     */
    public PropertySet(IMObject object) {
        this(object, DescriptorHelper.getArchetypeDescriptor(object));
    }

    /**
     * Construct a new <code>PropertySet</code>.
     *
     * @param object    the object
     * @param archetype the archetype descriptor
     */
    public PropertySet(IMObject object, ArchetypeDescriptor archetype) {
        for (NodeDescriptor descriptor : archetype.getAllNodeDescriptors()) {
            if (!descriptor.isHidden()) {
                Property property;
                if (descriptor.isReadOnly()) {
                    property = new ReadOnlyProperty(object, descriptor);
                } else {
                    property = new ModifiableProperty(object, descriptor);
                }
                _properties.put(descriptor.getName(), property);
            }
        }

    }

    /**
     * Returns the named property.
     *
     * @param name the name
     * @return the property corresponding to <code>name</code>, or
     *         <code>null</code> if none exists
     */
    public Property get(String name) {
        return _properties.get(name);
    }

    /**
     * Returns a property given its descriptor.
     *
     * @param descriptor the descriptor
     * @return the property corresponding to <code>descriptor</code>, or
     *         <code>null</code> if none exists
     */
    public Property get(NodeDescriptor descriptor) {
        return get(descriptor.getName());
    }

    /**
     * Returns the properties.
     *
     * @return the properties
     */
    public Collection<Property> getProperties() {
        return _properties.values();
    }

}
