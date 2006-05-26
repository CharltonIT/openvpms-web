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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Default implementation of the {@link IMObjectCollectionEditor} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultIMObjectCollectionEditor
        extends AbstractIMObjectCollectionEditor {

    /**
     * Construct a new <code>DefaultIMObjectCollectionEditor</code>.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public DefaultIMObjectCollectionEditor(CollectionProperty property,
                                           IMObject object,
                                           LayoutContext context) {
        super(property, object, context);
    }
}
