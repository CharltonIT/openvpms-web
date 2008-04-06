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

package org.openvpms.web.component.im.lookup;

import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.util.List;


/**
 * An {@link LookupQuery} that sources lookups from a list.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ListLookupQuery implements LookupQuery {

    /**
     * The lookups.
     */
    private final List<Lookup> lookups;


    /**
     * Creates a new <Tt>ListLookupQuery</tt>.
     *
     * @param lookups
     */
    public ListLookupQuery(List<Lookup> lookups) {
        this.lookups = lookups;
    }

    /**
     * Returns the lookups.
     *
     * @return the lookups
     */
    public List<Lookup> getLookups() {
        return lookups;
    }
}
