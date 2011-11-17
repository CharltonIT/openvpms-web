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
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Viewer for {@link IMObjectReference}s of type <em>document.*</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentViewer {

    /**
     * The reference to view.
     */
    private final IMObjectReference reference;

    /**
     * The parent object. May be <tt>null</tt>
     */
    private final IMObject parent;

    /**
     * The document name. May be <tt>null</tt>
     */
    private final String name;

    /**
     * Determines if a hyperlink should be created, to enable downloads of
     * the document.
     */
    private final boolean link;


    /**
     * Constructs a new <tt>DocumentViewer</tt>.
     *
     * @param act  the document act
     * @param link if <tt>true</tt> enable an hyperlink to the object
     */
    public DocumentViewer(DocumentAct act, boolean link) {
        this(act.getDocument(), act, act.getFileName(), link);
    }

    /**
     * Constructs a new <tt>DocumentViewer</tt>.
     *
     * @param reference the reference to view
     * @param parent    the parent. May be <tt>null</tt>
     * @param link      if <tt>true</tt> enable an hyperlink to the object
     */
    public DocumentViewer(IMObjectReference reference, IMObject parent, boolean link) {
        this(reference, parent, null, link);
    }

    /**
     * Constructs a new <tt>DocumentViewer</tt>.
     *
     * @param reference the reference to view. May be <tt>null</tt>
     * @param parent    the parent. May be <tt>null</tt>
     * @param name      the document file name. May be <tt>null</tt>
     * @param link      if <tt>true</tt> enable an hyperlink to the object
     */
    public DocumentViewer(IMObjectReference reference, IMObject parent, String name, boolean link) {
        this.reference = reference;
        this.parent = parent;
        if (name != null) {
            this.name = name;
        } else if (parent instanceof DocumentAct) {
            this.name = ((DocumentAct) parent).getFileName();
        } else if (reference != null) {
            this.name = DescriptorHelper.getDisplayName(reference.getArchetypeId().getShortName());
        } else {
            this.name = null;
        }
        this.link = link;
    }


    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        Component result;
        boolean hasDoc;
        if (reference != null) {
            hasDoc = true;
        } else if (parent instanceof DocumentAct) {
            IMObjectBean bean = new IMObjectBean(parent);
            hasDoc = bean.hasNode("documentTemplate");
        } else {
            hasDoc = false;
        }
        if (hasDoc) {
            if (link) {
                Downloader downloader;
                if (parent instanceof DocumentAct) {
                    DocumentAct act = (DocumentAct) parent;
                    downloader = new DocumentActDownloader(act);
                } else {
                    downloader = new DocumentRefDownloader(reference, name);
                }
                result = downloader.getComponent();
            } else {
                Label label = LabelFactory.create();
                label.setText(name);
                result = label;
            }
        } else {
            Label label = LabelFactory.create();
            label.setText(Messages.get("document.none"));
            result = label;
        }
        return result;
    }

}
