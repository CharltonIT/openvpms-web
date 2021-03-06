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

package org.openvpms.web.workspace;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.pane.ContentPane;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Collections;
import java.util.List;


/**
 * Title pane.
 *
 * @author Tim Anderson
 */
public class TitlePane extends ContentPane {

    /**
     * The practice rules.
     */
    private final PracticeRules practiceRules;

    /**
     * The user rules.
     */
    private final UserRules userRules;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The location selector.
     */
    private SelectField locationSelector;

    /**
     * The style name.
     */
    private static final String STYLE = "TitlePane";


    /**
     * Construct a new {@code TitlePane}.
     *
     * @param practiceRules the practice rules
     * @param userRules     the user rules
     * @param context       the context
     */
    public TitlePane(PracticeRules practiceRules, UserRules userRules, Context context) {
        this.practiceRules = practiceRules;
        this.userRules = userRules;
        this.context = context;
        doLayout();
    }

    /**
     * Lay out the component.
     */
    protected void doLayout() {
        setStyleName(STYLE);

        Label logo = LabelFactory.create(null, "logo");
        RowLayoutData centre = new RowLayoutData();
        centre.setAlignment(new Alignment(Alignment.DEFAULT, Alignment.CENTER));
        logo.setLayoutData(centre);

        Label user = LabelFactory.create(null, "small");
        user.setText(Messages.format("label.user", getUserName()));

        Row locationUserRow = RowFactory.create("CellSpacing", user);

        List<Party> locations = getLocations();
        if (!locations.isEmpty()) {
            Party defLocation = getDefaultLocation();
            Label location = LabelFactory.create("app.location", "small");
            IMObjectListModel model = new IMObjectListModel(locations,
                                                            false, false);
            locationSelector = SelectFieldFactory.create(model);
            if (defLocation != null) {
                locationSelector.setSelectedItem(defLocation);
            }
            locationSelector.setCellRenderer(IMObjectListCellRenderer.NAME);
            locationUserRow.add(location, 0);
            locationUserRow.add(locationSelector, 1);
            locationSelector.addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    changeLocation();
                }
            });
        }

        Row inset = RowFactory.create("InsetX", locationUserRow);
        RowLayoutData right = new RowLayoutData();
        right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.BOTTOM));
        right.setWidth(new Extent(100, Extent.PERCENT));
        inset.setLayoutData(right);
        Row row = RowFactory.create(logo, inset);
        add(row);
    }

    /**
     * Returns the user name for the current user.
     *
     * @return the user name
     */
    protected String getUserName() {
        User user = context.getUser();
        return (user != null) ? user.getName() : null;
    }

    /**
     * Changes the location.
     */
    private void changeLocation() {
        Party selected = (Party) locationSelector.getSelectedItem();
        context.setLocation(selected);
    }

    /**
     * Returns the locations for the current user.
     *
     * @return the locations
     */
    private List<Party> getLocations() {
        List<Party> locations = Collections.emptyList();
        User user = context.getUser();
        if (user != null) {
            locations = userRules.getLocations(user);
            if (locations.isEmpty()) {
                Party practice = context.getPractice();
                if (practice != null) {
                    locations = practiceRules.getLocations(practice);
                }
            }
            IMObjectSorter.sort(locations, "name");
        }
        return locations;
    }

    /**
     * Returns the default location for the current user.
     *
     * @return the default location, or {@code null} if none is found
     */
    private Party getDefaultLocation() {
        Party location = null;
        User user = context.getUser();
        if (user != null) {
            location = userRules.getDefaultLocation(user);
            if (location == null) {
                Party practice = context.getPractice();
                if (practice != null) {
                    location = practiceRules.getDefaultLocation(practice);
                }
            }
        }
        return location;
    }

}
