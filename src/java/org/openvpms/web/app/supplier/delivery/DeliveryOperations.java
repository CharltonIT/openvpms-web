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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.supplier.delivery;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.im.edit.ActOperations;


/**
 * Determines the operations that may be performed on <em>act.supplierDelivery</em> acts.
 *
 * @author Tim Anderson
 */
public class DeliveryOperations extends ActOperations<FinancialAct> {

    /**
     * The singleton instance.
     */
    public static final DeliveryOperations INSTANCE = new DeliveryOperations();


    /**
     * Default constructor.
     */
    private DeliveryOperations() {
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the delivery to check
     * @return {@code true} if the delivery status is <em>IN_PROGRESS</em>
     */
    @Override
    public boolean canEdit(FinancialAct act) {
        return ActStatus.IN_PROGRESS.equals(act.getStatus());
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the delivery to check
     * @return {@code true} if the act status is <em>IN_PROGRESS</em>
     */
    @Override
    public boolean canDelete(FinancialAct act) {
        return ActStatus.IN_PROGRESS.equals(act.getStatus());
    }

}
