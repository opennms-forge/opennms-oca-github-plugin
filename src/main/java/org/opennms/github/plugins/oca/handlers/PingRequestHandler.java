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
