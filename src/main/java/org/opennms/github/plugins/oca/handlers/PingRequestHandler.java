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

import org.opennms.github.plugins.oca.OCAChecker;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;

public class PingRequestHandler implements Handler {
    @Override
    public Response handle(OCAChecker ocaChecker, String payload) throws IOException, URISyntaxException {
        return Response.ok().build();
    }
}
