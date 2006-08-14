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

package org.openvpms.web.component.im.view.act;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.util.GridFactory;

import java.util.List;


/**
 * Act layout strategy. Hides the items node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The act item editor. May be <code>null</code>.
     */
    private final IMObjectCollectionEditor _editor;

    /**
     * Determines if the items node should be displayed.
     */
    private final boolean _showItems;


    /**
     * Construct a new <code>ActLayoutStrategy</code>.
     */
    public ActLayoutStrategy() {
        this(true);
    }

    /**
     * Construct a new <code>ActLayoutStrategy</code>
     *
     * @param showItems if <code>true</code>, show the items node
     */
    public ActLayoutStrategy(boolean showItems) {
        this(null, showItems);
    }

    /**
     * Construct a new <code>ActLayoutStrategy</code>.
     *
     * @param editor the act items editor. May be <code>null</code>.
     */
    public ActLayoutStrategy(IMObjectCollectionEditor editor) {
        this(editor, true);
    }

    /**
     * Construct a new <code>ActLayoutStrategy</code>.
     *
     * @param editor    the act items editor. May be <code>null</code>.
     * @param showItems if <code>true</code>, show the items node
     */
    private ActLayoutStrategy(IMObjectCollectionEditor editor,
                              boolean showItems) {
        _editor = editor;
        _showItems = showItems;
    }

    /**
     * Lays out each child component in a group box
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container to use
     * @param context     the layout context
     */
    @Override
    protected void doComplexLayout(IMObject object,
                                   List<NodeDescriptor> descriptors,
                                   PropertySet properties, Component container,
                                   LayoutContext context) {
        Grid grid = GridFactory.create(4);
        IMObjectComponentFactory factory = context.getComponentFactory();
        for (NodeDescriptor descriptor : descriptors) {
            Property property = properties.get(descriptor);
            Component component = factory.create(property, object);
            add(grid, property, component, context);
        }

        container.add(grid);

        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(object);
        NodeDescriptor items = archetype.getNodeDescriptor("items");
        if (items != null && _showItems && !items.isHidden()) {
            GroupBox box = new GroupBox();
            box.setTitle(items.getDisplayName());

            if (_editor != null) {
                box.add(_editor.getComponent());
            } else {
                Component child = createItems(object, items, properties, context
                );
                box.add(child);
            }
            container.add(box);
        }
    }

    /**
     * Creates a component to represent the item node.
     *
     * @param object     the parent object
     * @param items      the items node descriptor
     * @param properties the properties
     * @param context    the layout context
     * @return a component to represent the items node
     */
    protected Component createItems(IMObject object, NodeDescriptor items,
                                    PropertySet properties,
                                    LayoutContext context) {
        IMObjectLayoutStrategy strategy
                = new ActRelationshipTableLayoutStrategy(items);

        context = new DefaultLayoutContext(context);
        context.setComponentFactory(new TableComponentFactory(context));

        return strategy.apply(object, properties, null, context);
    }

    /**
     * Returns a node filter to filter nodes. This implementation filters the
     * "items" node.
     *
     * @param context the context
     * @return a node filter to filter nodes
     */
    @Override
    protected NodeFilter getNodeFilter(LayoutContext context) {
        NodeFilter filter = new NamedNodeFilter("items");
        return getNodeFilter(context, filter);
    }


}
