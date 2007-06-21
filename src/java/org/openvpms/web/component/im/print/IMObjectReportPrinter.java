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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.print;

import org.openvpms.archetype.rules.doc.DocumentException;
import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.NotFound;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.IMReport;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.ReportException;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.system.ServiceHelper;


/**
 * Prints reports for {@link IMObject}s generated by {@link IMReport}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectReportPrinter<T extends IMObject>
        extends AbstractIMReportPrinter<T> {

    /**
     * The archetype short name to determine which template to use.
     */
    private final String shortName;

    /**
     * The document template to use.
     */
    private Entity template;


    /**
     * Constructs a new <tt>IMObjectReportPrinter</tt>.
     *
     * @param object the object to print
     * @throws OpenVPMSException for any error
     */
    public IMObjectReportPrinter(T object) {
        this(object, null);
    }

    /**
     * Constructs a new <tt>IMObjectReportPrinter</tt>.
     *
     * @param object   the object to print
     * @param template the document template to use. May be <tt>null</tt>
     * @throws OpenVPMSException for any error
     */
    public IMObjectReportPrinter(T object, Entity template) {
        super(object);
        shortName = object.getArchetypeId().getShortName();
        this.template = template;
        setInteractive(getInteractive(getTemplate(), getDefaultPrinter()));
    }

    /**
     * Constructs a new <tt>IMReportPrinter</tt> to print a collection of
     * objects.
     *
     * @param objects   the objects to print
     * @param shortName the archetype short name to determine the template to
     *                  use
     * @throws OpenVPMSException for any error
     */
    public IMObjectReportPrinter(Iterable<T> objects, String shortName) {
        this(objects, shortName, null);
    }

    /**
     * Constructs a new <tt>IMReportPrinter</tt> to print a collection of
     * objects.
     *
     * @param objects   the objects to print
     * @param shortName the archetype short name to determine the template to
     *                  use
     * @param template  the document template to use. May be <tt>null</tt>
     * @throws OpenVPMSException for any error
     */
    public IMObjectReportPrinter(Iterable<T> objects, String shortName,
                                 Entity template) {
        super(objects);
        this.shortName = shortName;
        this.template = template;
        setInteractive(getInteractive(getTemplate(), getDefaultPrinter()));
    }

    /**
     * Returns the default printer for an object.
     *
     * @return the default printer, or <code>null</code> if there is
     *         none defined
     * @throws OpenVPMSException for any error
     */
    public String getDefaultPrinter() {
        Entity template = getTemplate();
        return (template != null) ? getDefaultPrinter(template) : null;
    }

    /**
     * Returns a display name for the objects being printed.
     *
     * @return a display name for the objects being printed
     */
    public String getDisplayName() {
        return DescriptorHelper.getDisplayName(shortName);
    }

    /**
     * Creates a new report.
     *
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    protected IMReport<T> createReport() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Entity template = getTemplate();
        DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
        IMReport<IMObject> report;
        if (template == null) {
            report = ReportFactory.createIMObjectReport(shortName, service,
                                                        handlers);
        } else {
            TemplateHelper helper = new TemplateHelper();
            Document doc = helper.getDocumentFromTemplate(template);
            if (doc == null) {
                throw new DocumentException(NotFound);
            }
            report = ReportFactory.createIMObjectReport(doc, service,
                                                        handlers);
        }
        return (IMReport<T>) report;
    }

    /**
     * Returns the print properties for an object.
     *
     * @param printer the printer
     * @return the print properties
     * @throws OpenVPMSException for any error
     */
    @Override
    protected PrintProperties getProperties(String printer) {
        return getProperties(printer, getTemplate());
    }

    /**
     * Returns the document template.
     *
     * @return the document template, or <tt>null</tt> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Entity getTemplate() {
        if (template == null) {
            TemplateHelper helper = new TemplateHelper();
            template = helper.getTemplateForArchetype(shortName);
        }
        return template;
    }

}
