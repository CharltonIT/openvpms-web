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

package org.openvpms.web.component.bound;

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.DocumentEvent;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.macro.Position;
import org.openvpms.web.component.property.NoOpPropertyTransformer;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyTransformer;
import org.openvpms.web.component.property.StringPropertyTransformer;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.DocumentListener;
import org.openvpms.web.echo.text.RichTextArea;


/**
 * Helper to bind a property to a text area component.
 *
 * @author Tim Anderson
 */
public class RichTextAreaComponentBinder extends Binder {

    private final RichTextArea richTextArea;
    
     /**
     * The document update listener.
     */
    private final DocumentListener listener;
    /**
     * Constructs a <tt>TextAreaComponentBinder</tt>.
     * <p/>
     * If not already present, the property is associated with an {@link StringPropertyTransformer}
     * that doesn't trim leading and trailing spaces or new lines.
     *
     * @param property  the property to bind
     * @param component the component to bind
     */
    public RichTextAreaComponentBinder(RichTextArea component, Property property) {
        super(property, false);
        this.richTextArea = component;
        listener = new DocumentListener() {
            public void onUpdate(DocumentEvent event) {
                setProperty();
            }
        };
        this.richTextArea.getDocument().addDocumentListener(listener);
        ActionListener actionListener = new ActionListener() {
            public void onAction(ActionEvent event) {
                // no-op.
            }
        };
        this.richTextArea.addActionListener(actionListener);
        bind();
    }

    /**
     * Constructs a <tt>TextAreaComponentBinder</tt>.
     * <p/>
     * If not already present, the property is associated with an {@link StringPropertyTransformer}
     * that doesn't trim leading and trailing spaces or new lines.
     *
     * @param component the component to bind
     * @param property  the property to bind
     * @param columns   the number of columns to display
     * @param rows      the number of rows to display
     */
    public RichTextAreaComponentBinder(RichTextArea component, Property property, int columns, int rows) {
        this(component, property);
        component.setWidth(new Extent(columns, Extent.EX));
        component.setHeight(new Extent(rows, Extent.EM));
    }
    
    @Override
    protected void setFieldValue(Object value) {
        richTextArea.getDocument().removeDocumentListener(listener);
        String text = (value != null) ? value.toString() : null;
        richTextArea.setText(text);
        richTextArea.getDocument().addDocumentListener(listener);
    }

    @Override
    protected Object getFieldValue() {
        return richTextArea.getText();
    }
    /**
     * Updates the property from the field.
     * <p/>
     * This moves the cursor position by the change in length, if a macro is expanded.
     *
     * @param property the property to update
     * @return {@code true} if the property was updated
     */
    @Override
    protected boolean setProperty(Property property) {
        Object oldValue = getFieldValue();
        Object value = oldValue;
        Position position = null;
        PropertyTransformer transformer = property.getTransformer();
        PropertyTransformer oldTransformer = null;
        if (value instanceof String && transformer instanceof StringPropertyTransformer) {
            // want to track the cursor position changed by macro expansion, so need to invoke the transformer
            // directly, prior to setting the (possibly) expanded value on the property.
            // If the transformation, fails, just invoke setValue() with the original value so the property can
            // trap the error.
            try {
                position = new Position(richTextArea.getCursorPosition());
                value = ((StringPropertyTransformer) transformer).apply(value, position);

                // don't want to run macro expansion again
                oldTransformer = transformer;
                property.setTransformer(NoOpPropertyTransformer.INSTANCE);
            } catch (Throwable ignore) {
                // the transformation has failed, so let the property handle the error
                position = null;
            }
        }

        boolean result;
        try {
            result = property.setValue(value);
        } finally {
            if (oldTransformer != null) {
                // reset the transformer
                property.setTransformer(oldTransformer);
            }
        }
        String newValue = property.getString();
        if (!ObjectUtils.equals(oldValue, newValue)) {
            setField();
            if (position != null && position.getOldPosition() != position.getNewPosition()) {
                richTextArea.setCursorPosition(position.getNewPosition());
            }
        }
        return result;
    }
}