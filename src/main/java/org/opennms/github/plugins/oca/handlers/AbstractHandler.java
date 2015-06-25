package org.opennms.github.plugins.oca.handlers;

import org.json.JSONArray;
import org.json.JSONObject;
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

    protected GithubApi getGithubApi() {
        return githubApi;
    }

    protected static Set<String> extractContributorSet(JSONArray commitJsonArray) {
        Set<String> contributorSet = new HashSet<>();
        for (int i = 0; i < commitJsonArray.length(); i++) {
            // We have to exclude commits with no committer
            JSONObject eachElement = commitJsonArray.getJSONObject(i);
            if (eachElement.isNull("committer")) {
                // TODO MVR we may have to add a mapping for email or user name to github ids
                JSONObject committer = eachElement.getJSONObject("commit").getJSONObject("committer");
                contributorSet.add(String.format("%s (%s)", committer.getString("name"), committer.getString("email")));
            } else {
                String committerId = eachElement.getJSONObject("committer").getString("login");
                contributorSet.add(committerId);
            }
        }
        return contributorSet;
    }
}
