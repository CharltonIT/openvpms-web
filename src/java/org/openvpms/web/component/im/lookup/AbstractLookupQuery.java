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
 * Abstract implementation of {@link LookupQuery}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractLookupQuery implements LookupQuery {

    /**
     * Returns the default lookup.
     *
     * @return the default lookup, or <tt>null</tt> if none is defined
     */
    public Lookup getDefault() {
        return getDefault(getLookups());
    }

    /**
     * Returns the default lookup.
     *
     * @param lookups the lookups to search
     * @return the default lookup
     */
    protected Lookup getDefault(List<Lookup> lookups) {
        for (Lookup lookup : lookups) {
            if (lookup.isDefaultLookup()) {
                return lookup;
            }
        }
        return null;
    }

    /**
     * Helper to return a lookup from a list given its code.
     *
     * @param code the lookup code
     * @return the lookup, or <tt>null</tt> if none is found
     */
    protected Lookup getLookup(String code, List<Lookup> lookups) {
        for (Lookup lookup : lookups) {
            if (code.equals(lookup.getCode())) {
                return lookup;
            }
        }
        return null;
    }


}
