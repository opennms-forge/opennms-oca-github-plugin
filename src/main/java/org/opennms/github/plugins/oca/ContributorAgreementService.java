package org.opennms.github.plugins.oca;

import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
public class ContributorAgreementService {

    private interface Handler {
        Response handle(OCAChecker ocaChecker, String payload);
    }

    private class PullRequestHandler implements Handler {
        public Response handle(OCAChecker ocaChecker, String payload) {

            try {
                String pullRequestNumber = getPullRequestNumber(payload, null);
                String sha = getSha(payload);
                githubApi.updateStatus(sha, GithubApi.State.Pending);

                String user = getUser(payload, "pull_request");
                boolean hasOcaSigned = ocaChecker.hasUserOCASigned(user);
                githubApi.updateStatus(sha, hasOcaSigned ? GithubApi.State.Success : GithubApi.State.Error);
                if (!hasOcaSigned) {
                    String content = loadWelcomeContent(user);
                    githubApi.createCommentOnIssue(pullRequestNumber, content);
                }

                return null;
            } catch (IOException | URISyntaxException io) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    private String getPullRequestNumber(String payload, String root) {
        JSONObject payloadObject = new JSONObject(payload);
        if (root != null) {
            return Long.toString(payloadObject.getJSONObject(root).getLong("number"));
        }
        return Long.toString(payloadObject.getLong("number"));
    }

    private String loadWelcomeContent(String user) throws IOException, URISyntaxException {
        Contributor contributor = ocaChecker.getContributor(user);
        URL resourceURL = getClass().getResource("/oca-welcome.md");
        byte[] bytes = Files.readAllBytes(Paths.get(resourceURL.getPath()));
        String content = new String(bytes);
        content = content.replaceAll(":githubid:", user);
        if (contributor != null) {
            content = content.replaceAll(":user:", contributor.getName());
        }
        return content;
    }

    private String getSha(String payload) {
        JSONObject payloadObject = new JSONObject(payload);
        return payloadObject.getJSONObject("pull_request").getJSONObject("head").getString("sha");
    }

    // TODO MVR implement accordingly
    private String getUser(String payload, String root) {
        JSONObject payloadObject = new JSONObject(payload);
        return payloadObject.getJSONObject(root).getJSONObject("user").getString("login");
    }

    private class IssuecommentRequestHandler implements Handler {

        @Override
        public Response handle(OCAChecker ocaChecker, String payload) {
            JSONObject jsonObject = new JSONObject(payload);
            String body = jsonObject.getJSONObject("comment").getString("body");
            if (Pattern.compile(OCA_REDO_COMMENT, Pattern.CASE_INSENSITIVE).matcher(body).matches()) {
                // TODO MVR verreinfachen....
                try {
                    Long issueNumber = jsonObject.getJSONObject("issue").getLong("number");
                    String user = getUser(payload, "issue");
                    String sha = getSha(issueNumber);
                    boolean hasOcaSigned = ocaChecker.hasUserOCASigned(user);
                    githubApi.updateStatus(sha, hasOcaSigned ? GithubApi.State.Success : GithubApi.State.Error);

                    return null;
                } catch (IOException | URISyntaxException io) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }

            }
            return null;
        }

        private String getSha(Long issueNumber) throws IOException {
            String responseContent = githubApi.getPullRequestInfo(String.valueOf(issueNumber));
            JSONObject jsonObject = new JSONObject(responseContent);
            return jsonObject.getJSONObject("head").getString("sha");
        }
    }

    protected static final String OCA_WIKI_RAW_URL = "http://www.opennms.org/w/index.php?title=Executed_contributor_agreements&action=raw";

    private static final String OCA_REDO_COMMENT = ".*alfred.*oca.*";

    private final GithubApi githubApi = new GithubApi();

    private final OCAChecker ocaChecker;

    private final Map<String, Handler> responseActionMap = new HashMap<>();

    public ContributorAgreementService() throws MalformedURLException {
        // These events are supported by our API
        responseActionMap.put("pull_request", new PullRequestHandler());
        responseActionMap.put("issue_comment", new IssuecommentRequestHandler());

        ocaChecker = new OCAChecker(new URL(OCA_WIKI_RAW_URL));
    }

    @GET
    @Path("/ping")
    public Response get() {
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("/payload")
    public Response post(
            @HeaderParam("X-Github-event") String eventType,
            @HeaderParam("X-Github-Delivery") String uniqueId,
            @HeaderParam("X-Github-Signature") String signatureUsingSecret,
            String payload) throws IOException {

        Handler actionHandler = responseActionMap.get(eventType);
        if (actionHandler == null) {
            return Response
                    .status(Response.Status.NOT_IMPLEMENTED)
                    .entity(String.format("The provided eventType :'%s' is not supported at the moment.", eventType))
                    .build();
        }

        Response response = actionHandler.handle(ocaChecker, payload);
        if (response != null) {
            return response;
        }

        return Response.status(Response.Status.OK).build();
    }

}
