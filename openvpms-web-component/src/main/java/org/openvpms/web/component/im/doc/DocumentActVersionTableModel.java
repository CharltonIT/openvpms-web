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
 *  Copyright 2007-2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;


import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * Table model for document version acts.
 * <p/>
 * This suppresses the status, type and version columns.
 *
 * @author Tim Anderson
 */
public class DocumentActVersionTableModel extends DocumentActTableModel {

    /**
     * Constructs a {@code DocumentActTableModel}.
     */
    public DocumentActVersionTableModel(LayoutContext context) {
        super(false, false, false, context);
    }

}