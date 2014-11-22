/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openvpms.web.echo.text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.text.Document;

/**
 * The <code>RichTextArea</code> component provides HTML rich text editing
 * facilities.
 * <p>
 * The actual commands support be the RichTextArea are defined via the
 * <code>RichTextRenderer</code> interface.
 *
 */
public class RichTextArea extends echopointng.RichTextArea {

    private Map attributeMap;

    /**
     * The cursor position property.
     */
    public static final String PROPERTY_CURSOR_POSITION = "cursorPosition";
    /**
     * The text received from the client. This only gets populated once the
     * cursor position is received.
     */
    private String pending;

    /**
     * Determines if the cursor position has been received from the client.
     */
    private boolean haveCursorPosition;

    /**
     * Determines if the text has been received from the client. Note that the
     * text may be null.
     */
    private boolean haveText;
    
    public RichTextArea(Document document) {
        super(document);
    }

    public RichTextArea(Document document, String text, int columns, int rows) {
        super(document);
        if (text != null) {
            document.setText(text);
        }
        setWidth(new Extent(columns, Extent.EM));
        setHeight(new Extent(rows, Extent.EM));
    }

    /**
     * @see echopointng.able.Attributeable#getAttribute(java.lang.String)
     */
    public Object getAttribute(String attributeName) {
        if (attributeMap != null) {
            return attributeMap.get(attributeName);
        }
        return null;
    }

    /**
     * @see echopointng.able.Attributeable#getAttributeNames()
     */
    public String[] getAttributeNames() {
        if (attributeMap == null) {
            return new String[0];
        }
        int count = 0;
        String[] attributeNames = new String[attributeMap.keySet().size()];
        for (Iterator iter = attributeMap.keySet().iterator(); iter.hasNext();) {
            attributeNames[count++] = (String) iter.next();
        }
        return attributeNames;
    }

    /**
     * @see nextapp.echo2.app.text.TextComponent#processInput(java.lang.String,
     * java.lang.Object)
     */
    public void processInput(String inputName, Object inputValue) {
        if (TEXT_CHANGED_PROPERTY.equals(inputName)) {
            if (haveCursorPosition) {
                inputValue = makeValidXHTML(
                        new StringBuffer((String) inputValue));
                setText((String) inputValue);
            } else {
                pending = makeValidXHTML(new StringBuffer((String) inputValue));
                haveText = true;
            }
        } else if (PROPERTY_CURSOR_POSITION.equals(inputName)) {
            setProperty(PROPERTY_CURSOR_POSITION, inputValue);
            if (!commitPending()) {
                haveCursorPosition = true;
            } else {
                if (INPUT_ACTION.equals(inputName)) {
                    commitPending();
                    haveCursorPosition = false;
                }
                super.processInput(inputName, inputValue);
            }
        }
        if ("spellcheck".equals(inputName)) {
            // toggle the spell check
            setSpellCheckInProgress(!isSpellCheckInProgress());
        }
    }

    private boolean commitPending() {
        if (haveText) {
            try {
                setText(pending);
            } finally {
                pending = null;
                haveText = false;
            }
            return true;
        }
        return false;
    }

    /**
     * @see echopointng.able.Attributeable#setAttribute(java.lang.String,
     * java.lang.Object)
     */
    public void setAttribute(String attributeName, Object attributeValue) {
        if (attributeMap == null) {
            attributeMap = new HashMap();
        }
        attributeMap.put(attributeName, attributeValue);
    }

    /**
     * Returns the cursor position.
     *
     * @return the cursor position
     */
    public int getCursorPosition() {
        Integer value = (Integer) getProperty(PROPERTY_CURSOR_POSITION);
        return value == null ? 0 : value;
    }

    /**
     * Sets the cursor position.
     *
     * @param position the cursor position
     */
    public void setCursorPosition(int position) {
        if (position < 0) {
            setProperty(PROPERTY_CURSOR_POSITION, null);
        } else {
            setProperty(PROPERTY_CURSOR_POSITION, position);
        }
    }
}
