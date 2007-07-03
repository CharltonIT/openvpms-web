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

package org.openvpms.web.component.property;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


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
    private Map<String, Property> properties = new HashMap<String, Property>();


    /**
     * Constructs a new <tt>PropertySet</tt> from an object.
     *
     * @param object the object
     */
    public PropertySet(IMObject object) {
        this(object, DescriptorHelper.getArchetypeDescriptor(object));
    }

    /**
     * Constructs a new <tt>PropertySet</tt> for an object and descriptor.
     *
     * @param object    the object
     * @param archetype the archetype descriptor
     */
    public PropertySet(IMObject object, ArchetypeDescriptor archetype) {
        for (NodeDescriptor descriptor : archetype.getAllNodeDescriptors()) {
            Property property = new IMObjectProperty(object, descriptor);
            properties.put(descriptor.getName(), property);
        }
    }

    /**
     * Returns the named property.
     *
     * @param name the name
     * @return the property corresponding to <tt>name</tt>, or <tt>null</tt>
     *         if none exists
     */
    public Property get(String name) {
        return properties.get(name);
    }

    /**
     * Returns a property given its descriptor.
     *
     * @param descriptor the descriptor
     * @return the property corresponding to <tt>descriptor</tt>, or
     *         <tt>null</tt> if none exists
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
        return properties.values();
    }

    /**
     * Determines if any of the properties have been modified.
     *
     * @return <tt>true</tt> if at least one property has been modified
     */
    public boolean isModified() {
        for (Property property : getProperties()) {
            if (property.isModified()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clears the modified status of all properties.
     */
    public void clearModified() {
        for (Property property : getProperties()) {
            property.clearModified();
        }
    }

}
