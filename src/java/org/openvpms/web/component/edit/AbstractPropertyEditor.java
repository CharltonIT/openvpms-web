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
 *  $Id: AbstractPropertyEditor.java 1535 2006-11-14 04:41:04Z tanderson $
 */

package org.openvpms.web.component.edit;

import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;


/**
 * Abstract implementation of the {@link PropertyEditor} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-11-14 04:41:04Z $
 */
public abstract class AbstractPropertyEditor implements PropertyEditor {

    /**
     * The property being edited.
     */
    private final Property property;


    /**
     * Creates a new <tt>AbstractPropertyEditor</tt>.
     *
     * @param property the property being edited
     */
    public AbstractPropertyEditor(Property property) {
        this.property = property;
    }

    /**
     * Returns the property being edited.
     *
     * @return the property being edited
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return <tt>true</tt> if the object has been modified
     */
    public boolean isModified() {
        return getProperty().isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        getProperty().clearModified();
    }

    /**
     * Add a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        getProperty().addModifiableListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        getProperty().addModifiableListener(listener, index);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        getProperty().removeModifiableListener(listener);
    }

    /**
     * Determines if the object is valid.
     *
     * @return <tt>true</tt> if the object is valid; otherwise
     *         <tt>false</tt>
     */
    public boolean isValid() {
        Validator validator = new Validator();
        return validator.validate(this);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid
     *         otherwise <tt>false</tt>
     */
    public boolean validate(Validator validator) {
        return validator.validate(getProperty());
    }

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed, the behaviour of invoking any method is undefined.
     */
    public void dispose() {
    }
}
