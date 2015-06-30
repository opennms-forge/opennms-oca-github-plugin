package org.opennms.github.plugins.oca;

import java.io.IOException;

/**
 * Created by mvrueden on 30/06/15.
 */
public interface GithubApi {
    public enum State {
        Pending, Error, Success;
    }

    // POST /repos/:owner/:repo/statuses/:sha
    void updateStatus(String sha, String committer, State state) throws IOException;

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
