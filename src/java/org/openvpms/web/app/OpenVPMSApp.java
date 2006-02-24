package org.openvpms.web.app;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Window;

import org.openvpms.web.app.login.LoginPane;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.resource.util.Styles;


/**
 * The entry point to the OpenVPMS application.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class OpenVPMSApp extends ContextApplicationInstance {

    /**
     * The window.
     */
    private Window _window;


    /**
     * Invoked to initialize the application, returning the default window.
     *
     * @return the default window of the application
     */
    public Window init() {
        setStyleSheet(Styles.DEFAULT_STYLE_SHEET);
        _window = new Window();
        _window.setTitle("OpenVPMS");
        _window.setContent(new LoginPane());
        return _window;
    }

    /**
     * Returns the instance associated with the current thread.
     *
     * @return the current instance, or <code>null</code>
     */
    public static OpenVPMSApp getInstance() {
        return (OpenVPMSApp) ApplicationInstance.getActive();
    }

    /**
     * Sets the content pane.
     *
     * @param content the content pane
     */
    public void setContent(ContentPane content) {
        _window.setContent(content);
    }

    /**
     * Returns the content pane.
     *
     * @return the content pane
     */
    public ContentPane getContent() {
        return _window.getContent();
    }

    /**
     * Logout the current user.
     */
    public void logout() {
        getDefaultWindow().removeAll();
        setContent(new LoginPane());
        clearContext();
    }
}
