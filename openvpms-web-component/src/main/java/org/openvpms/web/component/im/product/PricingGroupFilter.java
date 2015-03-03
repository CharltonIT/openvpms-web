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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;

import java.util.List;

/**
 * Filters prices based on the selected pricing group.
 *
 * @author Tim Anderson
 */
public class PricingGroupFilter {

    /**
     * The selected pricing group.
     */
    private PricingGroup pricingGroup;

    /**
     * Determines if filtering is required.
     */
    private boolean needsFilter;

    /**
     * Determines if prices for all pricing locations should be displayed.
     */
    private boolean showAll;

    /**
     * Listener to notify
     */
    private ActionListener listener;


    /**
     * Constructs a {@link PricingGroupFilter}.
     *
     * @param context the layout context
     */
    public PricingGroupFilter(LayoutContext context) {
        pricingGroup = PricingGroupHelper.getPricingGroup(context.getContext());
        needsFilter = PricingGroupHelper.pricingGroupsConfigured();
    }

    /**
     * Determines if prices need to be filtered.
     *
     * @return {@code true} if prices need to be filtered by pricing location
     */
    public boolean needsFilter() {
        return needsFilter;
    }

    /**
     * Returns the selected pricing group.
     *
     * @return the selected pricing group. May be {@code null}
     */
    public PricingGroup getPricingGroup() {
        return pricingGroup;
    }

    /**
     * Registers a listener to be notified when the pricing location changes.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    /**
     * Filters the prices according to the selected pricing group.
     *
     * @param prices the prices to filter
     * @return the filtered prices
     */
    public List<IMObject> getPrices(List<IMObject> prices) {
        if (!showAll) {
            return PricingGroupHelper.filterPrices(prices, pricingGroup);
        }
        return prices;
    }

    /**
     * Returns the filter component.
     *
     * @return the filter component
     */
    public Component getComponent() {
        final PricingGroupSelectField filter = new PricingGroupSelectField(pricingGroup, true);
        filter.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                pricingGroup = filter.getSelected();
                showAll = filter.isAllSelected();
                if (listener != null) {
                    listener.onAction(new ActionEvent(this, null));
                }
            }
        });
        return RowFactory.create(Styles.CELL_SPACING, LabelFactory.create("product.pricingGroup"), filter);
    }

}
