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

package org.openvpms.web.workspace.patient.estimate;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.workspace.customer.estimate.EstimateActRelationshipCollectionEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Estimate item collection editor for visits.
 *
 * @author Tim Anderson
 */
public class VisitEstimateItemRelationshipCollectionEditor extends EstimateActRelationshipCollectionEditor {

    /**
     * Constructs a {@link VisitEstimateItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public VisitEstimateItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);
    }

    /**
     * Returns the current patient.
     *
     * @return the current patient. May be {@code null}
     */
    public Party getPatient() {
        return getContext().getContext().getPatient();
    }

    /**
     * Returns the acts for the current patient.
     *
     * @return the patient's acts
     */
    public List<Act> getPatientActs() {
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        List<IMObject> objects = editor.getObjects();
        List<Act> acts = new ArrayList<Act>();
        Party patient = getPatient();
        if (patient != null) {
            IMObjectReference patientRef = patient.getObjectReference();
            for (IMObject object : objects) {
                Act act = (Act) object;
                ActBean bean = new ActBean(act);
                if (ObjectUtils.equals(patientRef, bean.getNodeParticipantRef("patient"))) {
                    acts.add(act);
                }
            }
        }
        return acts;
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    @Override
    @SuppressWarnings("unchecked")
    protected ResultSet<IMObject> createResultSet() {
        List acts = getPatientActs();
        ResultSet<IMObject> set = new IMObjectListResultSet<IMObject>(acts, ROWS);
        set.sort(new SortConstraint[]{new NodeSortConstraint("startTime", false)});
        return set;
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        VisitEstimateItemEditor editor = new VisitEstimateItemEditor((Act) object, (Act) getObject(), context);
        editor.setProductListener(getProductListener());
        return editor;
    }

}
