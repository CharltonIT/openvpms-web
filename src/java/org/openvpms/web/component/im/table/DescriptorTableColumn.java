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

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.table.TableColumn;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Table column associated with a {@link NodeDescriptor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 * @see DescriptorTableModel
 */
public class DescriptorTableColumn extends TableColumn {

    /**
     * The node descriptor.
     */

    private NodeDescriptor _descriptor;

    /**
     * Construct a new <code>DescriptorTableColumn</code> with the specified
     * model index,undefined width, and undefined cell and header renderers.
     *
     * @param modelIndex the column index of model data visualized by this
     *                   column
     * @param descriptor the node descriptor
     */
    public DescriptorTableColumn(int modelIndex, NodeDescriptor descriptor) {
        super(modelIndex);
        _descriptor = descriptor;
    }

    /**
     * Returns the header value for this column.  The header value is the object
     * that will be provided to the header renderer to produce a component that
     * will be used as the table header for this column.
     *
     * @return the header value for this column
     */
    @Override
    public Object getHeaderValue() {
        return _descriptor.getDisplayName();
    }

    /**
     * Returns the value of the cell.
     *
     * @param context the context
     * @return the value of the cell
     */
    public Object getValue(IMObject context) {
        return _descriptor.getValue(context);
    }

    /**
     * Returns the descriptor.
     *
     * @return the descriptor
     */
    public NodeDescriptor getDescriptor() {
        return _descriptor;
    }

}
