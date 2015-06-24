package org.opennms.github.plugins.oca;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

public class ConfigTest {

    @After
    public void resetProperties() throws IllegalAccessException, NoSuchFieldException {
        System.clearProperty(Config.PROPERTY_FILE_LOCATION_PROPERTY_NAME);
        Field field = Config.class.getDeclaredField("properties");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    public void testDefault() {
        Assert.assertNotNull(Config.GITHUB_API_URL);
        Assert.assertNotNull(Config.GITHUB_API_TOKEN);
        Assert.assertNotNull(Config.GITHUB_USER);
        Assert.assertNotNull(Config.GITHUB_REPO);
        Assert.assertNotNull(Config.OCA_WIKI_URL_PAGE_RAW_EDIT);
        Assert.assertNotNull(Config.OCA_REDO_COMMENT_REGEXP);
    }

    // checks that the default properties can be overridden by a properties file
    @Test
    public void testCustom() {
        System.setProperty(Config.PROPERTY_FILE_LOCATION_PROPERTY_NAME, "target/test-classes/custom.properties");

        Assert.assertEquals(Config.getProperty("github.api.url", "bla"), "http://opennms.org");
        Assert.assertEquals(Config.getProperty("github.api.token", "bla"), "ulf ulf ulf");
        Assert.assertEquals(Config.getProperty("github.user", "bla"), "custom-user");
        Assert.assertEquals(Config.getProperty("github.repository", "bla"), "custom-repository");

        Assert.assertEquals(Config.getProperty("oca.redo.regexp", "bla"), "custom regexp");
        Assert.assertEquals(Config.getProperty("oca.url.edit-raw-page", "bla"), "custom page");
    }
}
