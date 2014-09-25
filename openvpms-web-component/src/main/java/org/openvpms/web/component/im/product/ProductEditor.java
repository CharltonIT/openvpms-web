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

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.product.ProductPriceUpdater;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.MultipleEntityRelationshipCollectionEditor;
import org.openvpms.web.component.im.relationship.RelationshipCollectionEditor;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * A {@link Product} editor that recalculates prices when
 * {@link ProductSupplier} relationships change.
 *
 * @author Tim Anderson
 */
public class ProductEditor extends AbstractIMObjectEditor {

    /**
     * The product price updater.
     */
    private ProductPriceUpdater updater;

    /**
     * Constructs a {@link ProductEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context. May be {@code null}.
     */
    public ProductEditor(Product object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        CollectionProperty suppliers = getCollectionProperty("suppliers");
        CollectionProperty stock = getCollectionProperty("stockLocations");
        if (suppliers != null && stock != null) {
            RelationshipCollectionEditor stockLocations
                    = new MultipleEntityRelationshipCollectionEditor(stock, object, getLayoutContext()) {
                @Override
                protected IMObjectEditor createEditor(IMObject object, LayoutContext context) {
                    IMObjectEditor editor = super.createEditor(object, context);
                    if (editor instanceof ProductStockLocationEditor) {
                        ((ProductStockLocationEditor) editor).setProductEditor(ProductEditor.this);
                    }
                    return editor;
                }
            };
            getEditors().add(stockLocations);
        }
        updater = new ProductPriceUpdater(ServiceHelper.getCurrencies(), ServiceHelper.getArchetypeService(),
                                          ServiceHelper.getLookupService());
    }

    /**
     * Returns the product supplier references.
     *
     * @return the product supplier references
     */
    public List<IMObjectReference> getSuppliers() {
        List<IMObjectReference> result = new ArrayList<IMObjectReference>();
        IMObjectCollectionEditor suppliers = (IMObjectCollectionEditor) getEditor("suppliers");
        if (suppliers != null) {
            for (IMObjectEditor editor : suppliers.getEditors()) {
                IMObjectReference target = editor.getProperty("target").getReference();
                if (target != null) {
                    result.add(target);
                }
            }
        }
        return result;
    }

    /**
     * Invoked when layout has completed. This can be used to perform
     * processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        IMObjectCollectionEditor editor = (IMObjectCollectionEditor) getEditor("suppliers");
        if (editor != null) {
            editor.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onSupplierChanged();
                }
            });
        }
    }

    /**
     * Invoked when a product-supplier relationship changes. This recalculates product prices if required.
     */
    private void onSupplierChanged() {
        IMObjectCollectionEditor suppliers = (IMObjectCollectionEditor) getEditor("suppliers");
        IMObjectCollectionEditor prices = (IMObjectCollectionEditor) getEditor("prices");
        Collection<IMObjectEditor> currentPrices = prices.getEditors();
        Collection<IMObjectEditor> editors = suppliers.getEditors();
        for (IMObjectEditor editor : editors) {
            EntityRelationship rel = (EntityRelationship) editor.getObject();
            ProductSupplier ps = new ProductSupplier(rel);
            List<ProductPrice> updated = updater.update((Product) getObject(), ps, false);
            for (ProductPrice price : updated) {
                updatePriceEditor(price, currentPrices);
            }
        }
        prices.refresh();
    }

    /**
     * Refreshes the price editor associated with a product price.
     *
     * @param price   the price
     * @param editors the price editors
     */
    private void updatePriceEditor(ProductPrice price, Collection<IMObjectEditor> editors) {
        for (IMObjectEditor editor : editors) {
            if (editor.getObject().equals(price) && (editor instanceof ProductPriceEditor)) {
                ((ProductPriceEditor) editor).refresh();
                break;
            }
        }
    }

}
