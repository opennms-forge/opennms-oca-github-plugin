package org.opennms.github.plugins.oca.handlers;

import org.json.JSONObject;
import org.opennms.github.plugins.oca.Contributor;
import org.opennms.github.plugins.oca.GithubApi;
import org.opennms.github.plugins.oca.OCAChecker;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public class PullRequestHandler extends AbstractHandler {

    public PullRequestHandler(GithubApi githubApi) {
        super(githubApi);
    }

    public Response handle(OCAChecker ocaChecker, String payload) throws IOException, URISyntaxException {
        String pullRequestNumber = extractPullRequestNumber(payload);
        String sha = extractSha(payload);
        Set<String> contributorSet = getContributorSet(pullRequestNumber);
        for (String eachContributor : contributorSet) {
            boolean hasOcaSigned = ocaChecker.hasUserOCASigned(eachContributor);
            getGithubApi().updateStatus(sha, eachContributor, hasOcaSigned ? GithubApi.State.Success : GithubApi.State.Error);
            if (!hasOcaSigned) {
                String content = loadWelcomeMessage(ocaChecker, eachContributor);
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

    private String loadWelcomeMessage(OCAChecker ocaChecker, String user) throws IOException, URISyntaxException {
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
}
