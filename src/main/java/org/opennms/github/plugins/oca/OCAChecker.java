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

import com.google.common.io.ByteStreams;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCAChecker {

    private final URL ocaSource;
    private boolean forceReload;
    private List<Contributor> contributorList;
    private long lastReload;
    private final String mappingFileLocation;

    public OCAChecker(URL ocaSource, String mappingFileLocation) {
        this.ocaSource = ocaSource;
        this.mappingFileLocation = mappingFileLocation;
    }

    public boolean hasUserOCASigned(Committer committer) throws IOException, URISyntaxException {
        if (shouldReload()) {
            lastReload = System.currentTimeMillis();
            contributorList = loadFromPath();
            mergeWithManuallyApproved(contributorList);
            forceReload = false;
        }

        for (Contributor eachContributor : contributorList) {
            // we have a github id, so we only have to look in the contributors list
            if (committer.getGithubId() != null &&  committer.getGithubId().equalsIgnoreCase(eachContributor.getGithubId())) {
                return true;
            }
            if (committer.getEmail() != null && eachContributor.matchesEmail(committer.getEmail())){
                return true;
            }
        }
        return false;

    }

    private void mergeWithManuallyApproved(List<Contributor> contributorSet) throws IOException {
        Properties properties = loadProperties();

        // we iterate over each mapping and assign the email address to
        // the contributor
        for (Map.Entry<Object, Object> eachEntry : properties.entrySet()) {
            for (Contributor eachContributor : contributorSet) {
                String githubId = (String) eachEntry.getValue();
                if (eachContributor.getGithubId().equalsIgnoreCase(githubId)) {
                    eachContributor.addEmail((String) eachEntry.getKey());
                }
            }
        }
    }

    private Properties loadProperties() throws IOException {
        Properties p = new Properties();
        if (mappingFileLocation != null) {
            Path path = Paths.get(mappingFileLocation);
            if (Files.exists(path)) {
                p.load(new FileInputStream(mappingFileLocation));
            }
        }
        return p;
    }

    private void saveProperties(Properties p) throws IOException {
        Objects.requireNonNull(mappingFileLocation, "No mapping file location defined.");
        try (FileOutputStream outputStream = new FileOutputStream(mappingFileLocation)) {
            p.store(outputStream, "Additional email to github id mappings to consider for OCA-Check");
        }
    }

    public void setForceReload(boolean forceReload) {
        this.forceReload = forceReload;
    }

    public void approve(String githubId, String email) throws IOException {
        Properties p = loadProperties();
        if (!p.containsKey(email)) {
            p.put(email, githubId);
            saveProperties(p);
            setForceReload(true);
        }
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
