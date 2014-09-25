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

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.Component;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditorFactory;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCache;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;

import java.util.List;


/**
 * An editor for relationships.
 *
 * @author Tim Anderson
 */
public abstract class AbstractRelationshipEditor extends AbstractIMObjectEditor {

    /**
     * Editor for the source of the relationship. Null if the source is the parent.
     */
    private IMObjectReferenceEditor<Entity> sourceEditor;

    /**
     * Editor for the target of the relationship. Null if the target is the parent.
     */
    private IMObjectReferenceEditor<Entity> targetEditor;

    /**
     * The nodes to render.
     */
    private ArchetypeNodes nodes;

    /**
     * The source node name.
     */
    private static final String SOURCE = "source";

    /**
     * The target node name.
     */
    private static final String TARGET = "target";


    /**
     * Constructs an {@code AbstractRelationshipEditor}.
     *
     * @param relationship  the relationship
     * @param parent        the parent object
     * @param layoutContext the layout context
     */
    public AbstractRelationshipEditor(IMObject relationship, IMObject parent, LayoutContext layoutContext) {
        super(relationship, parent, layoutContext);
        IMObjectCache cache = layoutContext.getCache();
        Property sourceProp = getSource();
        Property targetProp = getTarget();

        IMObjectReference sourceRef = (IMObjectReference) sourceProp.getValue();
        IMObjectReference targetRef = (IMObjectReference) targetProp.getValue();

        IMObject source = getObject(sourceRef, parent, sourceProp.getArchetypeRange(), cache);
        IMObject target = getObject(targetRef, parent, targetProp.getArchetypeRange(), cache);

        // initialise the properties if null
        if (sourceRef == null && source != null) {
            sourceProp.setValue(source.getObjectReference());
        }
        if (targetRef == null && target != null && !ObjectUtils.equals(source, target)) {
            // only add the target if it is different to the source (e.g. see entityRelationship.productLink where the
            // source and target archetypes are the same)
            targetProp.setValue(target.getObjectReference());
        }

        nodes = new ArchetypeNodes();

        if (source == null || !source.equals(parent)) {
            sourceEditor = createSourceEditor(sourceProp, layoutContext);
            nodes.simple(SOURCE);
            nodes.first(SOURCE);
            nodes.exclude(TARGET);
        }

        if (target == null || !target.equals(parent) || target.equals(source)) {
            targetEditor = createTargetEditor(targetProp, layoutContext);
            nodes.simple(TARGET);
            nodes.first(TARGET);
            nodes.exclude(SOURCE);
        }
    }

    /**
     * Sets the source of the relationship.
     *
     * @param source the source. May be {@code null}
     */
    public void setSource(Entity source) {
        if (sourceEditor != null) {
            sourceEditor.setObject(source);
        } else {
            getSource().setValue(source != null ? source.getObjectReference() : null);
        }
    }

    /**
     * Sets the target of the relationship.
     *
     * @param target the target. May be {@code null}
     */
    public void setTarget(Entity target) {
        if (targetEditor != null) {
            targetEditor.setObject(target);
        } else {
            getTarget().setValue(target != null ? target.getObjectReference() : null);
        }
    }

    /**
     * Returns the source property.
     *
     * @return the source property
     */
    protected Property getSource() {
        return getProperty(SOURCE);
    }

    /**
     * Returns the target property.
     *
     * @return the target property
     */
    protected Property getTarget() {
        return getProperty(TARGET);
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

    protected IMObjectReferenceEditor<Entity> getSourceEditor() {
        return sourceEditor;
    }

    protected IMObjectReferenceEditor<Entity> getTargetEditor() {
        return targetEditor;
    }

    /**
     * Creates a new editor for the relationship source.
     *
     * @param property the source property
     * @param context  the layout context
     * @return a new reference editor
     */
    protected IMObjectReferenceEditor<Entity> createSourceEditor(Property property, LayoutContext context) {
        return createReferenceEditor(property, context);
    }

    /**
     * Creates a new editor for the relationship target.
     *
     * @param property the target property
     * @param context  the layout context
     * @return a new reference editor
     */
    protected IMObjectReferenceEditor<Entity> createTargetEditor(Property property, LayoutContext context) {
        return createReferenceEditor(property, context);
    }

    /**
     * Creates a new reference editor.
     *
     * @param property the reference property
     * @param context  the layout context
     * @return a new reference editor
     */
    protected IMObjectReferenceEditor<Entity> createReferenceEditor(Property property, LayoutContext context) {
        IMObjectReferenceEditor<Entity> editor = IMObjectReferenceEditorFactory.create(property, getObject(), context);
        editor.setAllowCreate(true);
        return editor;
    }

    /**
     * Returns the archetype nodes to display.
     *
     * @return the nodes
     */
    protected ArchetypeNodes getArchetypeNodes() {
        return nodes;
    }

    /**
     * Relationship layout strategy. Displays the source/target nodes before any others.
     * <p/>
     * If there is only a single node, this is rendered without any label.
     */
    protected class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Constructs a {@code LayoutStrategy}.
         */
        public LayoutStrategy() {
        }

        /**
         * Apply the layout strategy.
         * <p/>
         * This renders an object in a {@code Component}, using a factory to create the child components.
         *
         * @param object     the object to apply
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param context    the layout context
         * @return the component containing the rendered {@code object}
         */
        @Override
        public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
            if (sourceEditor != null) {
                addComponent(new ComponentState(sourceEditor));
            }
            if (targetEditor != null) {
                addComponent(new ComponentState(targetEditor));
            }
            return super.apply(object, properties, parent, context);
        }

        /**
         * Lay out out the object in the specified container.
         *
         * @param object     the object to lay out
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param container  the container to use
         * @param context    the layout context
         */
        @Override
        protected void doLayout(IMObject object, PropertySet properties, IMObject parent, Component container,
                                LayoutContext context) {
            ArchetypeDescriptor archetype = context.getArchetypeDescriptor(object);
            ArchetypeNodes nodes = getArchetypeNodes();
            NodeFilter filter = getNodeFilter(object, context);

            List<Property> simple = nodes.getSimpleNodes(properties, archetype, object, filter);
            List<Property> complex = nodes.getComplexNodes(properties, archetype, object, filter);

            if (simple.size() == 1 && complex.isEmpty()) {
                ComponentSet set = createComponentSet(object, simple, context);
                container.add(set.getComponents().get(0).getComponent());
            } else {
                doSimpleLayout(object, parent, simple, container, context);
                doComplexLayout(object, parent, complex, container, context);
            }
        }

        /**
         * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
         *
         * @return the archetype nodes
         */
        @Override
        protected ArchetypeNodes getArchetypeNodes() {
            return nodes;
        }
    }

    /**
     * Returns the object associated with the reference, defaulting it to the parent if the reference is unset,
     * and the parent is of the correct archetype.
     *
     * @param reference      the reference
     * @param parent         the parent object
     * @param archetypeRange the archetypes that the reference may refer to
     * @param cache          the cache
     * @return the object, or {@code null} if the reference is set but refers to an invalid object
     */
    private IMObject getObject(IMObjectReference reference, IMObject parent, String[] archetypeRange,
                               IMObjectCache cache) {
        IMObject result = null;
        if (reference == null) {
            if (TypeHelper.isA(parent, archetypeRange)) {
                result = parent;
            }
        } else if (reference.equals(parent.getObjectReference())) {
            result = parent;
        } else {
            result = cache.get(reference);
        }
        return result;
    }
}
