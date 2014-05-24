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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.workflow.investigation;

import echopointng.GroupBox;
import java.util.Iterator;
import java.util.List;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.patient.InvestigationActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GroupBoxFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.workspace.reporting.AbstractReportingWorkspace;

/**
 * Workspace to display list of investigation results.
 *
 * @author Tim Anderson
 */
public class InvestigationsWorkspace extends AbstractReportingWorkspace<Act> {

    /**
     * Constructs an {@code InvestigationsWorkspace}.
     *
     * @param context the context
     * @param mailContext the mail context
     */
    private DefaultIMObjectTableBrowser<Act> browser;

    public InvestigationsWorkspace(Context context, MailContext mailContext) {
        super("workflow", "investigation", Act.class, context, mailContext);
    }

    /**
     * Lays out the components.
     *
     * @param container the container
     * @param group the focus group
     */
    @Override
    protected void doLayout(Component container, FocusGroup group) {
        Query<Act> query = new InvestigationsQuery();

        // create a layout context, with hyperlinks enabled
        LayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        TableComponentFactory factory = new TableComponentFactory(context);
        context.setComponentFactory(factory);
        context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);

        InvestigationsTableModel model = new InvestigationsTableModel(context);
        browser = new DefaultIMObjectTableBrowser<Act>(query, model, context);
        browser.addBrowserListener(new BrowserListener<Act>() {
            @Override
            public void query() {
                selectFirst();
            }
            @Override
            public void selected(Act act) {
                
                setObject(act);
            }

            @Override
            public void browsed(Act act) {
                setObject(act);
            }
        });
        GroupBox box = GroupBoxFactory.create(browser.getComponent());
        container.add(box);
        group.add(browser.getFocusGroup());
        browser.setSelectMultiple();
    }

    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add("print", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onPrint();
            }
        });
        buttons.add("completed", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onComplete();
            }
        });
        buttons.add("reviewed", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onReviewed();
            }
        });
    }

    /**
     * Determines if the workspace should be refreshed.
     *
     * @return {@code true}
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

    private void onPrint() {
        try {
            Act selected = browser.getSelected();
            if (selected != null) {
                ActBean act = new ActBean(selected);
                IMObject ref = act.getObject("docReference");
                if (ref != null){
                    print(selected);
                }                    
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    private void onComplete() {
        try {
            List<Act>  selected= browser.getMultipleSelected();
            if (selected != null) {
            Iterator<Act> iterator = selected.iterator();
            while(iterator.hasNext()) {
            Act act = iterator.next();
            
                act.setStatus(InvestigationActStatus.COMPLETED);
                SaveHelper.save(act);
            }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    private void onReviewed() {
        try {
            List<Act> selected = browser.getMultipleSelected();
            if (selected != null) {
            Iterator<Act> iterator = selected.iterator();
             while(iterator.hasNext()) {
                Act act = iterator.next();
                ActBean actbean = new ActBean(act);
                actbean.setValue("reviewed", true);
                boolean save = SaveHelper.save(act);
            }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    protected void print(Act object) {
        try {
            IMPrinter<Act> printer = createPrinter(object);
            printer.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    protected IMPrinter<Act> createPrinter(Act object) {
        ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object, getContext());
        IMPrinter<Act> printer = IMPrinterFactory.create(object, locator, getContext());

        InteractiveIMPrinter<Act> interactive = new InteractiveIMPrinter<Act>(printer, getContext(), getHelpContext());
        interactive.setMailContext(getMailContext());
        return interactive;
    }
    /**
     * Selects the first available act.
     */
    private void selectFirst() {
        List<Act> acts = browser.getObjects();
        if (!acts.isEmpty()) {
            Act current = acts.get(0);
            browser.setSelected(current);
            setObject(current);
        } else {
            setObject(null);
        }
    }
}
