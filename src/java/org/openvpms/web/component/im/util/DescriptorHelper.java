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

package org.openvpms.web.component.im.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.spring.ServiceHelper;

import java.util.*;


/**
 * Helper class for working with {@link ArchetypeDescriptor} and {@link
 * NodeDescriptor}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class DescriptorHelper {

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(DescriptorHelper.class);


    /**
     * Returns an archetype descriptor, given its shortname.
     *
     * @param shortName the shortname
     * @return the descriptor corresponding to <code>shortName</code>, or
     *         <code>null</code> if none exists
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(String shortName) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        return service.getArchetypeDescriptor(shortName);
    }

    /**
     * Returns an archetype descriptor, given a reference.
     *
     * @param reference the object reference.
     * @return the descriptor corresponding to <code>reference</code>, or
     *         <code>null</code> if none exists
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(
            IMObjectReference reference) {
        ArchetypeId id = reference.getArchetypeId();
        return getArchetypeDescriptor(id.getShortName());
    }

    /**
     * Returns the archetype descriptor for the specified object.
     *
     * @param object the object
     * @return the archetype descriptor corresponding to <code>object</code>
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(IMObject object) {
        return getArchetypeDescriptor(object,
                ServiceHelper.getArchetypeService());
    }

    /**
     * Returns the archetype descriptor for the specified object.
     *
     * @param object  the object
     * @param service the archetype service
     * @return the archetype descriptor corresponding to <code>object</code>
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(
            IMObject object, IArchetypeService service) {
        ArchetypeDescriptor descriptor;
        ArchetypeId archId = object.getArchetypeId();

        //TODO This is a work around until we resolve the current
        // problem with archetyping and archetype. We need to
        // extend this page and create a new archetype specific
        // edit page.
        if (object instanceof AssertionDescriptor) {
            AssertionTypeDescriptor atDesc = service.getAssertionTypeDescriptor(
                    object.getName());
            archId = new ArchetypeId(atDesc.getPropertyArchetype());
        }

        descriptor = service.getArchetypeDescriptor(archId);
        if (descriptor == null) {
            descriptor = getArchetypeDescriptor(
                    object.getArchetypeId().getShortName());
        }

        if (_log.isDebugEnabled()) {
            _log.debug("Returning archetypeDescriptor="
                    + (descriptor == null ? null : descriptor.getName())
                    + " for archId=" + archId
                    + " and object=" + object.getClass().getName());
        }

        return descriptor;
    }

    /**
     * Returns the archetype descriptors for an archetype range.
     */
    public static List<ArchetypeDescriptor> getArchetypeDescriptors(
            String[] range) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<ArchetypeDescriptor> result = new ArrayList<ArchetypeDescriptor>();
        for (String shortName : range) {
            result.addAll(service.getArchetypeDescriptors(shortName));
        }
        return result;
    }

    /**
     * Returns archetype short names from a descriptor.
     * This expands any wildcards. If the {@link NodeDescriptor#getFilter} is non-null, matching shortnames are
     * returned, otherwise matching short names from {@link NodeDescriptor#getArchetypeRange()} are returned.
     *
     * @param descriptor the node descriptor
     * @return a list of short names
     */
    public static String[] getShortNames(NodeDescriptor descriptor) {
        String filter = descriptor.getFilter();
        String[] names;
        if (!StringUtils.isEmpty(filter)) {
            names = DescriptorHelper.getShortNames(filter, false);
        } else {
            names = DescriptorHelper.getShortNames(
                    descriptor.getArchetypeRange(), false);
        }
        return names;
    }

    /**
     * Returns archetype short names matching the specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @return a list of short names matching the criteria
     */
    public static String[] getShortNames(String refModelName,
                                         String entityName,
                                         String conceptName) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<String> names = Collections.emptyList();
        try {
            names = service.getArchetypeShortNames(refModelName,
                    entityName, conceptName, true);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return names.toArray(new String[0]);
    }

    /**
     * Returns primary archetype short names matching the specified criteria.
     *
     * @param shortName the short name. May contain wildcards
     * @return a list of short names matching the criteria
     */
    public static String[] getShortNames(String shortName) {
        return getShortNames(new String[]{shortName}, true);
    }

    /**
     * Returns archetype short names matching the specified criteria.
     *
     * @param shortName   the short name. May contain wildcards
     * @param primaryOnly if <code>true</code> only include primary archetypes
     * @return a list of short names matching the criteria
     */
    public static String[] getShortNames(String shortName, boolean primaryOnly) {
        return getShortNames(new String[]{shortName}, primaryOnly);
    }

    /**
     * Returns primary archetype short names matching the specified criteria.
     *
     * @param shortNames the short names. May contain wildcards
     * @return a list of short names matching the criteria
     */
    public static String[] getShortNames(String[] shortNames) {
        return getShortNames(shortNames, true);
    }

    /**
     * Returns archetype short names matching the specified criteria.
     *
     * @param shortNames  the shortNames. May contain wildcards
     * @param primaryOnly if <code>true</code> only include primary archetypes
     * @return a list of short names matching the criteria
     */
    public static String[] getShortNames(String[] shortNames, boolean primaryOnly) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        Set<String> result = new HashSet<String>();
        try {
            for (String shortName : shortNames) {
                List<String> matches = service.getArchetypeShortNames(shortName, primaryOnly);
                result.addAll(matches);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return result.toArray(new String[0]);
    }

    /**
     * Returns the display name for an archetype.
     *
     * @param shortName the archetype short name
     * @return the archetype display name, or <code>null</code> if none exists
     */
    public static String getDisplayName(String shortName) {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(shortName);
        return (descriptor != null) ? descriptor.getDisplayName() : null;
    }

    /**
     * Returns the display name for an object.
     *
     * @param object the object
     * @return a display name for the object, or <code>null</code> if none
     *         exists
     */
    public static String getDisplayName(IMObject object) {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(object);
        return (descriptor != null) ? descriptor.getDisplayName() : null;
    }

    /**
     * Returns the display name for a node
     *
     * @param object the object
     * @param node   the node
     * @return a display name for the node, or <code>null</code> if none eixsts
     */
    public static String getDisplayName(IMObject object, String node) {
        String result = null;
        ArchetypeDescriptor archetype = getArchetypeDescriptor(object);
        if (archetype != null) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
            if (descriptor != null) {
                result = descriptor.getDisplayName();
            }
        }
        return result;
    }

    /**
     * Determines if an archetype identifiers short name matches a (potentially
     * wildcarded) short name.
     *
     * @param id        the archetype identifier
     * @param shortName the short name to compare
     * @return <code>true</code> if the short name matches; otherwise
     *         <code>false</code>
     */
    public static boolean matches(ArchetypeId id, String shortName) {
        return matches(id.getShortName(), shortName);
    }

    /**
     * Determiens if a short name matches any of a list of (potentially
     * wildcarded) short names.
     *
     * @param shortName  the short name
     * @param shortNames the short names to check
     * @return <code>true</code> if the short name matches; otherwise
     *         <code>false</code>
     */
    public static boolean matches(String shortName, String[] shortNames) {
        for (String other : shortNames) {
            if (matches(shortName, other)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a short name matches a wildcarded short name.
     *
     * @param shortName the short name
     * @param wildcard  the wildcarded short name
     * @return <code>true</code> if the short names matches; otherwise
     *         <code>false</code>
     */
    public static boolean matches(String shortName, String wildcard) {
        String regexp = wildcard.replace(".", "\\.").replace("*", ".*");
        return shortName.matches(regexp);
    }

    /**
     * Determines the minimum cardinality from an archetype range assertion.
     *
     * @param descriptor the node descriptor
     * @param shortName  the archetype short name
     * @return the minimum cardinality, or <code>0</code> if none is specified
     */
    public static int getMinCardinality(NodeDescriptor descriptor,
                                        String shortName) {
        int result = 0;
        NamedProperty property
                = getArchetypeProperty(descriptor, shortName, "minCardinality");
        if (property != null) {
            result = Integer.parseInt(property.getValue().toString());
        }
        return result;
    }

    /**
     * Determines the maximum cardinality from an archetype range assertion.
     *
     * @param descriptor the node descriptor
     * @param shortName  the archetype short name
     * @return the maximum cardinality, or <code>0</code> if none is specified
     */
    public static int getMaxCardinality(NodeDescriptor descriptor,
                                        String shortName) {
        int result = 0;
        NamedProperty property
                = getArchetypeProperty(descriptor, shortName, "maxCardinality");
        if (property != null) {
            if (NodeDescriptor.UNBOUNDED_AS_STRING.equals(property.getValue()))
            {
                result = NodeDescriptor.UNBOUNDED;
            } else {
                result = Integer.parseInt(property.getValue().toString());
            }
        }
        return result;
    }

    /**
     * Helper to return a named propery.
     *
     * @param descriptor the node descriptor
     * @param shortName  the archetype  short name
     * @param name       the property name
     * @return the named property, or <code>null</code> if none exists
     */
    private static NamedProperty getArchetypeProperty(NodeDescriptor descriptor,
                                                      String shortName,
                                                      String name) {
        NamedProperty result = null;
        PropertyMap properties = getArchetypeProperties(descriptor, shortName);
        if (properties != null) {
            result = properties.getProperties().get(name);
        }
        return result;
    }

    /**
     * Returns properties from a descriptors archetype range for a paricular
     * archetype
     *
     * @param descriptor the node descriptor
     * @param shortName  the archetype short name to matches on
     * @return the properties for the specfied archetype, or <code>null</code>
     *         if none exists
     */
    private static PropertyMap getArchetypeProperties(NodeDescriptor descriptor,
                                                      String shortName) {
        PropertyMap result = null;
        AssertionDescriptor assertionDesc
                = descriptor.getAssertionDescriptor("archetypeRange");
        if (assertionDesc != null) {
            PropertyList archetypes
                    = (PropertyList) assertionDesc.getProperty("archetypes");
            for (NamedProperty prop : archetypes.getPropertiesAsArray()) {
                PropertyMap archetype = (PropertyMap) prop;
                NamedProperty name = archetype.getProperties().get("shortName");
                if (name.getValue().equals(shortName)) {
                    result = archetype;
                    break;
                }
            }
        }
        return result;
    }

}
