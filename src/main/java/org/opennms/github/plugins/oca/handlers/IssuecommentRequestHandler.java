package org.opennms.github.plugins.oca.handlers;

import org.json.JSONObject;
import org.opennms.github.plugins.oca.Config;
import org.opennms.github.plugins.oca.GithubApi;
import org.opennms.github.plugins.oca.OCAChecker;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.regex.Pattern;

public class IssuecommentRequestHandler extends AbstractHandler {

    public IssuecommentRequestHandler(GithubApi githubApi) {
        super(githubApi);
    }

    @Override
    public Response handle(OCAChecker ocaChecker, String payload) throws IOException, URISyntaxException {
        JSONObject jsonObject = new JSONObject(payload);
        String body = jsonObject.getJSONObject("comment").getString("body");
        if (Pattern.compile(Config.OCA_REDO_COMMENT_REGEXP, Pattern.CASE_INSENSITIVE).matcher(body).matches()) {
            ocaChecker.setForceReload(true);
            Long requestNumber = jsonObject.getJSONObject("issue").getLong("number");
            String sha = getSha(requestNumber);
            Set<String> contributorSet = getContributorSet(requestNumber);
            for (String eachContributor : contributorSet) {
                boolean hasOcaSigned = ocaChecker.hasUserOCASigned(eachContributor);
                getGithubApi().updateStatus(sha, eachContributor, hasOcaSigned ? GithubApi.State.Success : GithubApi.State.Error);
            }
        }
        return null;
    }

    private String getSha(Long issueNumber) throws IOException {
        // The issue comment does not contain enough information about the issue/pull request.
        // We request the information and return the latest commit on the issue/pull request
        String responseContent = getGithubApi().getPullRequestInfo(String.valueOf(issueNumber));
        JSONObject jsonObject = new JSONObject(responseContent);
        return jsonObject.getJSONObject("head").getString("sha");
    }
}
