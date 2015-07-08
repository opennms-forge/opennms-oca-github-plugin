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
