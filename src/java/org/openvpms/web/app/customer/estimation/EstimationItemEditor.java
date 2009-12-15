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

package org.openvpms.web.app.customer.estimation;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.customer.PriceActItemEditor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;

import java.math.BigDecimal;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerEstimationItem</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class EstimationItemEditor extends PriceActItemEditor {

    /**
     * Low quantity selling units.
     */
    private Label lowQtySellingUnits = LabelFactory.create();

    /**
     * High quantity selling units.
     */
    private Label highQtySellingUnits = LabelFactory.create();

    /**
     * Node filter, used to disable properties when a product template is
     * selected.
     */
    private static final NodeFilter TEMPLATE_FILTER = new NamedNodeFilter(
            "lowQty", "highQty", "fixedPrice", "lowUnitPrice", "highUnitPrice",
            "lowTotal", "highTotal");


    /**
     * Construct a new <tt>EstimationItemEdtor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public EstimationItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.customerEstimationItem")) {
            throw new IllegalArgumentException(
                    "Invalid act type:" + act.getArchetypeId().getShortName());
        }

        // add a listener to update the discount when the fixed, high unit price
        // or quantity, changes
        ModifiableListener listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateDiscount();
            }
        };
        getProperty("fixedPrice").addModifiableListener(listener);
        getProperty("highUnitPrice").addModifiableListener(listener);
        getProperty("highQty").addModifiableListener(listener);
    }

    /**
     * Sets the product quantity.
     *
     * @param quantity the product quantity
     */
    public void setQuantity(BigDecimal quantity) {
        getProperty("lowQty").setValue(quantity);
        getProperty("highQty").setValue(quantity);
    }

    /**
     * Invoked when the product is changed, to update prices.
     *
     * @param product the product. May be <tt>null</tt>
     */
    @Override
    protected void productModified(Product product) {
        super.productModified(product);

        Property discount = getProperty("discount");
        discount.setValue(BigDecimal.ZERO);

        if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            if (getFilter() != TEMPLATE_FILTER) {
                changeLayout(TEMPLATE_FILTER);
            }
            // zero out the fixed, low and high prices.
            Property fixedPrice = getProperty("fixedPrice");
            Property lowUnitPrice = getProperty("lowUnitPrice");
            Property highUnitPrice = getProperty("highUnitPrice");
            fixedPrice.setValue(BigDecimal.ZERO);
            lowUnitPrice.setValue(BigDecimal.ZERO);
            highUnitPrice.setValue(BigDecimal.ZERO);
            updateSellingUnits(null);
        } else {
            if (getFilter() != null) {
                changeLayout(null);
            }
            Property fixedPrice = getProperty("fixedPrice");
            Property lowUnitPrice = getProperty("lowUnitPrice");
            Property highUnitPrice = getProperty("highUnitPrice");
            ProductPrice fixed = null;
            ProductPrice unit = null;
            if (product != null) {
                fixed = getDefaultFixedProductPrice(product);
                unit = getDefaultUnitProductPrice(product);
            }

            if (fixed != null) {
                fixedPrice.setValue(fixed.getPrice());
            } else {
                fixedPrice.setValue(BigDecimal.ZERO);
            }
            if (unit != null) {
                lowUnitPrice.setValue(unit.getPrice());
                highUnitPrice.setValue(unit.getPrice());
            } else {
                lowUnitPrice.setValue(BigDecimal.ZERO);
                highUnitPrice.setValue(BigDecimal.ZERO);
            }
            updateSellingUnits(product);
        }
    }

    /**
     * Returns the unit price.
     * <p/>
     * This implememntation returns the high unit price.
     *
     * @return the unit price
     */
    @Override
    protected BigDecimal getUnitPrice() {
        BigDecimal value = (BigDecimal) getProperty("highUnitPrice").getValue();
        return (value != null) ? value : BigDecimal.ZERO;
    }

    /**
     * Returns the quantity.
     * <p/>
     * This implememntation returns the high quantity.
     *
     * @return the quantity
     */
    @Override
    protected BigDecimal getQuantity() {
        BigDecimal value = (BigDecimal) getProperty("highQty").getValue();
        return (value != null) ? value : BigDecimal.ZERO;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PriceItemLayoutStrategy() {
            @Override
            protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
                ComponentState state = super.createComponent(property, parent, context);
                if ("lowQty".equals(property.getName())) {
                    Component component = RowFactory.create("CellSpacing", state.getComponent(), lowQtySellingUnits);
                    state = new ComponentState(component, property);
                } else if ("highQty".equals(property.getName())) {
                    Component component = RowFactory.create("CellSpacing", state.getComponent(), highQtySellingUnits);
                    state = new ComponentState(component, property);
                }
                return state;
            }
        };
    }

    /**
     * Updates the selling units label.
     *
     * @param product the product. May be <tt>null</tt>
     */
    private void updateSellingUnits(Product product) {
        String units = "";
        if (product != null) {
            units = LookupNameHelper.getName(product, "sellingUnits");
        }
        lowQtySellingUnits.setText(units);
        highQtySellingUnits.setText(units);
    }

}
