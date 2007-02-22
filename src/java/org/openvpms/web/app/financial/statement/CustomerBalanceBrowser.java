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

package org.openvpms.web.app.financial.statement;

import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.query.TableBrowser;


/**
 * Browser for customer balance summaries.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerBalanceBrowser extends TableBrowser<ObjectSet> {

    /**
     * Construct a new <tt>CustomerBalanceBrowser</tt> that queries objects and
     * displays them in a table.
     */
    public CustomerBalanceBrowser() {
        super(new CustomerBalanceQuery(), null,
              new CustomerBalanceSummaryTableModel());
    }

}
