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
