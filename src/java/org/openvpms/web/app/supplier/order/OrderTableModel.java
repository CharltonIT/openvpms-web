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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier.order;

import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;


/**
 * Table model for <em>act.supplierOrder</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderTableModel extends AbstractActTableModel {

    /**
     * The nodes to display.
     */
    private static final String[] NODES
            = new String[]{"startTime", "supplier", "stockLocation", "status",
                           "deliveryStatus", "amount", "title"};


    /**
     * Creates a new <tt>OrderTableModel</tt>.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context. May be <tt>null</tt>
     */
    public OrderTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
    }

    /**
     * Returns a list of descriptor names to include in the table.
     * This implementation returns <code>null</code> to indicate that the
     * intersection should be calculated from all descriptors.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getDescriptorNames() {
        return NODES;
    }
}
