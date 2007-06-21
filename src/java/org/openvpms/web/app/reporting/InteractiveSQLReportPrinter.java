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

import org.openvpms.report.ParameterType;
import org.openvpms.web.component.dialog.PrintDialog;
import org.openvpms.web.component.print.InteractivePrinter;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;

import java.util.ArrayList;
import java.util.List;


/**
 * Interactive printer for {@link SQLReportPrinter}. Pops up a dialog with
 * options to print, preview, or cancel, and supply the report with parameters.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InteractiveSQLReportPrinter extends InteractivePrinter {

    /**
     * Constructs a new <tt>InteractiveSQLReportPrinter</tt>.
     *
     * @param printer the printer to delegate to
     */
    public InteractiveSQLReportPrinter(SQLReportPrinter printer) {
        super(printer);
    }

    /**
     * Creates a new print dialog.
     *
     * @return a new print dialog
     */
    @Override
    protected PrintDialog createDialog() {
        final SQLReportPrinter printer = getPrinter();
        return new SQLReportDialog(getProperties()) {

            @Override
            protected void doPrint() {
                printer.setParameters(getValues());
            }

            @Override
            protected void doPreview() {
                printer.setParameters(getValues());
                doPrintPreview();
            }
        };
    }

    /**
     * Prints the object.
     *
     * @param printerName the printer name. May be <tt>null</tt>
     */
    @Override
    protected void doPrint(String printerName) {
        super.doPrint(printerName);
    }

    /**
     * Returns the underlying printer.
     *
     * @return the printer
     */
    protected SQLReportPrinter getPrinter() {
        return (SQLReportPrinter) super.getPrinter();
    }

    /**
     * Returns the user-configurable report properties.
     *
     * @return the corresponding list of user-configurable properties
     */
    private List<Property> getProperties() {
        List<Property> result = new ArrayList<Property>();
        for (ParameterType type : getPrinter().getParameterTypes()) {
            if (!type.isSystem()) {
                SimpleProperty property = new SimpleProperty(type.getName(),
                                                             type.getType());
                property.setDescription(type.getDescription());
                if (property.isBoolean() || property.isString()
                        || property.isNumeric() || property.isDate()) {
                    Object defaultValue = type.getDefaultValue();
                    if (defaultValue != null) {
                        property.setValue(defaultValue);
                    }
                    result.add(property);
                }
            }
        }
        return result;
    }

}
