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

package org.openvpms.web.component.im.print;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.print.AbstractPrinter;

import java.util.Map;


/**
 * Abstract implementation of the {@link IMPrinter} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMPrinter<T>
        extends AbstractPrinter implements IMPrinter<T> {

    /**
     * The reporter.
     */
    private final Reporter<T> reporter;


    /**
     * Constructs an {@link AbstractIMPrinter}.
     *
     * @param reporter the reporter
     */
    public AbstractIMPrinter(Reporter<T> reporter) {
        this.reporter = reporter;
    }

    /**
     * Returns the objects being printed.
     *
     * @return the objects being printed
     */
    public Iterable<T> getObjects() {
        return reporter.getObjects();
    }

    /**
     * Prints the object.
     *
     * @param printer the printer name. May be {@code null}
     * @throws PrintException    if {@code printer} is null and {@link #getDefaultPrinter()} also returns {@code null}
     * @throws OpenVPMSException for any error
     */
    public void print(String printer) {
        if (printer == null) {
            printer = getDefaultPrinter();
        }
        if (printer == null) {
            throw new PrintException(PrintException.ErrorCode.NoPrinter);
        }
        reporter.print(getObjects(), getProperties(printer));
    }

    /**
     * Sets parameters to pass to the report.
     *
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     */
    public void setParameters(Map<String, Object> parameters) {
        reporter.setParameters(parameters);
    }

    /**
     * Returns a map of parameters names and their values, to pass to the
     * report.
     *
     * @return a map of parameter names and their values. May be {@code null}
     */
    public Map<String, Object> getParameters() {
        return reporter.getParameters();
    }

    /**
     * Sets fields to pass to the report.
     *
     * @param fields a map of field names and their values, to pass to the report. May be {@code null}
     */
    public void setFields(Map<String, Object> fields) {
        reporter.setFields(fields);
    }

    /**
     * Returns a document corresponding to that which would be printed.
     *
     * @return a document
     * @throws OpenVPMSException for any error
     */
    public Document getDocument() {
        return reporter.getDocument();
    }

    /**
     * Returns a document corresponding to that which would be printed.
     *
     * @param mimeType the mime type. If {@code null} the default mime type associated with the report will be used.
     * @param email    if {@code true} indicates that the document will be emailed. Documents generated from templates
     *                 can perform custom formatting
     * @return a document
     * @throws OpenVPMSException for any error
     */
    public Document getDocument(String mimeType, boolean email) {
        return reporter.getDocument(mimeType, email);
    }

    /**
     * Returns the object being printed.
     *
     * @return the object being printed, or {@code null} if a collection
     *         is being printed
     */
    protected T getObject() {
        return reporter.getObject();
    }

    /**
     * Returns the reporter.
     *
     * @return the reporter
     */
    protected Reporter<T> getReporter() {
        return reporter;
    }
}
