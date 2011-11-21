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

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.query.Browser;

import java.util.List;


/**
 * Context information to pass to the mail editor.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public interface MailContext {

    /**
     * Returns the available 'from' email addresses.
     *
     * @return the 'from' email addresses
     */
    List<Contact> getFromAddresses();

    /**
     * Returns the available ''to' email addresses.
     *
     * @return the 'to' email addresses
     */
    List<Contact> getToAddresses();

    /**
     * Returns a browser for documents that may be attached to mails.
     *
     * @return a browser. May be <tt>null</tt>
     */
    Browser<Act> createAttachmentBrowser();
}