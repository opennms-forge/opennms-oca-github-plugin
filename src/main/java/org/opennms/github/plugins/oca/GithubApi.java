package org.opennms.github.plugins.oca;

import org.json.JSONObject;
import org.json.JSONTokener;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by mvrueden on 23/06/15.
 */
public class GithubApi {

    public enum State {
        Pending, Error, Success;
    }

    private static final String GITHUB_API_URL = "https://api.github.com";

    // TODO MVR make it configurable and hide it :)
    private static final String ACCESS_TOKEN = " ";

    // TODO MVR change to OpenNMS
    private static final String GITHUB_REPO = System.getProperty("github.repository", "mvrueden/opennms");

    public void updateStatus(String sha, State state) throws IOException {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client
                    .target(GITHUB_API_URL)
                    .path("/repos/")
                    .path(GITHUB_REPO)
                    .path("/statuses/")
                    .path(sha);

            JSONObject stateObject = new JSONObject(new JSONTokener(getClass().getResourceAsStream("/OCA-status.json")));
            stateObject.put("state", state.name().toLowerCase());

            Response response = target
                    .request("application/vnd.github.VERSION.raw+json")
                    .header("Authorization", String.format("token %s", ACCESS_TOKEN))
                    .post(Entity.entity(stateObject.toString(), MediaType.TEXT_PLAIN_TYPE));
            if (!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
                throw new IOException(response.getEntity().toString());
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    // POST /repos/:owner/:repo/issues/:number/comments
    public void createCommentOnIssue(String issueNumber, String commentText) throws IOException {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client
                    .target(GITHUB_API_URL)
                    .path("/repos/")
                    .path(GITHUB_REPO)
                    .path("/issues/")
                    .path(issueNumber)
                    .path("comments");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("body", commentText);

            Response response = target
                    .request("application/vnd.github.VERSION.raw+json")
                    .header("Authorization", String.format("token %s", ACCESS_TOKEN))
                    .post(Entity.entity(jsonObject.toString(), MediaType.TEXT_PLAIN_TYPE));
            if (!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
                throw new IOException(response.getEntity().toString());
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    // GET /repos/:owner/:repo/pulls/:pullNumber
    public String getPullRequestInfo(String pullNumber) throws IOException {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client
                    .target(GITHUB_API_URL)
                    .path("/repos/")
                    .path(GITHUB_REPO)
                    .path("/pulls/")
                    .path(pullNumber);

            Response response = target
                    .request("application/vnd.github.VERSION.raw+json")
                    .header("Authorization", String.format("token %s", ACCESS_TOKEN))
                    .get();
            if (!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
                throw new IOException(response.getEntity().toString());
            }
            return response.readEntity(String.class);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
