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

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditorFactory;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.GridFactory;

import java.util.List;


/**
 * An editor for relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-17 01:10:40Z $
 */
public abstract class AbstractRelationshipEditor
        extends AbstractIMObjectEditor {

    /**
     * Editor for the source of the relationship.
     */
    private Entity _source;

    /**
     * Editor for the target of the relationship.
     */
    private Entity _target;


    /**
     * Construct a new <code>EntityRelationshipEditor</code>.
     *
     * @param relationship  the relationship
     * @param parent        the parent object
     * @param layoutContext the layout context
     */
    public AbstractRelationshipEditor(IMObject relationship,
                                      IMObject parent,
                                      LayoutContext layoutContext) {
        super(relationship, parent, layoutContext);
        Context context = layoutContext.getContext();
        IMObjectReference sourceRef;
        IMObjectReference targetRef;
        IMObject source;
        IMObject target;

        Property sourceProp = getSource();
        Property targetProp = getTarget();

        sourceRef = (IMObjectReference) sourceProp.getValue();
        targetRef = (IMObjectReference) targetProp.getValue();

        source = IMObjectHelper.getObject(sourceRef,
                                          sourceProp.getDescriptor(), context);
        target = IMObjectHelper.getObject(targetRef,
                                          targetProp.getDescriptor(), context);

        // initialise the properties if null
        if (sourceRef == null && source != null) {
            sourceProp.setValue(source.getObjectReference());
        }
        if (targetRef == null && target != null) {
            targetProp.setValue(target.getObjectReference());
        }

        IMObject edited = layoutContext.getContext().getCurrent();
        boolean srcReadOnly = true;
        if (source == null || !source.equals(edited)) {
            srcReadOnly = false;
        }

        _source = new Entity(sourceProp, srcReadOnly, layoutContext);

        boolean targetReadOnly = true;
        if (target == null || !target.equals(edited) || target.equals(source)) {
            targetReadOnly = false;
        }

        _target = new Entity(targetProp, targetReadOnly, layoutContext);
    }

    /**
     * Returns the source property.
     *
     * @return the source property
     */
    protected Property getSource() {
        return getProperty("source");
    }

    /**
     * Returns the target property.
     *
     * @return the target property
     */
    protected Property getTarget() {
        return getProperty("target");
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    /**
     * Creates a new reference editor.
     *
     * @param property the reference property
     * @param context  the layout context
     * @return a new reference editor
     */
    protected IMObjectReferenceEditor createReferenceEditor(
            Property property, LayoutContext context) {
        IMObjectReferenceEditor editor = IMObjectReferenceEditorFactory.create(
                property, context);
        editor.setAllowCreate(true);
        return editor;
    }

    /**
     * Relationship layout strategy. Displays the source and target nodes
     * before any others.
     */
    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Returns a node filter to filter nodes. This implementation filters
         * the "source" and "target" nodes.
         *
         * @param context the context
         * @return a node filter to filter nodes
         */
        @Override
        protected NodeFilter getNodeFilter(LayoutContext context) {
            NodeFilter filter = new NamedNodeFilter("source", "target");
            return getNodeFilter(context, filter);
        }

        /**
         * Lays out child components in a 2x2 grid.
         *
         * @param object      the parent object
         * @param descriptors the property descriptors
         * @param properties  the properties
         * @param container   the container to use
         * @param context     the layout context
         */
        @Override
        protected void doSimpleLayout(IMObject object,
                                      List<NodeDescriptor> descriptors,
                                      PropertySet properties,
                                      Component container,
                                      LayoutContext context) {
            Grid grid = GridFactory.create(4);
            add(grid, _source.getProperty(), _source.getComponent(), context);
            add(grid, _target.getProperty(), _target.getComponent(), context);
            doGridLayout(object, descriptors, properties, grid, context);
            container.add(grid);
        }

    }

    /**
     * Editor for a source/target entity in a relationship.
     */
    private class Entity extends AbstractPropertyEditor {

        /**
         * The viewer.
         */
        private IMObjectReferenceViewer _viewer;

        /**
         * The editor.
         */
        private IMObjectReferenceEditor _editor;

        /**
         * Construct a new <code>Entity</code>.
         *
         * @param property the reference property
         * @param readOnly if <code>true<code> don't render the select button
         * @param context  the layout context
         */
        public Entity(Property property, boolean readOnly,
                      LayoutContext context) {
            super(property);
            if (readOnly) {
                IMObjectReference ref = (IMObjectReference) property.getValue();
                _viewer = new IMObjectReferenceViewer(ref, false);
            } else {
                _editor = createReferenceEditor(property, context);
            }
        }

        /**
         * Sets the value of the reference to the supplied object.
         *
         * @param object the object. May  be <code>null</code>
         */
        public void setObject(IMObject object) {
            if (_editor != null) {
                _editor.setObject(object);
            }
        }

        /**
         * Returns the edit component.
         *
         * @return the edit component
         */
        public Component getComponent() {
            return (_editor != null) ? _editor.getComponent() :
                    _viewer.getComponent();
        }

    }

}
