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

import java.util.Objects;

public class Context {

    private final GithubApi.State state;
    private final String name;

    public Context(String name, GithubApi.State state) {
        this.name = name;
        this.state = state;
    }

    public GithubApi.State getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Context context = (Context) o;
        return Objects.equals(state, context.state)
                && Objects.equals(name, context.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, name);
    }
}