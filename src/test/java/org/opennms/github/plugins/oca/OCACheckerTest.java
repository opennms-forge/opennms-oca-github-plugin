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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class OCACheckerTest {

    @Test
    public void testOcaSignedOK() throws IOException, URISyntaxException {
        OCAChecker ocaChecker = new OCAChecker(OCACheckerTest.class.getResource("/oca-source.txt"), "mapping.properties");
        Assert.assertEquals(Boolean.TRUE, ocaChecker.hasUserOCASigned(createCommitter("mvrueden")));
        Assert.assertEquals(Boolean.TRUE, ocaChecker.hasUserOCASigned(createCommitter("mfuhrmann")));
        Assert.assertEquals(Boolean.TRUE, ocaChecker.hasUserOCASigned(createCommitter("indigo423")));
        Assert.assertEquals(Boolean.TRUE, ocaChecker.hasUserOCASigned(createCommitter("fooker")));
        Assert.assertEquals(Boolean.FALSE, ocaChecker.hasUserOCASigned(createCommitter("Ulf")));
    }

    @Test
    public void testWikiDownload() throws IOException, URISyntaxException {
        OCAChecker ocaChecker = new OCAChecker(new URL(Config.OCA_WIKI_URL_PAGE_RAW_EDIT), "mapping.properties");
        Assert.assertEquals(Boolean.TRUE, ocaChecker.hasUserOCASigned(createCommitter("mvrueden")));
    }

    // checks that all contributors can be checked without throwing any exceptions
    @Test
    public void testAllContributor() throws IOException, URISyntaxException {
        OCAChecker ocaChecker = new OCAChecker(getClass().getResource("/oca-source.txt"), "mapping.properties");
        Assert.assertEquals(Boolean.FALSE, ocaChecker.hasUserOCASigned(createCommitter("XXX_ULF_XXX")));
    }

    @Test
    public void testManuallyApproved() throws IOException, URISyntaxException {
        OCAChecker ocaChecker = new OCAChecker(getClass().getResource("/oca-source.txt"), "target/mapping.properties");
        ocaChecker.approve("mvrueden", "mvr@opennms.com");
        Assert.assertEquals(Boolean.TRUE, ocaChecker.hasUserOCASigned(createCommitter(null, "Markus von Rüden", "mvr@opennms.com")));
        Assert.assertEquals(Boolean.FALSE, ocaChecker.hasUserOCASigned(createCommitter(null, "Markus von Rüden", "mvrueden@opennms.com")));
    }

    private static Committer createCommitter(String githubId, String name, String email)  {
        Committer committer = new Committer();
        committer.setGithubId(githubId);
        committer.setName(name);
        committer.setEmail(email);
        return committer;
    }

    private static Committer createCommitter(String githubId) {
        return createCommitter(githubId, null, null);
    }
}
