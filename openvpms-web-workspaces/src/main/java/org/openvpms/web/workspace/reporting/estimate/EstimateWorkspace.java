/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.estimate;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GroupBoxFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.workspace.reporting.AbstractReportingWorkspace;

/**
 *
 * @author benjamincharlton
 */
public class EstimateWorkspace extends AbstractReportingWorkspace<Act> {
    
    private Query<Act> query;
    
    public EstimateWorkspace(Context context, MailContext mailContext) {
        super("reporting", "estimate", Act.class, context, mailContext);
    }
        protected void doLayout(Component container, FocusGroup group) {
        LayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        IMObjectComponentFactory factory = new TableComponentFactory(context);
        context.setComponentFactory(factory);
        context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);

        query = new EstimateQuery();
        Browser<Act> browser = new DefaultIMObjectTableBrowser<Act>(query, new EstimateTableModel(context), context);
        GroupBox box = GroupBoxFactory.create(browser.getComponent());
        container.add(box);
        group.add(browser.getFocusGroup());
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button set
     */
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add("print", new ActionListener() {
            public void onAction(ActionEvent event) {
                //todo;
            }
        });
    }
    
    class EstimateTableModel extends AbstractActTableModel {

        public EstimateTableModel(LayoutContext context){
            super(EstimateQuery.SHORT_NAMES, context);
            }

        /**
         * Returns a list of descriptor names to include in the table.
         *
         * @return the list of descriptor names to include in the table
         */
        @Override
        protected String[] getNodeNames() {
            return new String[]{"customer", "status", "startTime","endTime", "lowTotal", "highTotal","notes"};
        }
    }
}
