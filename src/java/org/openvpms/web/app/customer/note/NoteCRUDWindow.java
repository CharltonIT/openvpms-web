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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.customer.note;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.DefaultIMObjectActions;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.subsystem.AbstractViewCRUDWindow;


/**
 * Customer note CRUD window.
 *
 * @author Tim Anderson
 */
public class NoteCRUDWindow extends AbstractViewCRUDWindow<Act> {

    /**
     * Constructs a new {@code NoteCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create.
     * @param help       the help context
     */
    public NoteCRUDWindow(Archetypes<Act> archetypes, HelpContext help) {
        super(archetypes, DefaultIMObjectActions.<Act>getInstance(), help);
    }

}
