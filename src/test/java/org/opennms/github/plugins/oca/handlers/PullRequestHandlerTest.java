/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.github.plugins.oca.handlers;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.opennms.github.plugins.oca.handlers.PullRequestHandler.isAlreadyInformed;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.github.plugins.oca.Committer;
import org.opennms.github.plugins.oca.Context;
import org.opennms.github.plugins.oca.GithubApi;
import org.opennms.github.plugins.oca.OCAChecker;
import org.opennms.github.plugins.oca.OCACheckerTest;

import com.google.common.collect.Lists;

public class PullRequestHandlerTest {

    @Test
    public void verifyIsAlreadyInformedWorks() {
        assertThat(isAlreadyInformed(createContextList(), createCommitter("mvrueden")), is(true));
        assertThat(isAlreadyInformed(createContextList(), createCommitter("j-white")), is(true));
        assertThat(isAlreadyInformed(createContextList(), createCommitter("RangerRick")), is(true));
        assertThat(isAlreadyInformed(createContextList(), createCommitter("web-flow")), is(true));
        assertThat(isAlreadyInformed(createContextList(), createCommitter("ulf")), is(false));
    }

    @Test
    public void verifyReadStatusMergesDuplicates() throws IOException {
        final GithubApi testApi = Mockito.mock(GithubApi.class);

        // Dummy status
        Mockito.when(testApi.readStatus(Mockito.anyString()))
                .thenReturn(new JSONArray(new JSONTokener(getClass().getResourceAsStream("/responses/dummy-status.json"))).toString());

        // Now verify reading the status
        final PullRequestHandler pullRequestHandler = new PullRequestHandler(testApi);
        final List<Context> contextList = pullRequestHandler.readStatus("401e68d01578be1238e38e8ba305d971c2815aef");
        assertEquals(4, contextList.size());
        assertThat(contextList, hasItems(
                new Context("OCA j-white", GithubApi.State.Success),
                new Context("OCA RangerRick", GithubApi.State.Success),
                new Context("OCA web-flow", GithubApi.State.Success),
                new Context("OCA mvrueden", GithubApi.State.Error)));
    }

    @Test
    public void verifyHandleDoesNotInformAlreadyInformed() throws IOException, URISyntaxException {
        final GithubApi testApi = Mockito.mock(GithubApi.class);

        // The mocked payload
        final String payload = new JSONObject(new JSONTokener(getClass().getResourceAsStream("/responses/dummy-payload.json"))).toString();

        // Mock commits
        Mockito.when(testApi.getPullRequestCommits(Mockito.anyString()))
                .thenReturn(new JSONArray(new JSONTokener(getClass().getResourceAsStream("/responses/dummy-commits.json"))).toString());

        // Mock status
        Mockito.when(testApi.readStatus(Mockito.anyString()))
                .thenReturn(new JSONArray(new JSONTokener(getClass().getResourceAsStream("/responses/dummy-status.json"))).toString());

        // Execute Handle
        final OCAChecker ocaChecker = new OCAChecker(OCACheckerTest.class.getResource("/oca-source.txt"), "mapping.properties");
        final PullRequestHandler handler = new PullRequestHandler(testApi);
        handler.handle(ocaChecker, payload);

        // Ensure that no comment was created, as all contributors have already been informed
        Mockito.verify(testApi, Mockito.times(0)).createCommentOnIssue(Mockito.anyString(), Mockito.anyString());
    }

    private static List<Context> createContextList() {
        return Lists.newArrayList(
                new Context("OCA j-white", GithubApi.State.Success),
                new Context("OCA RangerRick", GithubApi.State.Success),
                new Context("OCA web-flow", GithubApi.State.Success),
                new Context("OCA mvrueden", GithubApi.State.Error)
        );
    }

    private static Committer createCommitter(String githubId) {
        final Committer committer = new Committer();
        committer.setGithubId(githubId);
        return committer;
    }
}
