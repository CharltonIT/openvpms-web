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

package org.openvpms.web.app.financial;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.app.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.im.view.act.ActRelationshipCollectionViewer;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;


/**
 * CRUD window for the financial workspace. This provides selection and event
 * notification for child acts and print support.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FinancialActCRUDWindow
        extends AbstractViewCRUDWindow<FinancialAct> {

    /**
     * Create a new <code>FinancialActCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public FinancialActCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Creates a new {@link IMObjectViewer} for an object.
     *
     * @param object the object to view
     */
    @Override
    protected IMObjectViewer createViewer(IMObject object) {
        return new IMObjectViewer(object, null, new LayoutStrategy(), null);
    }

    /**
     * Invoked when a child act is selected/deselected.
     *
     * @param child the child act. May be <code>null</code>
     */
    protected void onChildActSelected(FinancialAct child) {
    }

    /**
     * Layout strategy that creates an {@link Viewer} for the items node.
     */
    private class LayoutStrategy extends ActLayoutStrategy {

        /**
         * Creates a component for the items node.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display <code>property</code>
         */
        @Override
        protected ComponentState createItems(Property property, IMObject parent,
                                             LayoutContext context) {
            Viewer viewer = new Viewer((CollectionProperty) property,
                                       parent, context);
            return new ComponentState(viewer.getComponent(), property);
        }
    }

    /**
     * Viewer that invokes <em>onChildActSelected()</em> when an act is
     * selected.
     */
    private class Viewer extends ActRelationshipCollectionViewer {

        /**
         * Construct a new <code>Viewer</code>.
         *
         * @param property the collection to view
         * @param parent   the parent object
         * @param context  the layout context. May be <tt>null</tt>
         */
        public Viewer(CollectionProperty property, IMObject parent,
                      LayoutContext context) {
            super(property, parent, context);
        }

        /**
         * Browse an act.
         *
         * @param act the act to browse
         */
        @Override
        protected void browse(Act act) {
            onChildActSelected((FinancialAct) act);
            super.browse(act);
        }

    }
}
