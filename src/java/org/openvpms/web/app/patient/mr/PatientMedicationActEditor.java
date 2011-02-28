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

package org.openvpms.web.app.patient.mr;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.edit.act.PatientActEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.TitledTextArea;

import java.math.BigDecimal;


/**
 * Editor for <em>act.patientMedication</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientMedicationActEditor extends PatientActEditor {

    /**
     * Dispensing units label.
     */
    private Label dispensingUnits;

    /**
     * Usage notes text.
     */
    private TitledTextArea usageNotes;

    /**
     * Determines if the product node should be displayed read-only.
     */
    private boolean showProductReadOnly = false;


    /**
     * Constructs a <tt>PatientMedicationActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
     */
    public PatientMedicationActEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, PatientArchetypes.PATIENT_MEDICATION)) {
            throw new IllegalArgumentException("Invalid act type:" + act.getArchetypeId().getShortName());
        }

        dispensingUnits = LabelFactory.create();
        String displayName = DescriptorHelper.getDisplayName(ProductArchetypes.MEDICATION, "usageNotes");
        usageNotes = new TitledTextArea(displayName);
        usageNotes.setEnabled(false);

        if (parent != null) {
            ActBean bean = new ActBean(parent);
            if (bean.hasNode("product")) {
                // update the product from the parent
                Product product = (Product) bean.getParticipant(ProductArchetypes.PRODUCT_PARTICIPATION);
                if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
                    setProduct(product);
                    if (bean.hasNode("quantity")) {
                        setQuantity(bean.getBigDecimal("quantity"));
                    }
                } else {
                    setProduct(null);
                }
            }
        } else {
            Product product = getProduct();
            updateDispensingUnits(product);
            updateUsageNotes(product);
        }
    }

    /**
     * Updates the product.
     *
     * @param product the product. May be <tt>null</tt>
     */
    public void setProduct(Product product) {
        if (setParticipant("product", product)) {
            if (getProductEditor() == null) {
                productModified(product); // only invoke if the product participation changed
            }
        }
    }

    /**
     * Returns the product.
     *
     * @return the product. May be <tt>null</tt>
     */
    public Product getProduct() {
        return (Product) getParticipant("product");
    }

    /**
     * Sets the quantity.
     *
     * @param quantity the quantity
     */
    public void setQuantity(BigDecimal quantity) {
        getProperty("quantity").setValue(quantity);
    }

    /**
     * Determines if the product should be displayed read-only.
     *
     * @param readOnly if <tt>true</tt> display the product read-only.
     */
    public void setProductReadOnly(boolean readOnly) {
        showProductReadOnly = readOnly;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        PatientMedicationActLayoutStrategy strategy = new PatientMedicationActLayoutStrategy() {
            @Override
            protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
                ComponentState state = super.createComponent(property, parent, context);
                if ("quantity".equals(property.getName())) {
                    Component component = RowFactory.create("CellSpacing", state.getComponent(), dispensingUnits);
                    state = new ComponentState(component, property);
                }
                return state;
            }
        };
        strategy.setProductReadOnly(showProductReadOnly);
        strategy.setUsageNotes(usageNotes);
        return strategy;
    }

    /**
     * Invoked when layout has completed. This can be used to perform
     * processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        final ProductParticipationEditor product = getProductEditor();
        if (product != null) {
            product.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    productModified(product.getEntity());
                }
            });
        }
        super.onLayoutCompleted();
    }

    /**
     * Invoked when the product is modified.
     *
     * @param product the product. May be <tt>null</tt>
     */
    private void productModified(Product product) {
        if (product != null) {
            IMObjectBean bean = new IMObjectBean(product);
            if (bean.hasNode("dispInstructions")) {
                Property label = getProperty("label");
                label.setValue(bean.getValue("dispInstructions"));
            }
        }
        updateDispensingUnits(product);
        updateUsageNotes(product);
    }

    /**
     * Updates the dispensing units label.
     *
     * @param product the product. May be <tt>null</tt>
     */
    private void updateDispensingUnits(Product product) {
        String units = "";
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            units = LookupNameHelper.getName(product, "dispensingUnits");
        }
        dispensingUnits.setText(units);
    }

    /**
     * Updates the usage notes.
     *
     * @param product the product. May be <tt>null</tt
     */
    private void updateUsageNotes(Product product) {
        String notes = "";
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            IMObjectBean bean = new IMObjectBean(product);
            notes = bean.getString("usageNotes");
        }
        usageNotes.setText(notes);
        usageNotes.setVisible(!StringUtils.isEmpty(notes));
    }

    /**
     * Returns the product editor.
     *
     * @return the product editor, or <tt>null</tt> if none exists
     */
    private ProductParticipationEditor getProductEditor() {
        ParticipationEditor<Product> editor = getParticipationEditor("product", false);
        return (ProductParticipationEditor) editor;
    }

}
