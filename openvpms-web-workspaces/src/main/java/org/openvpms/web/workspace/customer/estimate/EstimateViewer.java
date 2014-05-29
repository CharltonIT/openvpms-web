/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openvpms.web.workspace.customer.estimate;

import java.util.List;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.echo.dialog.PopupDialog;
import static org.openvpms.web.echo.dialog.PopupDialog.CLOSE;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

/**
 *
 * @author benjamincharlton
 */
public class EstimateViewer extends PopupDialog {

    /**
     * The alerts to display.
     */
    private final List<Act> estimates;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The alerts table.
     */
    private PagedIMTable<Act> table;

    /**
     * The column containing the alerts table and optional viewer.
     */
    private Column column;

    /**
     * The alert viewer.
     */
    private Component viewer;
    /**
     * Constructs an {@code AlertsViewer} to display alerts for multiple alert types.
     *
     * @param alerts  the alerts
     * @param context the context
     * @param help    the help context
     */
    public EstimateViewer(List<Act> estimate, Context context, HelpContext help) {
        super(Messages.get("estimates.title"),"EstimatesViewer" , CLOSE, help);
        this.context = context;
        this.estimates = estimate;
        setModal(true);
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Column column = ColumnFactory.create("Inset", getComponent());
        getLayout().add(column);
    }

    /**
     * Renders the component.
     *
     * @return the component
     */
    private Component getComponent() {
        ResultSet<Act> set = new ListResultSet<Act>(estimates, 20);
        Model model = new Model();
        table = new PagedIMTable<Act>(model, set);

        table.getTable().setStyleName("EstimatesTableViewer");
        // this style disables the selection blur style used in other tables, as it hides white text

        column = ColumnFactory.create("CellSpacing", table);

        if (estimates.size() == 1) {
            show(estimates.get(0));
        } else {
            table.getTable().addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    showSelected();
                }
            });
        }
        return column;
    }

    /**
     * Displays the selected estimate.
     */
    private void showSelected() {
        Act estimate = table.getSelected();
        show(estimate);
    }

    /**
     * Shows an alert.
     *
     * @param estimate the alert to show. May be {@code null}
     */
    private void show(Act estimate) {
        if (estimate != null) {
            if (viewer != null) {
                column.remove(viewer);
            }
            if (estimate != null) {
                DefaultLayoutContext layout = new DefaultLayoutContext(context, getHelpContext());
                viewer = new IMObjectViewer(estimate, layout).getComponent();
            } else {
                viewer = LabelFactory.create("estimates.nodetail", "bold");
                ColumnLayoutData layout = new ColumnLayoutData();
                layout.setAlignment(Alignment.ALIGN_CENTER);
                viewer.setLayoutData(layout);
            }
            column.add(viewer);
        }
    }
    private LayoutContext createLayoutContext(Context context, HelpContext help) {
        LayoutContext result = new DefaultLayoutContext(context, help);
        result.setEdit(true); // hack to disable hyperlinks
        TableComponentFactory factory = new TableComponentFactory(result);
        result.setComponentFactory(factory);
        return result;
    }

    private class Model extends AbstractActTableModel {

        public Model() {
            super(new String[]{EstimateArchetypes.ESTIMATE}, createLayoutContext(context, getHelpContext()));
        }
    }
}
