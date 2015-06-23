package org.opennms.github.plugins.oca;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCAChecker {

    private final URL ocaSource;

    public OCAChecker(URL ocaSource) {
        this.ocaSource = ocaSource;
    }

    public boolean hasUserOCASigned(String user) throws IOException, URISyntaxException {
        Contributor contributor = getContributor(user);
        return contributor != null;
    }

    private List<Contributor> loadFromPath() throws IOException, URISyntaxException {
        return loadFromPath(ocaSource);
    }

    public Contributor getContributor(String user) throws IOException, URISyntaxException {
        List<Contributor> contributorList = loadFromPath();

        for (Contributor eachContributor : contributorList) {
            if (eachContributor.getGithubId().equals(user)) {
                return eachContributor;
            }
        }
        return null;
    }

    private static List<Contributor> loadFromPath(URL url) throws IOException, URISyntaxException {
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        ByteStreams.copy(url.openStream(), contentStream);
        String content = contentStream.toString();

        /*
         * Reg-Exp pattern to extract all contributors from the contributors list.
         * There are four groups:
         * 1. Name
         * 2. Company
         * 3. github/sourceforge url
         * 4. github/sourceforge username
         */
        List<Contributor> contributorList = new ArrayList<>();
        Pattern p = Pattern.compile("\\|-\\s*\\|(.*\\s*)\\|(.*\\s*)\\|\\s*\\[(.*) (.*)\\]", Pattern.MULTILINE);
        Matcher matcher = p.matcher(content);
        while (matcher.find()) {
            Contributor contributor = new Contributor();
            contributor.setName(matcher.group(1).trim());
            contributor.setCompany(matcher.group(2).trim());
            //contributor.githubUrl = matcher.group(3).trim();
            contributor.setGithubId(matcher.group(4).trim());
            contributorList.add(contributor);
        }
        return contributorList;
    }
}
