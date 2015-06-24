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
    private boolean forceReload;
    private List<Contributor> contributorList;
    private long lastReload;

    public OCAChecker(URL ocaSource) {
        this.ocaSource = ocaSource;
    }

    public boolean hasUserOCASigned(String user) throws IOException, URISyntaxException {
        Contributor contributor = getContributor(user);
        return contributor != null;
    }

    public Contributor getContributor(String user) throws IOException, URISyntaxException {
        if (shouldReload()) {
            lastReload = System.currentTimeMillis();
            contributorList = loadFromPath();
            forceReload = false;
        }

        for (Contributor eachContributor : contributorList) {
            if (eachContributor.getGithubId().equals(user)) {
                return eachContributor;
            }
        }
        return null;
    }

    public void setForceReload(boolean forceReload) {
        this.forceReload = forceReload;
    }

    private List<Contributor> loadFromPath() throws IOException, URISyntaxException {
        return loadFromPath(ocaSource);
    }

    private boolean shouldReload() {
        if (forceReload
                || contributorList == null
                || (System.currentTimeMillis() - lastReload >= 10000) /* Only reload every 10 seconds */ ) {
            return true;
        }
        return false;
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
