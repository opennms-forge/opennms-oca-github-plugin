package org.opennms.github.plugins.oca;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Contributor {
    private String name;
    private String company;
    private String githubId;
    private Set<String> emails = new HashSet<String>();

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

    public void addEmail(String email) {
        emails.add(email);
    }

    public boolean matchesEmail(String email) {
        return emails.contains(email);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() == getClass()) {
            Contributor contributor = (Contributor) obj;
            boolean equals = Objects.equals(name, contributor.name)
                    && Objects.equals(company, contributor.company)
                    && Objects.equals(githubId, contributor.company)
                    && Objects.equals(emails, contributor.emails);
            return equals;
        }
        return false;
    }

}
