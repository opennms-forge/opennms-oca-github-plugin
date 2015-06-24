package org.opennms.github.plugins.oca;

import com.google.common.base.Throwables;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    protected static final String PROPERTY_FILE_LOCATION_PROPERTY_NAME = "property.file";

    public static final String GITHUB_API_URL = getProperty("github.api.url", "https://api.github.com");

    public static final String GITHUB_API_TOKEN = getProperty("github.api.token", "");

    public static final String GITHUB_USER  = getProperty("github.user", "OpenNMS");

    public static final String GITHUB_REPO = getProperty("github.repository", "opennms");

    public static final String GITHUB_WEBHOOK_SECRET  = getProperty("github.webhook.secret", "");

    public static final String OCA_REDO_COMMENT_REGEXP = getProperty("oca.redo.regexp", ".*alfred.*oca.*");

    public static final String OCA_WIKI_URL_PAGE_RAW_EDIT = getProperty("oca.url.edit-raw-page", "http://www.opennms.org/w/index.php?title=Executed_contributor_agreements&action=raw");

    private static Properties properties;

    protected static String getProperty(String key, String defaultValue) {
        if (properties == null) {
            properties = new Properties();
            String location = System.getProperty(PROPERTY_FILE_LOCATION_PROPERTY_NAME);
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
