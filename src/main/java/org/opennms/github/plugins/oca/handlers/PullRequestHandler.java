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
package org.opennms.github.plugins.oca.handlers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.github.plugins.oca.Committer;
import org.opennms.github.plugins.oca.Context;
import org.opennms.github.plugins.oca.GithubApi;
import org.opennms.github.plugins.oca.OCAChecker;

public class PullRequestHandler extends AbstractHandler {

    public PullRequestHandler(GithubApi githubApi) {
        super(githubApi);
    }

    public Response handle(OCAChecker ocaChecker, String payload) throws IOException, URISyntaxException {
        final String pullRequestNumber = extractPullRequestNumber(payload);
        final String sha = extractSha(payload);
        final Set<Committer> committerSet = getCommitterSet(pullRequestNumber);
        final List<Context> contextList = readStatus(sha);
        for (Committer eachCommitter : committerSet) {
            boolean isAlreadyInformed = isAlreadyInformed(contextList, eachCommitter);
            boolean hasOcaSigned = updateStatus(sha, eachCommitter, ocaChecker);

            // If user was already informed, do not send "welcome message"
            if (!hasOcaSigned && !isAlreadyInformed) {
                String content = loadWelcomeMessage(eachCommitter);
                getGithubApi().createCommentOnIssue(pullRequestNumber, content);
            }
        }
        return null;
    }

    private String extractPullRequestNumber(String payload) {
        JSONObject payloadObject = new JSONObject(payload);
        return Long.toString(payloadObject.getLong("number"));
    }

    private String extractSha(String payload) {
        JSONObject payloadObject = new JSONObject(payload);
        return payloadObject.getJSONObject("pull_request").getJSONObject("head").getString("sha");
    }

    private String loadWelcomeMessage(Committer contributor) throws IOException, URISyntaxException {
        final URL resourceURL = getClass().getResource("/oca-welcome.md");
        final byte[] bytes = Files.readAllBytes(Paths.get(resourceURL.getPath()));

        String content = new String(bytes);
        String githubId;
        if (contributor.getGithubId() != null) {
            githubId = String.format("@%s", contributor.getGithubId());
        } else {
            githubId = String.format("%s (%s)", contributor.getName(), contributor.getEmail());
        }
        content = content.replaceAll(":githubid:", githubId);
        return content;
    }

    protected static boolean isAlreadyInformed(List<Context> contextList, Committer committer) {
        final String committerId = committer.getGithubId() != null ? committer.getGithubId() : committer.getEmail();
        final Optional<Context> first = contextList.stream().filter(c -> c.getName().equals("OCA " + committerId)).findFirst();
        return first.isPresent();
    }

    protected List<Context> readStatus(final String sha) throws IOException {
        final String status = getGithubApi().readStatus(sha);
        final JSONArray statusArray = new JSONArray(status);
        final Set<Context> contextList = new HashSet<>();
        for (int i=0; i<statusArray.length(); i++) {
            final JSONObject contextObject = statusArray.getJSONObject(i);
            final String contextName = contextObject.getString("context");
            final String contextState = contextObject.getString("state");
            final Context context = new Context(contextName, GithubApi.State.createFrom(contextState));
            contextList.add(context);
        }
        return new ArrayList<>(contextList);
    }
}
