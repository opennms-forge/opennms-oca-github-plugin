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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("githubId", githubId)
                .add("name", name)
                .add("email", email)
                .toString();
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
