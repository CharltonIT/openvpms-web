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

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.TextField;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.TextDocument;


/**
 * Binds a {@link Property} to a <code>TextField</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundTextField extends TextField {

    /**
     * Construct a new <code>BoundTextField</code>.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display
     */
    public BoundTextField(Property property, int columns) {
        super(new TextDocument());
        if (columns <= 10) {
            setWidth(new Extent(columns, Extent.EM));
        } else {
            setWidth(new Extent(columns, Extent.EX));
        }

        Binder binder = new TextComponentBinder(this, property);
        binder.setField();
        if (!StringUtils.isEmpty(property.getDescription())) {
            setToolTipText(property.getDescription());
        }
    }

}