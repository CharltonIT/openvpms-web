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
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Abstract implementation of the {@link IMObjectView} inteface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectView implements IMObjectView {

    /**
     * The object to display.
     */
    private final IMObject object;

    /**
     * The parent object. May be <code>null</code>.
     */
    private final IMObject parent;

    /**
     * The object's properties.
     */
    private PropertySet properties;

    /**
     * The layout strafegy.
     */
    private IMObjectLayoutStrategy layout;

    /**
     * The component produced by the renderer.
     */
    private Component component;

    /**
     * The component focus group.
     */
    private FocusGroup focusGroup;

    /**
     * Invoked when the layout changes.
     */
    private ActionListener layoutListener;


    /**
     * Construct a new <code>AbstractIMObjectView</code>.
     *
     * @param object the object to display
     * @param parent the parent object. May be <code>null</code>
     * @param layout the layout strategy. May be <code>null</code>
     */
    public AbstractIMObjectView(IMObject object, PropertySet properties,
                                IMObject parent,
                                IMObjectLayoutStrategy layout) {
        this.object = object;
        this.parent = parent;
        this.properties = properties;
        this.layout = layout;
    }

    /**
     * Returns the object being viewed.
     *
     * @return the object being viewed
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    public Component getComponent() {
        if (component == null) {
            component = createComponent();
        }
        return component;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or <code>null</code> if the component hasn't
     *         been rendered
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Changes the layout.
     *
     * @param layout the new layout strategy
     */
    public void setLayout(IMObjectLayoutStrategy layout) {
        component = null;
        this.layout = layout;
        getComponent();
        if (layoutListener != null) {
            layoutListener.actionPerformed(new ActionEvent(this, null));
        }
    }

    /**
     * Returns the current layout.
     *
     * @return the layout. May be <code>null</code>
     */
    public IMObjectLayoutStrategy getLayout() {
        return layout;
    }

    /**
     * Sets a listener to be notified when the layout changes.
     *
     * @param listener the listener
     */
    public void setLayoutListener(ActionListener listener) {
        layoutListener = listener;
    }

    /**
     * Creates the component to display the object.
     *
     * @return a new component
     */
    protected Component createComponent() {
        LayoutContext context = getLayoutContext();
        ComponentState component = layout.apply(object, properties, parent,
                                                context);
        focusGroup = component.getFocusGroup();
        return component.getComponent();
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected abstract LayoutContext getLayoutContext();

}
