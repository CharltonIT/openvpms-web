/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 * http://www.openvpms.org/license/
 *
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.reporting.estimate;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GroupBoxFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.estimate.EstimateActions;
import org.openvpms.web.workspace.reporting.AbstractReportingWorkspace;

/**
 *
 * @author benjamincharlton
 */
public class EstimateWorkspace extends AbstractReportingWorkspace<Act> {

    private Query<Act> query;

    private EstimateActions actions;

    private Browser<Act> browser;

    public EstimateWorkspace(Context context, MailContext mailContext) {
        super("reporting", "estimate", Act.class, context, mailContext);
    }

    protected void doLayout(Component container, FocusGroup group) {
        LayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        IMObjectComponentFactory factory = new TableComponentFactory(context);
        context.setComponentFactory(factory);
        context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);

        query = new EstimateQuery();
        browser = new DefaultIMObjectTableBrowser<Act>(query, new EstimateTableModel(context), context);
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
                onPrint();
            }
        });
    }

    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        Act estimate = getObject();
        if (enable) {
            buttons.setEnabled("print", actions.canPost(estimate));
            buttons.setEnabled("print", actions.canInvoice(estimate));
        }
    }
    //todo

    protected void onPrint() {
        try {
            Act estimate = browser.getSelected();
            if (estimate != null) {
                ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(estimate, getContext());
                IMPrinter<Act> printer = IMPrinterFactory.create(estimate, locator, getContext());
                HelpContext printhelp = this.getHelpContext().topic(estimate, "print");
                InteractiveIMPrinter<Act> interactive = new InteractiveIMPrinter<Act>(printer, getContext(), printhelp);
                interactive.setMailContext(getMailContext());
                interactive.print();
            }

        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }
}

class EstimateTableModel extends AbstractActTableModel {

    public EstimateTableModel(LayoutContext context) {
        super(EstimateQuery.SHORT_NAMES, context);
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return new String[]{"id", "customer", "status", "startTime", "endTime", "lowTotal", "highTotal", "notes"};
    }
}
