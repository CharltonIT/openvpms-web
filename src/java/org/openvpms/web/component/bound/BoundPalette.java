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

package org.openvpms.web.component.bound;

import java.util.ArrayList;
import java.util.List;

import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.palette.Palette;


/**
 * Binds a {@link Property} to a <code>Palette</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundPalette extends Palette {

    /**
     * The bound property.
     */
    private CollectionProperty _property;


    /**
     * Construct a new <code>BoundPalette</coce>.
     *
     * @param items    all items that may be selected
     * @param property the property to bind
     */
    public BoundPalette(List items, CollectionProperty property) {
        super(items, new ArrayList(property.getValues()));
        _property = property;
    }

    /**
     * Add items to the 'selected' list.
     *
     * @param values the values to add.
     */
    @Override
    protected void add(Object[] values) {
        for (Object value : values) {
            _property.add(value);
        }
    }

    /**
     * Remove items from the 'selected' list.
     *
     * @param values the values to remove
     */
    @Override
    protected void remove(Object[] values) {
        for (Object value : values) {
            _property.remove(value);
        }
    }
}
