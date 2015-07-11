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

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.github.plugins.oca.Committer;
import org.opennms.github.plugins.oca.Config;
import org.opennms.github.plugins.oca.GithubApi;
import org.opennms.github.plugins.oca.OCAChecker;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssuecommentRequestHandler extends AbstractHandler {

    public IssuecommentRequestHandler(GithubApi githubApi) {
        super(githubApi);
    }

    @Override
    public Response handle(OCAChecker ocaChecker, String payload) throws IOException, URISyntaxException {
        final JSONObject jsonObject = new JSONObject(payload);
        final JSONObject jsonComment = jsonObject.getJSONObject("comment");
        final String issueOwner = jsonComment.getJSONObject("user").getString("login");
        final String body = jsonComment.getString("body");

        // only certain people are allowed to interact with ulf-bot
        if (!isAllowed(issueOwner)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        final String requestNumber = String.valueOf(jsonObject.getJSONObject("issue").getLong("number"));
        final String sha = getSha(requestNumber);

        // Redo OCA check
        if (Pattern.compile(Config.OCA_REDO_COMMENT_REGEXP, Pattern.CASE_INSENSITIVE).matcher(body).matches()) {
            ocaChecker.setForceReload(true);
            Set<Committer> committerSet = getCommitterSet(requestNumber);
            for (Committer eachCommitter : committerSet) {
                updateStatus(sha, eachCommitter, ocaChecker);
            }
        }

        // Add somebody to the oca list manually (e.g. when we do not have a githubid)
        Pattern pattern = Pattern.compile(Config.OCA_MANUALLY_APPROVE, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(body.trim());
        if (matcher.matches()) {
            String email = matcher.group(2);
            String githubId = matcher.group(3);
            ocaChecker.approve(githubId, email);
            Committer committer = new Committer();
            committer.setEmail(email);
            updateStatus(sha, committer, ocaChecker);
        }
        return null;
    }

    // Only members of a certain group are allowed to interact with ulf-bot
    protected boolean isAllowed(String issueOwner) throws IOException {
        String teamId = getTeamId(Config.OCA_TRUSTED_TEAM);
        String teamMembersString = getGithubApi().getTeamMembers(teamId);
        JSONArray teamArray = new JSONArray(teamMembersString);
        for (int i = 0; i < teamArray.length(); i++) {
            JSONObject eachTeam = teamArray.getJSONObject(i);
            if (issueOwner.equals(eachTeam.getString("login"))) {
                return true;
            }
        }
        return false;
    }

    private String getTeamId(String teamName) throws IOException {
        String teams = getGithubApi().getTeams(Config.GITHUB_USER);
        JSONArray teamArray = new JSONArray(teams);
        for (int i = 0; i < teamArray.length(); i++) {
            JSONObject team = teamArray.getJSONObject(i);
            if (teamName.equals(team.getString("slug"))) {
                return Long.toString(team.getLong("id"));
            }
        }
        return null;
    }

    private String getSha(String issueNumber) throws IOException {
        // The issue comment does not contain enough information about the issue/pull request.
        // We request the information and return the latest commit on the issue/pull request
        String responseContent = getGithubApi().getPullRequestInfo(issueNumber);
        JSONObject jsonObject = new JSONObject(responseContent);
        return jsonObject.getJSONObject("head").getString("sha");
    }
}
