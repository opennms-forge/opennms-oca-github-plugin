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

import org.junit.Assert;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opennms.github.plugins.oca.GithubApi;

import java.io.IOException;

/**
 * Created by mvrueden on 30/06/15.
 */
public class IssuecommentRequestHandlerTest {

    @Test
    public void testIsAllowed() throws IOException {
        // Create Mock
        final GithubApi githubApi = Mockito.mock(GithubApi.class);
        Mockito.when(githubApi.getTeams(Mockito.any())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                final String teamId = (String) invocation.getArguments()[0];
                if ("OpenNMS".equals(teamId)) {
                    return new JSONArray(new JSONTokener(getClass().getResourceAsStream("/responses/opennms-teams.json"))).toString();
                }
                return new JSONArray().toString();
            }
        });
        Mockito.when(githubApi.getTeamMembers(Mockito.anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                final String teamId = (String) invocation.getArguments()[0];
                if ("100003".equals(teamId)) {
                    return new JSONArray(new JSONTokener(getClass().getResourceAsStream("/responses/oca-members.json"))).toString();
                }
                return new JSONArray().toString();
            }
        });

        // Execute test
        final IssuecommentRequestHandler handler = new IssuecommentRequestHandler(githubApi);
        String[] allowedUsers = new String[]{"mvrueden", "indigo423", "fooker"};
        String[] notAllowedUsers = new String[]{"ivansjg"};

        for (String eachAllowedUser : allowedUsers) {
            Assert.assertEquals(Boolean.TRUE, handler.isAllowed(eachAllowedUser));
        }

        for (String eachNotAllowedUser : notAllowedUsers) {
            Assert.assertEquals(Boolean.FALSE, handler.isAllowed(eachNotAllowedUser));
        }
    }


}
