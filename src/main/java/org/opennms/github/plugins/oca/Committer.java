package org.opennms.github.plugins.oca;

import com.google.common.base.MoreObjects;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(githubId, name, email);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() == obj.getClass()) {
            Committer other = (Committer) obj;
            boolean equals = Objects.equals(githubId, other.githubId) && Objects.equals(name, other.name) && Objects.equals(email, other.email);
            return equals;
        }
        return false;
    }
}
