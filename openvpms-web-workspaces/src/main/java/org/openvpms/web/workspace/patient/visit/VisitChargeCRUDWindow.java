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

package org.openvpms.web.workspace.patient.visit;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.DefaultActActions;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.workspace.AbstractCRUDWindow;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.message.InformationMessage;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;


/**
 * Visit charge CRUD window.
 *
 * @author Tim Anderson
 */
public class VisitChargeCRUDWindow extends AbstractCRUDWindow<FinancialAct> implements VisitEditorTab {

    /**
     * Completed button identifier.
     */
    public static final String COMPLETED_ID = "button.completed";

    /**
     * In Progress button identifier.
     */
    public static final String IN_PROGRESS_ID = "button.inprogress";

    /**
     * The event.
     */
    private final Act event;

    /**
     * The charge editor.
     */
    private VisitChargeEditor editor;

    /**
     * Customer order rules.
     */
    private final OrderRules rules;

    /**
     * Determines if the charge is posted.
     */
    private boolean posted;

    /**
     * The container.
     */
    private Component container = ColumnFactory.create();

    /**
     * The tab identifier.
     */
    private int id;

    /**
     * The current order message.
     */
    private Component message;


    /**
     * Constructs a {@link VisitChargeCRUDWindow}.
     *
     * @param event   the event
     * @param context the context
     * @param help    the help context
     */
    public VisitChargeCRUDWindow(Act event, Context context, HelpContext help) {
        super(Archetypes.create(CustomerAccountArchetypes.INVOICE, FinancialAct.class),
              DefaultActActions.<FinancialAct>getInstance(), context, help);
        this.event = event;
        rules = ServiceHelper.getBean(OrderRules.class);
    }

    /**
     * Returns the identifier of this tab.
     *
     * @return the tab identifier
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * Sets the identifier of this tab.
     *
     * @param id the tab identifier
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(FinancialAct object) {
        container.removeAll();
        if (object != null) {
            posted = ActStatus.POSTED.equals(object.getStatus());
            if (posted) {
                IMObjectViewer viewer = new IMObjectViewer(object, new DefaultLayoutContext(getContext(),
                                                                                            getHelpContext()));
                container.add(viewer.getComponent());
                editor = null;
            } else {
                HelpContext edit = createEditTopic(object);
                editor = createVisitChargeEditor(object, event, createLayoutContext(edit));
                container.add(editor.getComponent());
            }
        } else {
            editor = null;
        }
        super.setObject(object);
    }

    /**
     * Returns the charge editor.
     *
     * @return the charge editor. May be {@code null}
     */
    public VisitChargeEditor getEditor() {
        return editor;
    }

    /**
     * Creates and edits a new object.
     */
    @Override
    public void create() {
        if (editor == null) {
            IArchetypeService archetypeService = ServiceHelper.getArchetypeService();
            FinancialAct invoice = (FinancialAct) archetypeService.create(CustomerAccountArchetypes.INVOICE);
            setObject(invoice);
        }
    }

    /**
     * Invoked when the tab is displayed.
     */
    @Override
    public void show() {
        checkOrders();
        if (editor != null) {
            editor.getFocusGroup().setFocus();
        }
    }

    /**
     * Saves the invoice.
     *
     * @return {@code true} if the invoice was saved
     */
    public boolean save() {
        boolean result;
        if (editor != null && !posted) {
            Validator validator = new DefaultValidator();
            if (editor.validate(validator)) {
                result = SaveHelper.save(editor);
                posted = ActStatus.POSTED.equals(getObject().getStatus());
            } else {
                result = false;
                ValidationHelper.showError(validator);
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Returns the CRUD window for the tab.
     *
     * @return the CRUD window for the tab, or {@code null} if the tab doesn't provide one
     */
    @Override
    public CRUDWindow<? extends Act> getWindow() {
        return this;
    }

    /**
     * Marks the charge IN_PROGRESS and saves it.
     *
     * @return {@code true} if the charge was updated
     */
    public boolean inProgress() {
        boolean result = false;
        if (editor != null && !posted) {
            editor.setStatus(ActStatus.IN_PROGRESS);
            result = save();
        }
        return result;
    }

    /**
     * Marks the charge COMPLETED and saves it.
     *
     * @return {@code true} if the charge was updated
     */
    public boolean complete() {
        boolean result = false;
        if (editor != null && !posted) {
            editor.setStatus(ActStatus.COMPLETED);
            result = save();
        }
        return result;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        enableButtons(getButtons(), getObject() != null);
        return container;
    }

    /**
     * Creates a new visit charge editor.
     *
     * @param charge  the charge
     * @param event   the clinical event
     * @param context the layout context
     * @return a new visit charge editor
     */
    protected VisitChargeEditor createVisitChargeEditor(FinancialAct charge, Act event, LayoutContext context) {
        return new VisitChargeEditor(charge, event, context);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        // button layout is handled by the parent dialog
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        if (buttons != null) {
            if (enable) {
                enable = !posted;
            }
            buttons.setEnabled(IN_PROGRESS_ID, enable);
            buttons.setEnabled(COMPLETED_ID, enable);
        }
    }

    /**
     * Determines if there are any pending orders, displaying a message if there are.
     */
    private void checkOrders() {
        if (message != null) {
            container.remove(message);
            message = null;
        }
        Party customer = getContext().getCustomer();
        Party patient = getContext().getPatient();
        if (customer != null && patient != null) {
            if (rules.hasOrders(customer, patient)) {
                message = new InformationMessage(Messages.format("customer.charge.pendingorders", customer.getName()));
                container.add(message, 0);
            }
        }
    }

}
