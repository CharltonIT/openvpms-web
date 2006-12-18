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

package org.openvpms.web.component.im.edit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ArchetypeHandler;
import org.openvpms.web.component.im.util.ArchetypeHandlers;


/**
 * A factory for {@link IMObjectReferenceEditor} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-08-10 07:37:14Z $
 */
public class IMObjectReferenceEditorFactory {

    /**
     * Editor implementations.
     */
    private static ArchetypeHandlers<IMObjectReferenceEditor> editors;

    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(IMObjectReferenceEditorFactory.class);

    /**
     * Prevent construction.
     */
    private IMObjectReferenceEditorFactory() {
    }

    /**
     * Creates a new editor.
     *
     * @param property the reference property
     * @param context  the layout context
     * @return an editor for <code>property</code>
     */
    public static IMObjectReferenceEditor create(Property property,
                                                 LayoutContext context) {
        IMObjectReferenceEditor result = null;

        String[] shortNames = DescriptorHelper.getShortNames(
                property.getDescriptor());
        ArchetypeHandler handler = getEditors().getHandler(shortNames);

        if (handler != null) {
            try {
                result = (IMObjectReferenceEditor) handler.create(
                        new Object[]{property, context});
            } catch (Throwable exception) {
                _log.error(exception, exception);
            }
        }
        if (result == null) {
            result = new DefaultIMObjectReferenceEditor(property, context);
        }
        return result;
    }

    /**
     * Returns the editors.
     *
     * @return the editors
     */
    private static synchronized ArchetypeHandlers<IMObjectReferenceEditor>
            getEditors() {
        if (editors == null) {
            editors = new ArchetypeHandlers<IMObjectReferenceEditor>(
                    "IMObjectReferenceEditorFactory.properties",
                    IMObjectReferenceEditor.class);
        }
        return IMObjectReferenceEditorFactory.editors;
    }

}