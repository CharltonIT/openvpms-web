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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.document;

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentActEditor;
import org.openvpms.web.component.im.doc.VersionedDocumentActEditorTest;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Tests the {@link CustomerDocumentActEditor} class with <em>act.customerDocumentAttachment</em> acts.
 *
 * @author Tim Anderson
 */
public class CustomerAttachmentActEditorTestCase extends VersionedDocumentActEditorTest {

    /**
     * Creates a new act.
     *
     * @return a new act
     */
    protected DocumentAct createAct() {
        return (DocumentAct) TestHelper.create(CustomerArchetypes.DOCUMENT_ATTACHMENT);
    }

    /**
     * Creates a new editor.
     *
     * @param act the act to edit
     * @return a new editor
     */
    protected DocumentActEditor createEditor(DocumentAct act) {
        DefaultLayoutContext layout = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        Context context = layout.getContext();
        context.setCustomer(TestHelper.createCustomer());
        context.setUser(TestHelper.createUser());
        return new CustomerDocumentActEditor(act, null, layout);
    }

    /**
     * Creates a new document.
     *
     * @return a new document
     */
    protected Document createDocument() {
        return createImage();
    }
}