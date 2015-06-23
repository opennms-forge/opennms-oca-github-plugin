package org.opennms.github.plugins.oca;

public class Contributor {
    private String name;
    private String company;
    private String githubId;

    public void setName(String name) {
        this.name = name;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    @Override
    public String toString() {
        return name + ", " + company + ", " + githubId;
    }

    public String getGithubId() {
        return githubId;
    }

    public String getName() {
        return name;
    }
}
