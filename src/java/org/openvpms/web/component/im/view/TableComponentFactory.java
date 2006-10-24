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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.layout.TableLayoutStrategyFactory;
import org.openvpms.web.component.util.DateFormatter;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;

import java.math.BigDecimal;
import java.util.Date;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components for
 * display in a table.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TableComponentFactory extends AbstractReadOnlyComponentFactory {

    /**
     * Construct a new <code>TableComponentFactory</code>.
     *
     * @param context the layout context.
     */
    public TableComponentFactory(LayoutContext context) {
        super(context, new TableLayoutStrategyFactory());
    }

    /**
     * Returns a component to display a lookup property.
     *
     * @param property the lookup property
     * @param context  the context object
     * @return a component to display the lookup property
     */
    protected Component getLookup(Property property, IMObject context) {
        Label result = LabelFactory.create();
        result.setText(getLookupName(property, context));
        return result;
    }

    /**
     * Returns a component to display a string property.
     *
     * @param property the boolean property
     * @return a component to display the property
     */
    @Override
    protected Component getString(Property property) {
        return getLabel(property);
    }

    /**
     * Returns a component to display a numeric property.
     *
     * @param property the numeric property
     * @return a component to display the property
     */
    protected Component getNumber(Property property) {
        String value = getNumericValue(property);
        Label label = LabelFactory.create();
        label.setText(value);
        TableLayoutData layout = new TableLayoutData();
        Alignment right = new Alignment(Alignment.RIGHT,
                                        Alignment.DEFAULT);
        layout.setAlignment(right);
        label.setLayoutData(layout);
        return label;
    }

    /**
     * Returns a component to display a date property.
     *
     * @param property the date property
     * @return a component to display the property
     */
    protected Component getDate(Property property) {
        String value = getDateValue(property);
        Label label = LabelFactory.create();
        label.setText(value);
        return label;
    }

    /**
     * Helper to convert a numeric property to string.
     *
     * @param property the numeric property
     * @return the string value of the property associated with
     *         <code>property</code>
     */
    protected String getNumericValue(Property property) {
        Object tmp = property.getValue();
        Number value;
        if (tmp instanceof String) {
            value = new BigDecimal((String) tmp);
        } else {
            value = (Number) tmp;
        }
        if (value != null) {
            return NumberFormatter.format(value, property.getDescriptor(),
                                          false);
        }
        return null;
    }

    /**
     * Helper to convert a date value to a string.
     *
     * @param property the date property
     * @return the string value of the property
     */
    protected String getDateValue(Property property) {
        Date value = (Date) property.getValue();
        return (value != null) ? DateFormatter.formatDate(value, false) : null;
    }

}
