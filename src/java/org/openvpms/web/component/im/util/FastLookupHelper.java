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

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IArchetypeQuery;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Helper to perform lookup queries that return partially populated lookups,
 * for performance reasons.
 * <p/>
 * The {@link Lookup#getSourceLookupRelationships()}
 * and {@link Lookup#getTargetLookupRelationships()} are not populated,
 * and should not be invoked.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FastLookupHelper {

    /**
     * The nodes to return.
     */
    private static final List<String> NODES = Arrays.asList("code", "name");


    /**
     * Returns a list of lookups for the specified short name.
     *
     * @param shortName the lookup short name
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    public static List<Lookup> getLookups(String shortName) {
        ArchetypeQuery query
                = new ArchetypeQuery(new String[]{shortName}, false, true)
                .setFirstResult(0)
                .setMaxResults(IArchetypeQuery.ALL_RESULTS);
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        List result = service.get(query, NODES).getResults();
        return (List<Lookup>) result;
    }

    /**
     * Return a list of lookups for the specified {@link NodeDescriptor}.
     *
     * @param descriptor the node descriptor
     * @return the list of lookups associated with the descriptor
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static List<Lookup> getLookups(NodeDescriptor descriptor) {
        return LookupHelper.get(ArchetypeServiceHelper.getArchetypeService(),
                                descriptor, NODES);
    }

    /**
     * Return a list of lookups for the specified {@link NodeDescriptor}
     * and context.
     *
     * @param descriptor the node descriptor
     * @param context    the context object
     * @return the list of lookups associated with the descriptor
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static List<Lookup> getLookups(NodeDescriptor descriptor,
                                          IMObject context) {
        return LookupHelper.get(ArchetypeServiceHelper.getArchetypeService(),
                                descriptor, context, NODES);
    }

    /**
     * Returns a map of lookup codes to lookup names for the specified
     * {@link NodeDescriptor}.
     *
     * @param descriptor the node descriptor
     * @return a map of lookup codes to lookup names
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static Map<String, String> getLookupNames(
            NodeDescriptor descriptor) {
        Map<String, String> result = new HashMap<String, String>();
        for (Lookup lookup : getLookups(descriptor)) {
            result.put(lookup.getCode(), lookup.getName());
        }
        return result;
    }
}
