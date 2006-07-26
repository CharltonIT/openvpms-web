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

package org.openvpms.web.component.im.lookup;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.GridFactory;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;


/**
 * {@link LookupRelationship} layout strategy.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupRelationshipLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Lay out out the object in the specified container.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param container  the container to use
     * @param context    the layout context
     */
    protected void doLayout(IMObject object, PropertySet properties,
                            Component container, LayoutContext context) {
        Property source = properties.get("source");
        Property target = properties.get("target");
        IMObjectReference srcRef = (IMObjectReference) source.getValue();
        IMObjectReference tgtRef = (IMObjectReference) target.getValue();
        boolean srcLink = false;
        boolean tgtLink = false;

        IMObject current = Context.getInstance().getCurrent();
        if (current == null) {
            srcLink = true;
            tgtLink = true;
        } else {
            IMObjectReference currRef = current.getObjectReference();
            if (srcRef != null && !srcRef.equals(currRef)) {
                srcLink = true;
            }
            if (tgtRef != null && !tgtRef.equals(currRef)) {
                tgtLink = true;
            }
        }

        IMObjectReferenceViewer sourceView
                = new IMObjectReferenceViewer(srcRef, srcLink);

        IMObjectReferenceViewer targetView
                = new IMObjectReferenceViewer(tgtRef, tgtLink);

        Grid grid = GridFactory.create(4);
        add(grid, source, sourceView.getComponent(), context);
        add(grid, target, targetView.getComponent(), context);
        container.add(grid);
    }

}