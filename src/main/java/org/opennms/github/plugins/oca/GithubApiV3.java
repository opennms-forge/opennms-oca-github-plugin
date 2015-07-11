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

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class GithubApiV3 implements GithubApi {

    private interface Predicate {
        WebTarget createTarget(Client client);
        Response doRequest(Invocation.Builder invocation);
    }

    private WebTarget createRepoWebTarget(Client client) {
        return client
                .target(Config.GITHUB_API_URL)
                .path("/repos/")
                .path(Config.GITHUB_USER)
                .path("/")
                .path(Config.GITHUB_REPO);
    }

    private String doRequest(Predicate predicate) throws IOException {
        Client client = ClientBuilder.newClient();
        try {

            WebTarget target = predicate.createTarget(client);

            Invocation.Builder invocation = target
                    .request("application/vnd.github.VERSION.raw+json")
                    .header("Authorization", String.format("token %s", Config.GITHUB_API_TOKEN));

            Response response = predicate.doRequest(invocation);
            if (!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
                throw new IOException(response.getEntity().toString());
            }
            if (response.hasEntity()) {
                return response.readEntity(String.class);
            }
            return null;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void updateStatus(String sha, String committer, State state) throws IOException {
        doRequest(new Predicate() {
            @Override
            public WebTarget createTarget(Client client) {
                return createRepoWebTarget(client).path("/statuses/").path(sha);
            }

            @Override
            public Response doRequest(Invocation.Builder invocation) {

                JSONObject stateObject = new JSONObject(new JSONTokener(getClass().getResourceAsStream("/OCA-status.json")));
                stateObject.put("context", stateObject.getString("context").replaceAll(":githubid:", committer));
                stateObject.put("description", stateObject.getString("description").replaceAll(":githubid:", committer));
                stateObject.put("state", state.name().toLowerCase());

                return invocation.post(Entity.entity(stateObject.toString(), MediaType.TEXT_PLAIN_TYPE));
            }
        });
    }

    // POST /repos/:owner/:repo/issues/:number/comments
    @Override
    public void createCommentOnIssue(String issueNumber, String commentText) throws IOException {
        doRequest(new Predicate() {
            @Override
            public WebTarget createTarget(Client client) {
                return createRepoWebTarget(client).path("/issues/").path(issueNumber).path("comments");
            }

            @Override
            public Response doRequest(Invocation.Builder invocation) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("body", commentText);

                return invocation.post(Entity.entity(jsonObject.toString(), MediaType.TEXT_PLAIN_TYPE));
            }
        });
    }

    // GET /repos/:owner/:repo/pulls/:pullNumber
    @Override
    public String getPullRequestInfo(String pullNumber) throws IOException {
        return doRequest(new Predicate() {
            @Override
            public WebTarget createTarget(Client client) {
                return createRepoWebTarget(client).path("/pulls/").path(pullNumber);
            }

            @Override
            public Response doRequest(Invocation.Builder invocation) {
                return invocation.get();
            }
        });
    }

    // GET /repos/:owner/:repo/pulls/:pullNumber/commits/page/:page
    @Override
    public String getPullRequestCommits(final String pullRequestNumber) throws IOException {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = createRepoWebTarget(client)
                    .path("/pulls/")
                    .path(pullRequestNumber)
                    .path("/commits");

            boolean next;
            int page = 1;
            JSONArray result = new JSONArray();
            do {
                WebTarget pageTarget = target.queryParam("page", page);
                Invocation.Builder invocation = pageTarget
                        .request("application/vnd.github.VERSION.raw+json")
                        .header("Authorization", String.format("token %s", Config.GITHUB_API_TOKEN));
                Response response = invocation.get();

                if (!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
                    throw new IOException(response.getEntity().toString());
                }

                if (response.hasEntity()) {
                    JSONArray chunk = new JSONArray(response.readEntity(String.class));
                    for (int i=0; i<chunk.length(); i++) {
                        result.put(chunk.get(i));
                    }
                }

                next = response.getHeaderString("Link") != null && response.getHeaderString("Link").contains("next");
                page++;
            } while (next);

            return result.toString();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    // GET /orgs/:repo/teams
    @Override
    public String getTeams(String organisation) throws IOException {
        return doRequest(new Predicate() {
            @Override
            public WebTarget createTarget(Client client) {
                return client.target(Config.GITHUB_API_URL).path("/orgs/").path(organisation).path("/teams");
            }

            @Override
            public Response doRequest(Invocation.Builder invocation) {
                return invocation.get();
            }
        });
    }

    // GET /teams/:teamId/members
    @Override
    public String getTeamMembers(String teamId) throws IOException {
        return doRequest(new Predicate() {
            @Override
            public WebTarget createTarget(Client client) {
                return client.target(Config.GITHUB_API_URL).path("/teams/").path(teamId).path("/members");
            }

            @Override
            public Response doRequest(Invocation.Builder invocation) {
                return invocation.get();
            }
        });
    }

}
