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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.LabelFactory;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractReadOnlyComponentFactory
        extends AbstractIMObjectComponentFactory {


    /**
     * Construct a new <code>AbstractReadOnlyComponentFactory</code>.
     *
     * @param context the layout context.
     */
    public AbstractReadOnlyComponentFactory(LayoutContext context) {
        super(context);
    }

    /**
     * Create a component to display a property.
     *
     * @param property the property to display
     * @param context  the context object
     * @return a component to display <code>object</code>
     */
    public Component create(Property property, IMObject context) {
        Component result;
        boolean enable = false;
        NodeDescriptor descriptor = property.getDescriptor();
        if (descriptor.isLookup()) {
            result = getLookup(property);
        } else if (descriptor.isBoolean()) {
            result = getBoolean(property);
        } else if (descriptor.isString()) {
            result = getTextComponent(property);
        } else if (descriptor.isNumeric()) {
            result = getNumber(property);
        } else if (descriptor.isDate()) {
            result = getDate(property);
        } else if (descriptor.isCollection()) {
            result = getCollectionViewer((CollectionProperty) property, context);
            // need to enable this otherwise table selection is disabled
            enable = true;
        } else if (descriptor.isObjectReference()) {
            result = getObjectViewer(property);
            // need to enable this for hyperlinks to work
            enable = true;
        } else {
            Label label = LabelFactory.create();
            label.setText("No viewer for type " + descriptor.getType());
            result = label;
        }
        result.setEnabled(enable);
        result.setFocusTraversalParticipant(false);
        return result;
    }

    /**
     * Create a component to display an object.
     *
     * @param object     the object to display
     * @param context    the object's parent. May be <code>null</code>
     * @param descriptor the parent object's descriptor. May be
     *                   <code>null</code>
     */
    public Component create(IMObject object, IMObject context,
                            NodeDescriptor descriptor) {
        IMObjectViewer viewer = new IMObjectViewer(object);
        return viewer.getComponent();
    }

    /**
     * Returns a component to display a lookup property.
     *
     * @param property the lookup property
     * @return a component to display the property
     */
    protected abstract Component getLookup(Property property);

    /**
     * Returns a component to display a boolean property.
     *
     * @param property the boolean property
     * @return a componnet to display the property
     */
    protected Component getBoolean(Property property) {
        return getCheckBox(property);
    }

    /**
     * Returns a component to display a number property.
     *
     * @param property the number property
     * @return a component to display the property
     */
    protected abstract Component getNumber(Property property);

    /**
     * Returns a component to display a date property.
     *
     * @param property the date property
     * @return a component to display the property
     */
    protected abstract Component getDate(Property property);

    /**
     * Returns a viewer for an object reference.
     *
     * @param property the object reference property
     * @return an component to display the object reference.
     */
    protected Component getObjectViewer(Property property) {
        IMObjectReference ref = (IMObjectReference) property.getValue();
        boolean link = true;
        if (getLayoutContext().isEdit()) {
            // disable hyperlinks if an edit is in progress.
            link = false;
        }
        return new IMObjectReferenceViewer(ref, link).getComponent();
    }

    /**
     * Returns a component to display a collection.
     *
     * @param parent the parent object
     * @return a collection to display the node
     */
    protected Component getCollectionViewer(CollectionProperty property,
                                            IMObject parent) {
        return new CollectionViewer(property, parent);
    }

}
