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

package org.openvpms.web.component.util;

import org.openvpms.web.component.bound.BoundDateField;
import org.openvpms.web.component.bound.BoundDateTimeField;
import org.openvpms.web.component.edit.Property;

/**
 * Factory for {@link BoundDateTimeField}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DateTimeFieldFactory extends ComponentFactory {

    /**
     * Creates a new bound date-time field with the default style.
     *
     * @param property the property to bind
     * @return a new bound date-time field
     */
    public static BoundDateTimeField create(Property property) {
        BoundDateTimeField field = new BoundDateTimeField(property);
        BoundDateField date = field.getDate();
        setDefaultStyle(date.getDateChooser());
        setDefaultStyle(date.getTextField());
        setDefaultStyle(field.getTime());
        return field;
    }

}
