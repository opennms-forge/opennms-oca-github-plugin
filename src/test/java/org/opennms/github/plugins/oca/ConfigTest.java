/**
 * This file is part of oca-github-plugin.
 *
 * oca-github-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * oca-github-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with oca-github-plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennms.github.plugins.oca;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

public class ConfigTest {

    @After
    public void resetProperties() throws IllegalAccessException, NoSuchFieldException {
        System.clearProperty(Config.PROPERTY_FILE_LOCATION_PROPERTY_KEY);
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
        Assert.assertNotNull(Config.GITHUB_WEBHOOK_SECRET);
        Assert.assertNotNull(Config.OCA_WIKI_URL_PAGE_RAW_EDIT);
        Assert.assertNotNull(Config.OCA_REDO_COMMENT_REGEXP);
        Assert.assertNotNull(Config.OCA_MANUALLY_APPROVE);
        Assert.assertNotNull(Config.OCA_TRUSTED_TEAM);
        Assert.assertNull(Config.MAPPING_FILE_LOCATION);
    }

    // checks that the default properties can be overridden by a properties file
    @Test
    public void testCustom() {
        System.setProperty(Config.PROPERTY_FILE_LOCATION_PROPERTY_KEY, "target/test-classes/custom.properties");

        Assert.assertEquals(Config.getProperty("github.api.url", "bla"), "http://opennms.org");
        Assert.assertEquals(Config.getProperty("github.api.token", "bla"), "ulf ulf ulf");
        Assert.assertEquals(Config.getProperty("github.user", "bla"), "custom-user");
        Assert.assertEquals(Config.getProperty("github.repository", "bla"), "custom-repository");

        Assert.assertEquals(Config.getProperty("github.webhook.secret", "bla"), "some custom secret");
        Assert.assertEquals(Config.getProperty("oca.regexp.approve", "bla"), "custom regexp 2");
        Assert.assertEquals(Config.getProperty("oca.regexp.redo", "bla"), "custom regexp");
        Assert.assertEquals(Config.getProperty("oca.trusted.team", "bla"), "custom team");
        Assert.assertEquals(Config.getProperty("oca.url.edit-raw-page", "bla"), "custom page");

        Assert.assertEquals(Config.getProperty("mapping.file.location", "bla"), "/tmp/dummy.properties");
    }
}
