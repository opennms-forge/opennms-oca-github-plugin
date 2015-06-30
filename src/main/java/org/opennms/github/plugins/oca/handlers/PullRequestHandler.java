package org.opennms.github.plugins.oca.handlers;

import org.json.JSONObject;
import org.opennms.github.plugins.oca.Committer;
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
        Set<Committer> committerSet = getCommitterSet(pullRequestNumber);
        for (Committer eachCommitter : committerSet) {
            boolean hasOcaSigned = updateStatus(sha, eachCommitter, ocaChecker);
            if (!hasOcaSigned) {
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
}
