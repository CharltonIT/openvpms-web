package org.openvpms.web.app.customer;

import org.openvpms.web.app.subsystem.DummyWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Customer subsystem.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class CustomerSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>CustomerSubsystem</code>.
     */
    public CustomerSubsystem() {
        super("customer");
        addWorkspace(new InformationWorkspace());
        addWorkspace(new DummyWorkspace("customer", "document"));
        addWorkspace(new EstimationWorkspace());
        addWorkspace(new InvoiceWorkspace());
        addWorkspace(new DummyWorkspace("customer", "payment"));
        addWorkspace(new AccountWorkspace());
    }
}
