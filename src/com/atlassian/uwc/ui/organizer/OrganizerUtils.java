package com.atlassian.uwc.ui.organizer;

import biz.artemis.confluence.xmlrpcwrapper.ConfluenceServerSettings;
import com.atlassian.uwc.ui.ConfluenceSettingsForm;
import com.atlassian.uwc.ui.UWCForm2;

/**
 * Methods used across the organizer and also ones that interface
 * with the UWC system which is considered external. A possible goal
 * is that the Organizer gets split off from the UWC at some point.
 */
public class OrganizerUtils {
    /** sigleton instance ref */
    private static OrganizerUtils instance;
    /** singleton getInstance method */
    public static OrganizerUtils getInstance() {
        if (instance==null) {
            instance = new OrganizerUtils();
        }
        return instance;
    }
    private OrganizerUtils() {}

   /**
     * Grab a ConfluenceServerSettings object which is necessary
     * to perform remote actions. The server settings in this
     * case are populated by what's in the UWC Form
     *
     * @return
     */
    public ConfluenceServerSettings getConfluenceSettings() {
        ConfluenceServerSettings css = new ConfluenceServerSettings();
        ConfluenceSettingsForm csf = UWCForm2.getInstance().confluenceSettingsForm;

        css.login = csf.getLogin();
        css.password = csf.getPassword();
        css.spaceKey = csf.getSpaceName();
        css.url = csf.getUrl();
        return css;
    }
}
