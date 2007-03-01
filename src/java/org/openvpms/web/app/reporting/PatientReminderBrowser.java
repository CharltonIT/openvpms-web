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

package org.openvpms.web.app.reporting;

import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TableBrowser;


/**
 * Patient reminder browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientReminderBrowser extends TableBrowser<ObjectSet> {

    /**
     * Construct a new <tt>PatientReminderBrowser</tt> that queries reminders
     * using the specified query.
     *
     * @param query the query
     */
    public PatientReminderBrowser(Query<ObjectSet> query) {
        super(query, null, new PatientReminderTableModel());
    }

}
