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
 *
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;


/**
 * Helper methods for document act table models.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActTableHelper {

    /**
     * Returns a component to display a document associated with a document
     * act.
     *
     * @param act  the document act
     * @param link if <tt>true</tt> add a hyperlink to the associated document,
     *             or the document template if there is no document
     * @return a new component
     */
    public static Component getDocumentViewer(DocumentAct act,
                                              boolean link) {
        Component result;
        ActBean bean = new ActBean(act);
        if (act.getDocument() != null || bean.hasNode("documentTemplate")) {
            DocumentViewer viewer = new DocumentViewer(act, link);
            result = viewer.getComponent();
        } else {
            result = new Label();
        }
        return result;
    }
}
