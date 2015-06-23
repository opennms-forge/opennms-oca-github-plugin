package org.opennms.github.plugins.oca;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class ContributorAgreementServiceTest {

    @Test
    public void testOcaSignedOK() throws IOException, URISyntaxException {
        OCAChecker ocaChecker = new OCAChecker(ContributorAgreementServiceTest.class.getResource("/oca-source.txt"));
        Assert.assertEquals(Boolean.TRUE, ocaChecker.hasUserOCASigned("mvrueden"));
        Assert.assertEquals(Boolean.TRUE, ocaChecker.hasUserOCASigned("mfuhrmann"));
        Assert.assertEquals(Boolean.TRUE, ocaChecker.hasUserOCASigned("indigo423"));
        Assert.assertEquals(Boolean.TRUE, ocaChecker.hasUserOCASigned("fooker"));
        Assert.assertEquals(Boolean.FALSE, ocaChecker.hasUserOCASigned("Ulf"));
    }

    @Test
    public void testWikiDownload() throws IOException, URISyntaxException {
        OCAChecker ocaChecker = new OCAChecker(new URL(ContributorAgreementService.OCA_WIKI_RAW_URL));
        Assert.assertNotNull(ocaChecker.getContributor("mvrueden"));
    }

    // checks that all contributors can be checked without throwing any exceptions
    @Test
    public void testAllContributor() throws IOException, URISyntaxException {
        OCAChecker ocaChecker = new OCAChecker(ContributorAgreementServiceTest.class.getResource("/oca-source.txt"));
        Contributor contributor = ocaChecker.getContributor("XXX_ULF_XXX");
        Assert.assertNull(contributor);
    }
}
