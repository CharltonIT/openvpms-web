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
 *
 *  $Id: $
 */

package org.openvpms.web.component.mail;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.query.Browser;


/**
 * A factory for attachment browser instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public interface AttachmentBrowserFactory {

    /**
     * Constructs an attachment browser.
     *
     * @param context the mail context.
     * @return a new attachment browser
     */
    Browser<Act> createBrowser(MailContext context);
}