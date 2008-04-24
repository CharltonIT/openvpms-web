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

package org.openvpms.web.component.im.edit.order;

import nextapp.echo2.app.text.TextComponent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.DeliveryStatus;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.LookupFilter;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.im.util.FastLookupHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.DelegatingProperty;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;
import java.util.List;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.supplierOrder</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class OrderEditor extends ActEditor {

    /**
     * Determines if the act was posted at construction. If so, only a limited
     * set of properties may be edited.
     */
    private final boolean posted;

    /**
     * Order business rules.
     */
    private final OrderRules rules;

    /**
     * Delivery status field.
     */
    private TextComponent deliveryStatusField;


    /**
     * Construct a new <tt>OrderEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context
     */
    public OrderEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.supplierOrder")) {
            throw new IllegalArgumentException(
                    "Invalid act type: " + act.getArchetypeId().getShortName());
        }
        posted = ActStatus.POSTED.equals(act.getStatus());
        rules = new OrderRules();
    }

    /**
     * Updates totals when an act item changes.
     */
    protected void onItemsChanged() {
        Property total = getProperty("amount");
        List<Act> acts = getEditor().getCurrentActs();
        BigDecimal value = ActHelper.sum((Act) getObject(), acts, "total");
        total.setValue(value);
        checkDeliveryStatus(acts);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy(getEditor());
    }

    /**
     * Checks if the delivery status needs to be updated.
     *
     * @param acts the current order item acts
     */
    private void checkDeliveryStatus(List<Act> acts) {
        Property deliveryStatus = getProperty("deliveryStatus");
        DeliveryStatus current
                = DeliveryStatus.valueOf((String) deliveryStatus.getValue());
        DeliveryStatus newStatus = null;
        for (Act act : acts) {
            FinancialAct item = (FinancialAct) act;
            DeliveryStatus status = rules.getDeliveryStatus(item);
            if (newStatus == null) {
                newStatus = status;
            } else if (status == DeliveryStatus.PART) {
                newStatus = status;
            } else if (status == DeliveryStatus.PENDING
                    && newStatus != DeliveryStatus.PART) {
                newStatus = status;
            }
        }
        if (newStatus != null && newStatus != current) {
            deliveryStatus.setValue(newStatus.toString());
            NodeDescriptor descriptor = deliveryStatus.getDescriptor();
            if (descriptor != null) {
                deliveryStatusField.setText(FastLookupHelper.getLookupName(
                        descriptor, getObject()));
            }
        }
    }

    private class LayoutStrategy extends ActLayoutStrategy {

        /**
         * Creates a new <tt>NonPostedLayoutStrategy</tt>.
         *
         * @param editor the act items editor
         */
        public LayoutStrategy(IMObjectCollectionEditor editor) {
            super(editor);
            if (posted) {
                editor.setCardinalityReadOnly(true);
            }
        }

        @Override
        protected ComponentState createComponent(Property property,
                                                 IMObject parent,
                                                 LayoutContext context) {
            ComponentState state;
            if (property.getName().equals("deliveryStatus")) {
                property = createReadOnly(property);
                state = super.createComponent(property, parent, context);
                deliveryStatusField = (TextComponent) state.getComponent();
            } else if (posted) {
                if (property.getName().equals("status")) {
                    LookupQuery query = new NodeLookupQuery(
                            parent, property.getDescriptor());
                    query = new LookupFilter(query, true, "POSTED",
                                             "CANCELLED");
                    LookupField field = LookupFieldFactory.create(property,
                                                                  query);
                    state = new ComponentState(field, property);
                } else {
                    // all other properties are read-only
                    property = createReadOnly(property);
                    state = super.createComponent(property, parent, context);
                }
            } else {
                state = super.createComponent(property, parent, context);
            }
            return state;
        }

        private Property createReadOnly(Property property) {
            property = new DelegatingProperty(property) {
                @Override
                public boolean isReadOnly() {
                    return true;
                }
            };
            return property;
        }
    }

}
