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

package org.openvpms.web.component.bound;

import nextapp.echo2.app.Color;
import nextapp.echo2.extras.app.ColorSelect;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ColourHelper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BoundColorSelect extends ColorSelect {

    /**
     * The property binder.
     */
    private final Binder binder;


    /**
     * Construct a new <tt>BoundColorSelect</tt>.
     *
     * @param property the property to bind
     */
    public BoundColorSelect(Property property) {
        binder = new Binder(property) {
            protected Object getFieldValue() {
                return ColourHelper.getString(getColor());
            }

            protected void setFieldValue(Object value) {
                Color color = convert(value);
                setColor(color);
            }

        };
        binder.setField();
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                binder.setProperty();
            }
        });
    }

    private Color convert(Object value) {
        return value != null ? ColourHelper.getColor(value.toString()) : null;
    }
}