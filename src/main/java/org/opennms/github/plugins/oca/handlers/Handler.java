package org.opennms.github.plugins.oca.handlers;

import org.opennms.github.plugins.oca.OCAChecker;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;

public interface Handler {
    Response handle(OCAChecker ocaChecker, String payload) throws IOException, URISyntaxException;
}
