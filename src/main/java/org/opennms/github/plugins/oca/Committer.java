package org.opennms.github.plugins.oca;

public class Committer {

    private String githubId;

    private String name;

    private String email;

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    public String getGithubId() {
        return githubId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
