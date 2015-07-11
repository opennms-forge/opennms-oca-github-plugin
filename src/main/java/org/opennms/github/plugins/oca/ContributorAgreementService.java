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

import com.google.common.io.BaseEncoding;
import org.opennms.github.plugins.oca.handlers.Handler;
import org.opennms.github.plugins.oca.handlers.IssuecommentRequestHandler;
import org.opennms.github.plugins.oca.handlers.PingRequestHandler;
import org.opennms.github.plugins.oca.handlers.PullRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
public class ContributorAgreementService {

    private static final Logger LOG = LoggerFactory.getLogger(ContributorAgreementService.class);

    private final Mac mac;

    private final GithubApi githubApi;

    private final OCAChecker ocaChecker;

    // X-Github-event -> Handler
    private final Map<String, Handler> responseHandlerMap = new HashMap<>();

    public ContributorAgreementService() throws MalformedURLException, InvalidKeyException, NoSuchAlgorithmException {
        githubApi = new GithubApiV3();
        ocaChecker = new OCAChecker(new URL(Config.OCA_WIKI_URL_PAGE_RAW_EDIT), Config.MAPPING_FILE_LOCATION);

        // These events are supported by our API
        responseHandlerMap.put("ping", new PingRequestHandler());
        responseHandlerMap.put("pull_request", new PullRequestHandler(githubApi));
        responseHandlerMap.put("issue_comment", new IssuecommentRequestHandler(githubApi));

        mac = Mac.getInstance("HmacSHA1");
        final SecretKeySpec signingKey = new SecretKeySpec(Config.GITHUB_WEBHOOK_SECRET.getBytes(), "HmacSHA1");
        mac.init(signingKey);
    }

    @GET
    @Path("/ping")
    public Response get() {
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("/payload")
    public Response post(
            @HeaderParam("X-Github-event") String eventType,
            @HeaderParam("X-Github-Delivery") String uniqueId,
            @HeaderParam("X-Hub-Signature") String signatureUsingSecret,
            String payload) throws IOException {

        LOG.info("Payload request with eventType = '{}', uniqueId = '{}', signature = '{}' received.", eventType, uniqueId, signatureUsingSecret);
        LOG.debug("Payload body: {}", payload);

        if (!isSignatureValid(signatureUsingSecret, payload)) {
            LOG.error("Signature does not match. Received: '{}', Expected: '{}'", signatureUsingSecret, "HIDDEN");
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Signature does not match.")
                    .build();
        }

        Handler actionHandler = responseHandlerMap.get(eventType);
        if (actionHandler == null) {
            LOG.info("The provided eventType: '{}' is not supported", eventType);
            return Response
                    .status(Response.Status.NOT_IMPLEMENTED)
                    .entity(String.format("The provided eventType: '%s' is not supported at the moment.", eventType))
                    .build();
        }
        try {
            Response response = actionHandler.handle(ocaChecker, payload);
            if (response != null) {
                return response;
            }
            return Response.status(Response.Status.OK).build();
        } catch (IOException | URISyntaxException io) {
            LOG.error(io.getMessage(), io);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isSignatureValid(String signature, String payload) {
        // there must be a signature
        if (signature != null) {
            byte[] encodedBytes = mac.doFinal(payload.getBytes());
            String encodedString = BaseEncoding.base16().encode(encodedBytes);
            return signature.equals(String.format("sha1=%s", encodedString.toLowerCase()));
        }
        return false;
    }


}
