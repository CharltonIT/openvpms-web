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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.customer.info;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.TabPaneModel;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Layout strategy for customers that includes an appointment browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerViewLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Lays out child components in a tab model.
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param model       the tab model
     * @param context     the layout context
     * @param shortcuts   if <tt>true</tt> include short cuts
     */
    @Override
    protected void doTabLayout(IMObject object, List<NodeDescriptor> descriptors, PropertySet properties,
                               TabPaneModel model, LayoutContext context, boolean shortcuts) {
        super.doTabLayout(object, descriptors, properties, model, context, shortcuts);
        Browser<Act> appointments = getAppointments((Party) object);
        Component inset = ColumnFactory.create("Inset", appointments.getComponent());

        String label = Messages.get("customer.info.appointments");
        if (shortcuts && model.size() < 10) {
            label = getShortcut(label, model.size() + 1);
        }
        model.addTab(label, inset);
    }

    /**
     * Creates a new appointment browser.
     *
     * @param customer the customer
     * @return a new appointment browser
     */
    protected Browser<Act> getAppointments(Party customer) {
        Query<Act> query = new CustomerAppointmentQuery(customer);
        IMTableModel<Act> model = new CustomerAppointmentTableModel();
        Browser<Act> browser = new DefaultIMObjectTableBrowser<Act>(query, model);
        browser.addQueryListener(new QueryBrowserListener<Act>() {
            public void selected(Act object) {
                switchTo(object);
            }

            public void query() {
                // no-op
            }
        });
        return browser;
    }

    /**
     * Switch to the appointment workspace, and select the appointment.
     *
     * @param act the appointment
     */
    private void switchTo(Act act) {
        ContextApplicationInstance.getInstance().switchTo(act);
    }

}