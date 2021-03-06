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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.focus;

import echopointng.DateField;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.web.echo.table.KeyTable;


/**
 * Focus helper.
 *
 * @author Tim Anderson
 */
public class FocusHelper {

    /**
     * Returns the first component that may have focus set.
     *
     * @param component the component. May be {@code null}
     * @return the first child component that may have focus set, or {@code null} if none may have focus set
     */
    public static Component getFocusable(Component component) {
        Component result = null;
        if (component != null) {
            if (isFocusable(component) && component.isEnabled()
                && component.isFocusTraversalParticipant()) {
                if (component instanceof DateField) {
                    result = ((DateField) component).getTextField();
                } else {
                    result = component;
                }
            } else if (component.getComponentCount() != 0) {
                for (Component child : component.getComponents()) {
                    result = getFocusable(child);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Sets the focus on the first component that may have focus set.
     *
     * @param component the component
     * @return the focussed component or {@code null} if no component may
     *         receive the focus
     */
    public static Component setFocus(Component component) {
        Component focusable = getFocusable(component);
        ApplicationInstance active = ApplicationInstance.getActive();
        if (active != null) {
            active.setFocusedComponent(focusable);
        }
        return focusable;
    }

    /**
     * Returns the component with the focus.
     *
     * @return the focussed component, or {@code null} if no component currently has the focus.
     */
    public static Component getFocus() {
        ApplicationInstance active = ApplicationInstance.getActive();
        return (active != null) ? active.getFocusedComponent() : null;
    }

    /**
     * Determines if a component is one that may receive focus.
     *
     * @param component the component
     * @return {@code true} if the component is a focusable component; otherwise {@code false}
     */
    private static boolean isFocusable(Component component) {
        return (component instanceof TextComponent
                || component instanceof CheckBox
                || component instanceof DateField
                || component instanceof AbstractButton
                || component instanceof KeyTable
                || component instanceof ListBox
                || component instanceof SelectField);
    }

}
