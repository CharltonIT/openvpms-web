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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.sms;

import org.openvpms.web.component.bound.Binder;
import org.openvpms.web.component.bound.TextAreaComponentBinder;
import org.openvpms.web.component.echo.SMSTextArea;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.StringPropertyTransformer;
import org.openvpms.web.component.util.TextDocument;

/**
 * Binds a {@link Property} to an {@link SMSTextArea}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class BoundSMSTextArea extends SMSTextArea {

    /**
     * The binder.
     */
    public Binder binder;


    /**
     * Constructs a new <tt>BoundTextArea</tt>.
     * If not already present, the property is associated with an {@link StringPropertyTransformer}
     * that doesn't trim leading and trailing spaces or new lines.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display
     * @param rows     the no. of rows to display
     */
    public BoundSMSTextArea(Property property, int columns, int rows) {
        super(new TextDocument());
        binder = new TextAreaComponentBinder(this, property, columns, rows);
    }

    /**
     * Life-cycle method invoked when the <tt>Component</tt> is added to a registered hierarchy.
     */
    @Override
    public void init() {
        super.init();
        binder.bind();
    }

    /**
     * Life-cycle method invoked when the <tt>Component</tt> is removed from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        binder.unbind();
    }
}