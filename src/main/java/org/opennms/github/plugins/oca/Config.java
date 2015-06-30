package org.opennms.github.plugins.oca;

import com.google.common.base.Throwables;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    protected static final String PROPERTY_FILE_LOCATION_PROPERTY_KEY = "property.file";

    public static final String GITHUB_API_URL = getProperty("github.api.url", "https://api.github.com");

    public static final String GITHUB_API_TOKEN = getProperty("github.api.token", "");

    public static final String GITHUB_USER  = getProperty("github.user", "OpenNMS");

    public static final String GITHUB_REPO = getProperty("github.repository", "opennms");

    public static final String GITHUB_WEBHOOK_SECRET  = getProperty("github.webhook.secret", "");

    public static final String MAPPING_FILE_LOCATION = getProperty("mapping.file.location", null);

    public static final String OCA_TRUSTED_TEAM = getProperty("oca.trusted.team", "oca-admins");

    public static final String OCA_REDO_COMMENT_REGEXP = getProperty("oca.regexp.redo", ".*(alfred|ulf).*oca.*");

    public static final String OCA_MANUALLY_APPROVE = getProperty("oca.regexp.approve", ".*(ulf|alfred).*approve.* ([A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}).*as.* ([a-zA-Z0-9]{1,}[a-zA-Z0-9-]*).*");

    public static final String OCA_WIKI_URL_PAGE_RAW_EDIT = getProperty("oca.url.edit-raw-page", "http://www.opennms.org/w/index.php?title=Executed_contributor_agreements&action=raw");


    private static Properties properties;

    protected static String getProperty(String key, String defaultValue) {
        if (properties == null) {
            properties = new Properties();
            String location = System.getProperty(PROPERTY_FILE_LOCATION_PROPERTY_KEY);
            if (location != null) {
                try {
                    properties.load(new FileInputStream(location));
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            }
        }
        return properties.getProperty(key, defaultValue);
    }
}
