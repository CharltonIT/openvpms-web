/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openvpms.web.echo.text;

import echopointng.ui.util.RenderingContext;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.PartialUpdateParticipant;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.service.JavaScriptService;
import static org.openvpms.web.echo.text.TextComponent.PROPERTY_CURSOR_POSITION;
import org.w3c.dom.Element;

/**
 *
 * @author benjamincharlton
 */
public class RichTextAreaPeer extends echopointng.ui.syncpeer.RichTextAreaPeer {

    public static final Service RICH_TEXT_SERVICE = JavaScriptService.forResource(
            "EPNG.RichTextArea", "/org/openvpms/web/echo/js/rta.js");

    static {  
        WebRenderServlet.getServiceRegistry().remove(RICH_TEXT_SERVICE);
        WebRenderServlet.getServiceRegistry().add(RICH_TEXT_SERVICE);
    }

    public RichTextAreaPeer() {
        super();
        partialUpdateManager.add(PROPERTY_CURSOR_POSITION,
                new PartialUpdateParticipant() {

                    /**
                     * @see
                     * PartialUpdateParticipant#canRenderProperty(RenderContext,
                     * ServerComponentUpdate)
                     */
                    public boolean canRenderProperty(RenderContext rc,
                            ServerComponentUpdate update) {
                        return true;
                    }

                    /**
                     * @see
                     * nextapp.echo2.webcontainer.PartialUpdateParticipant#renderProperty(
                     * nextapp.echo2.webcontainer.RenderContext,
                     * nextapp.echo2.app.update.ServerComponentUpdate)
                     */
                    public void renderProperty(RenderContext rc,
                            ServerComponentUpdate update) {
                        nextapp.echo2.app.text.TextComponent textComponent
                        = (nextapp.echo2.app.text.TextComponent) update.getParent();
                        String elementId = ContainerInstance.getElementId(
                                textComponent);
                        ServerMessage serverMessage = rc.getServerMessage();
                        Element itemizedUpdateElement = serverMessage.getItemizedDirective(
                                ServerMessage.GROUP_ID_POSTUPDATE,
                                "EPRTA.MessageProcessor", "set-cursor-position",
                                new String[0], new String[0]);
                        Element itemElement = serverMessage.getDocument().createElement(
                                "item");
                        itemElement.setAttribute("eid", elementId);
                        Object property = textComponent.getProperty(
                                PROPERTY_CURSOR_POSITION);
                        itemElement.setAttribute("cursorPosition",
                                property != null ? property.toString() : "0");
                        itemizedUpdateElement.appendChild(itemElement);
                    }
                });
    }
    
	protected void createInitDirective(RenderingContext rc, RichTextArea rta, String userAgent) {
        super.createInitDirective(rc, rta, userAgent);
        Integer cursorPos = (Integer) rta.getRenderProperty(PROPERTY_CURSOR_POSITION);
        ServerMessage serverMessage = rc.getServerMessage();
        //the item is created in the super function - we just need to adjust the already existing element.
        Element itemElement = serverMessage.getDocument().getElementById(rc.getElementId());
        if (cursorPos != null) {
            itemElement.setAttribute("cursor-position", cursorPos.toString());
        }
        if (!rta.isRenderEnabled()) {
            itemElement.setAttribute("enabled", "false");
        }
        if (rta.hasActionListeners()) {
            itemElement.setAttribute("server-notify", "true");
        }
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(
                ServerMessage.GROUP_ID_POSTUPDATE, "EPRTA.MessageProcessor", "init",
                new String[0], new String[0]);
        itemizedUpdateElement.appendChild(itemElement);
        
    }
}

