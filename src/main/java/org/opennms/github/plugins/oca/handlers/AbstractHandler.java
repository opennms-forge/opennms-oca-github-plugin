package org.opennms.github.plugins.oca.handlers;

import org.json.JSONArray;
import org.opennms.github.plugins.oca.GithubApi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


abstract class AbstractHandler implements Handler {

    private final GithubApi githubApi;

    public AbstractHandler(GithubApi githubApi) {
        this.githubApi = githubApi;
    }

    protected Set<String> getContributorSet(String pullRequestNumber) throws IOException {
        String commits = githubApi.getPullRequestCommits(pullRequestNumber);
        JSONArray commitJsonArray = new JSONArray(commits);
        return extractContributorSet(commitJsonArray);
    }

    protected Set<String> getContributorSet(Long pullRequestNumber) throws IOException {
        return getContributorSet(String.valueOf(pullRequestNumber));
    }

    protected GithubApi getGithubApi() {
        return githubApi;
    }

    protected static Set<String> extractContributorSet(JSONArray commitJsonArray) {
        Set<String> contributorSet = new HashSet<>();
        for (int i = 0; i < commitJsonArray.length(); i++) {
            // We have to exclude commits with no committer
            // TODO MVR figure out why the committer is null
            if (!commitJsonArray.getJSONObject(i).isNull("committer")) {
                String committerId = commitJsonArray.getJSONObject(i).getJSONObject("committer").getString("login");
                contributorSet.add(committerId);
            }
        }
        return contributorSet;
    }
}
