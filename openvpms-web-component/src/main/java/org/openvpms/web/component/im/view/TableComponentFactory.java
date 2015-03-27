/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.layout.TableLayoutStrategyFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.NumericPropertyFormatter;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.math.BigDecimal;
import java.util.Date;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components for
 * display in a table.
 *
 * @author Tim Anderson
 */
public class TableComponentFactory extends AbstractReadOnlyComponentFactory {

    /**
     * Construct a new <tt>TableComponentFactory</tt>.
     *
     * @param context the layout context.
     */
    public TableComponentFactory(LayoutContext context) {
        super(context, new TableLayoutStrategyFactory(), Styles.DEFAULT);
    }

    /**
     * Returns a component to display a lookup property.
     *
     * @param property the lookup property
     * @param context  the context object
     * @return a component to display the lookup property
     */
    @Override
    protected Component createLookup(Property property, IMObject context) {
        Label result = LabelFactory.create();
        result.setText(LookupNameHelper.getName(context, property.getName()));
        result.setEnabled(false);
        return result;
    }

    /**
     * Returns a component to display a string property.
     *
     * @param property the boolean property
     * @return a component to display the property
     */
    @Override
    protected Component createString(Property property) {
        return createLabel(property);
    }

    /**
     * Returns a component to display a numeric property.
     *
     * @param property the numeric property
     * @return a component to display the property
     */
    @Override
    protected Component createNumeric(Property property) {
        String value = getNumericValue(property);
        return TableHelper.rightAlign(value);
    }

    /**
     * Returns a component to display a date property.
     *
     * @param property the date property
     * @return a component to display the property
     */
    @Override
    protected Component createDate(Property property) {
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
     *         <tt>property</tt>
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
            return NumericPropertyFormatter.format(value, property, false);
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

    /**
     * Returns a component for an empty collection.
     *
     * @return the component
     */
    @Override
    protected Component getEmptyCollectionViewer() {
        return LabelFactory.create();
    }
}
