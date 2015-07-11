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

import org.junit.Assert;
import org.junit.Test;

public class CommitterTest {

    @Test
    public void testEquals() {
        Committer committer = new Committer();
        committer.setGithubId(null);
        committer.setName("A B");
        committer.setEmail("a.b@email.com");

        Committer committer2 = new Committer();
        committer2.setGithubId(null);
        committer2.setName("A B");
        committer2.setEmail("a.b@email.com");

        Assert.assertEquals(committer, committer);
        Assert.assertEquals(committer, committer2);

        Assert.assertEquals(committer.hashCode(), committer2.hashCode());
    }
}
