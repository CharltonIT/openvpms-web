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

package org.openvpms.web.component.im.layout;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.filter.ValueNodeFilter;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.layout.ViewLayoutStrategyFactory;


/**
 * Default implmentation of the {@link LayoutContext} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultLayoutContext implements LayoutContext {

    /**
     * The context.
     */
    private Context context;

    /**
     * Determines if this is an edit context.
     */
    private boolean edit;

    /**
     * The component factory.
     */
    private IMObjectComponentFactory factory;

    /**
     * The default node filter.
     */
    private NodeFilter filter;

    /**
     * The layout strategy factory.
     */
    private IMObjectLayoutStrategyFactory layoutFactory
            = DEFAULT_LAYOUT_FACTORY;

    /**
     * The layout depth.
     */
    private int depth;

    /**
     * The default layout strategy factory.
     */
    private static final IMObjectLayoutStrategyFactory DEFAULT_LAYOUT_FACTORY
            = new ViewLayoutStrategyFactory();

    /**
     * Construct a new <code>DefaultLayoutContext</code>.
     */
    public DefaultLayoutContext() {
        this((IMObjectComponentFactory) null);
    }

    /**
     * Construct a new <code>DefaultLayoutContext</code>.
     *
     * @param edit if <code>true</code> this is an edit context; if
     *             <code>false</code> it is a view context.
     */
    public DefaultLayoutContext(boolean edit) {
        this((IMObjectComponentFactory) null);
        this.edit = edit;
    }

    /**
     * Construct a new <code>DefaultLayoutContext</code>.
     *
     * @param factory the component factory. May  be <code>null</code>
     */
    public DefaultLayoutContext(IMObjectComponentFactory factory) {
        this.factory = factory;
        NodeFilter id = new ValueNodeFilter("uid", -1);
        NodeFilter showOptional = new BasicNodeFilter(true);
        filter = new ChainedNodeFilter(id, showOptional);
    }

    /**
     * Construct a new  <code>DefaultLayoutContext</code> from an existing
     * layout context. Increases the layout depth by 1.
     *
     * @param context the context
     */
    public DefaultLayoutContext(LayoutContext context) {
        this.context = context.getContext();
        factory = context.getComponentFactory();
        filter = context.getDefaultNodeFilter();
        edit = context.isEdit();
        layoutFactory = context.getLayoutStrategyFactory();
        depth = context.getLayoutDepth() + 1;
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    public Context getContext() {
        if (context == null) {
            context = GlobalContext.getInstance();
        }
        return context;
    }

    /**
     * Sets the context.
     *
     * @param context the context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Determines if this is an edit context.
     *
     * @return <code>true</code> if this is an edit context; <code>false</code>
     *         if it is a view context. Defaults to <code>false</code>
     */
    public boolean isEdit() {
        return edit;
    }

    /**
     * Sets if this is an edit context.
     *
     * @param edit if <code>true</code> this is an edit context; if
     *             <code>false</code> it is a view context.
     */
    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    /**
     * Returns the component factory.
     *
     * @return the component factory
     */
    public IMObjectComponentFactory getComponentFactory() {
        return factory;
    }

    /**
     * Sets the component factory.
     *
     * @param factory the component factory
     */
    public void setComponentFactory(IMObjectComponentFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns the default filter.
     *
     * @return the default filter. May be <code>null</code>
     */
    public NodeFilter getDefaultNodeFilter() {
        return filter;
    }

    /**
     * Sets the default filter.
     *
     * @param filter the default filter. May be <code>null</code>
     */
    public void setNodeFilter(NodeFilter filter) {
        this.filter = filter;
    }

    /**
     * Returns the layout strategy factory.
     *
     * @return the layout strategy factory
     */
    public IMObjectLayoutStrategyFactory getLayoutStrategyFactory() {
        return layoutFactory;
    }

    /**
     * Sets the layout strategy factory.
     *
     * @param factory the layout strategy factory
     */
    public void setLayoutStrategyFactory(
            IMObjectLayoutStrategyFactory factory) {
        layoutFactory = factory;
    }

    /**
     * Returns the layout depth.
     *
     * @return the layout depth. If unset, defaults to <code>0</code>
     */
    public int getLayoutDepth() {
        return depth;
    }

    /**
     * Sets the layout depth.
     *
     * @param depth the depth
     */
    public void setLayoutDepth(int depth) {
        this.depth = depth;
    }
}