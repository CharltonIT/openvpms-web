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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.statement;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.finance.account.CustomerBalanceSummaryQuery;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GroupBoxFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.reporting.AbstractReportingWorkspace;

import java.util.Calendar;
import java.util.Date;


/**
 * Statement workspace.
 *
 * @author Tim Anderson
 */
public class StatementWorkspace extends AbstractReportingWorkspace<Act> {

    /**
     * The query.
     */
    private CustomerBalanceQuery query;

    /**
     * The browser.
     */
    private Browser<ObjectSet> browser;

    /**
     * Determines if this is the first rendering of the workspace.
     */
    private boolean rendered;


    /**
     * Constructs a {@code StatementWorkspace}.
     *
     * @param context     the context
     * @param mailContext the mail context
     */
    public StatementWorkspace(Context context, MailContext mailContext) {
        super("reporting", "statement", Act.class, context, mailContext);
    }

    /**
     * Renders the workspace.
     *
     * @return the component representing the workspace
     */
    @Override
    public Component getComponent() {
        Component component = super.getComponent();
        if (rendered) {
            // make sure the account types are up to date
            query.refreshAccountTypes();
        } else {
            rendered = true;
        }
        return component;
    }

    /**
     * Lays out the components.
     *
     * @param container the container
     * @param group     the focus group
     */
    @Override
    protected void doLayout(Component container, FocusGroup group) {
        query = new CustomerBalanceQuery();
        query.getComponent();
        query.setDate(getYesterday()); // default statement date to yesterday
        browser = new CustomerBalanceBrowser(query, new DefaultLayoutContext(getContext(), getHelpContext()));
        GroupBox box = GroupBoxFactory.create(browser.getComponent());
        container.add(box);
        group.add(browser.getFocusGroup());
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button set
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add("sendAll", new ActionListener() {
            public void onAction(ActionEvent event) {
                onSendAll();
            }
        });
        buttons.add("print", new ActionListener() {
            public void onAction(ActionEvent event) {
                onPrint();
            }
        });
        buttons.add("report", new ActionListener() {
            public void onAction(ActionEvent event) {
                onReport();
            }
        });
        buttons.add("endPeriod", new ActionListener() {
            public void onAction(ActionEvent event) {
                onEndPeriod();
            }
        });
    }

    /**
     * Invoked when the 'send all' button is pressed.
     */
    private void onSendAll() {
        if (checkStatementDate("reporting.statements.run.invalidDate")) {
            String title = Messages.get("reporting.statements.run.title");
            String message = Messages.get("reporting.statements.run.message");
            HelpContext help = getHelpContext().subtopic("confirmsend");
            final SendStatementsDialog dialog = new SendStatementsDialog(title, message, help);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    doSendAll(dialog.reprint());
                }
            });
            dialog.show();
        }
    }

    /**
     * Processes all customers matching the criteria.
     *
     * @param reprint if {@code true}, process statements that have been printed.
     */
    private void doSendAll(boolean reprint) {
        try {
            HelpContext help = getHelpContext().subtopic("send");
            StatementGenerator generator = new StatementGenerator(query, getContext(), getMailContext(), help);
            generator.setReprint(reprint);
            generateStatements(generator, true);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the 'print' button is pressed to print the selected statement.
     */
    private void onPrint() {
        if (checkStatementDate("reporting.statements.run.invalidDate")) {
            try {
                ObjectSet selected = browser.getSelected();
                if (selected != null) {
                    IMObjectReference ref = selected.getReference(CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
                    Party customer = (Party) IMObjectHelper.getObject(ref, getContext());
                    if (customer != null) {
                        HelpContext help = getHelpContext().subtopic("print");
                        Context context = LocalContext.copy(getContext());
                        context.setPatient(null);
                        context.setCustomer(customer);
                        MailContext mailContext = new CustomerMailContext(context, help);
                        StatementGenerator generator = new StatementGenerator(
                                ref, query.getDate(), true, context, mailContext, help);
                        generator.setReprint(true);
                        generateStatements(generator, false);
                    }
                }
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
    }

    /**
     * Invoked when the 'end period' button is pressed.
     */
    private void onEndPeriod() {
        if (checkStatementDate("reporting.statements.eop.invalidDate")) {
            String title = Messages.get("reporting.statements.eop.title");
            String message = Messages.get("reporting.statements.eop.message");
            final HelpContext help = getHelpContext().subtopic("endperiod");
            final EndOfPeriodDialog dialog = new EndOfPeriodDialog(title, message, help);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    doEndPeriod(dialog.postCompletedInvoices(), help);
                }
            });
            dialog.show();
        }
    }

    /**
     * Runs end period.
     *
     * @param postCompletedInvoices if {@code true}, post completed invoices
     * @param help                  the help context
     */
    private void doEndPeriod(boolean postCompletedInvoices, HelpContext help) {
        try {
            EndOfPeriodGenerator generator = new EndOfPeriodGenerator(query.getDate(), postCompletedInvoices,
                                                                      getContext(), help);
            generator.setListener(new BatchProcessorListener() {
                public void completed() {
                    browser.query();
                }

                public void error(Throwable exception) {
                    ErrorHelper.show(exception);
                }
            });
            generator.process();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Generates statements.
     *
     * @param generator the statement generator
     * @param refresh   if {@code true}, refresh the browser on completion
     */
    private void generateStatements(final StatementGenerator generator, final boolean refresh) {
        generator.setListener(new BatchProcessorListener() {
            public void completed() {
                if (refresh) {
                    browser.query();
                }
            }

            public void error(Throwable exception) {
                generator.getProcessor().setCancel(true);
                ErrorHelper.show(exception);
            }
        });
        generator.process();
    }

    /**
     * Verfies that the statement date is at least a day prior to the current
     * date.
     *
     * @param errorKey the error message key, if the date is invalid
     * @return {@code true} if the statement date is less than today
     */
    private boolean checkStatementDate(String errorKey) {
        Date statementDate = query.getDate();
        Date date = getYesterday();
        if (date.compareTo(statementDate) < 0) {
            ErrorDialog.show(Messages.get(errorKey));
            return false;
        }
        return true;
    }

    /**
     * Returns yesterday's date.
     *
     * @return yesterday's date
     */
    private Date getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    /**
     * Invoked when the 'Report' button is pressed.
     */
    private void onReport() {
        try {
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(
                    query.getObjects(), "CUSTOMER_BALANCE", getContext());
            String type;
            if (query.queryAllBalances()) {
                type = Messages.get("reporting.statements.print.all");
            } else if (query.queryOverduebalances()) {
                type = Messages.get("reporting.statements.print.overdue");
            } else {
                type = Messages.get("reporting.statements.print.nonOverdue");
            }
            String title = Messages.format("imobject.print.title", type);
            HelpContext help = getHelpContext().subtopic("report");
            InteractiveIMPrinter<ObjectSet> iPrinter = new InteractiveIMPrinter<ObjectSet>(title, printer, getContext(),
                                                                                           help);
            iPrinter.setMailContext(getMailContext());
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
