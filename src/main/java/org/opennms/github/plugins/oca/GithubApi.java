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

import java.io.IOException;

/**
 * Created by mvrueden on 30/06/15.
 */
public interface GithubApi {
    enum State {
        Pending, Error, Success;

        public static State createFrom(String string) {
            for (State state : values()) {
                if (state.name().equalsIgnoreCase(string)) {
                    return state;
                }
            }
            throw new IllegalArgumentException("No state with name '" + string +"' found");
        }
    }

    // POST /repos/:owner/:repo/statuses/:sha
    void updateStatus(String sha, String committer, State state) throws IOException;

    // GET /repos/:owner/:repo/commits/:ref/statuses
    String readStatus(String ref) throws IOException;

    // POST /repos/:owner/:repo/issues/:number/comments
    void createCommentOnIssue(String issueNumber, String commentText) throws IOException;

    // GET /repos/:owner/:repo/pulls/:pullNumber
    String getPullRequestInfo(String pullNumber) throws IOException;

    // GET /repos/:owner/:repo/pulls/:pullNumber/commits/page/:page
    String getPullRequestCommits(String pullRequestNumber) throws IOException;

    // GET /orgs/:repo/teams
    String getTeams(String organisation) throws IOException;

    // GET /teams/:teamId/members
    String getTeamMembers(String teamId) throws IOException;
}
