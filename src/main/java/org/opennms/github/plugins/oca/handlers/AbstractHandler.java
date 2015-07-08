package org.opennms.github.plugins.oca.handlers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.github.plugins.oca.Committer;
import org.opennms.github.plugins.oca.GithubApi;
import org.opennms.github.plugins.oca.OCAChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;


abstract class AbstractHandler implements Handler {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private final GithubApi githubApi;

    public AbstractHandler(GithubApi githubApi) {
        this.githubApi = githubApi;
    }

    protected Set<Committer> getCommitterSet(String pullRequestNumber) throws IOException {
        String commits = githubApi.getPullRequestCommits(pullRequestNumber);
        JSONArray commitJsonArray = new JSONArray(commits);
        return extractCommitterSet(commitJsonArray);
    }

    protected GithubApi getGithubApi() {
        return githubApi;
    }

    protected static Set<Committer> extractCommitterSet(JSONArray commitJsonArray) {
        Set<Committer> contributorSet = new HashSet<>();
        for (int i = 0; i < commitJsonArray.length(); i++) {
            // We have to exclude commits with no committer
            JSONObject eachElement = commitJsonArray.getJSONObject(i);
            JSONObject committer = eachElement.getJSONObject("commit").getJSONObject("committer");

            if (eachElement.isNull("committer")) {
                Committer eachCommitter = new Committer();
                eachCommitter.setName(committer.getString("name"));
                eachCommitter.setEmail(committer.getString("email"));
                contributorSet.add(eachCommitter);
            } else {
                String committerId = eachElement.getJSONObject("committer").getString("login");
                Committer eachCommitter = new Committer();
                eachCommitter.setGithubId(committerId);
                eachCommitter.setName(committer.getString("name"));
                contributorSet.add(eachCommitter);
            }
        }
        return contributorSet;
    }

    protected boolean updateStatus(String sha, Committer committer, OCAChecker ocaChecker) throws IOException, URISyntaxException {
        boolean hasOcaSigned = ocaChecker.hasUserOCASigned(committer);
        LOG.info("OCA signed for committer '{}' is {}", committer, hasOcaSigned);
        getGithubApi().updateStatus(sha, committer.getGithubId() != null ? committer.getGithubId() : committer.getEmail(), hasOcaSigned ? GithubApi.State.Success : GithubApi.State.Error);
        return hasOcaSigned;
    }
}
