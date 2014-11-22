/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openvpms.web.component.bound;

import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.text.RichTextArea;
import org.openvpms.web.echo.text.TextDocument;
/**
 *
 * @author benjamincharlton
 */
public class BoundRichTextArea extends RichTextArea {
    
    public Binder binder;
    
    public BoundRichTextArea(Property property) {
        super(new TextDocument());
        binder = new RichTextAreaComponentBinder(this, property);
    }
    
    public BoundRichTextArea(Property property, int columns, int rows) {
        super(new TextDocument());
        binder = new RichTextAreaComponentBinder(this, property, columns, rows);
    }
    
    @Override
    public void init() {
        super.init();
        binder.bind();
    }
    
    @Override
    public void dispose() {
        super.dispose();
        binder.unbind();
    }
}
