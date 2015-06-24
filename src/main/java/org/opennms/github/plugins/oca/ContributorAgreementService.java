package org.opennms.github.plugins.oca;

import org.opennms.github.plugins.oca.handlers.Handler;
import org.opennms.github.plugins.oca.handlers.IssuecommentRequestHandler;
import org.opennms.github.plugins.oca.handlers.PullRequestHandler;

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
import java.util.HashMap;
import java.util.Map;

@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
public class ContributorAgreementService {

    private final GithubApi githubApi;

    private final OCAChecker ocaChecker;

    // X-Github-event -> Handler
    private final Map<String, Handler> responseHandlerMap = new HashMap<>();

    public ContributorAgreementService() throws MalformedURLException {
        githubApi = new GithubApi();
        ocaChecker = new OCAChecker(new URL(Config.OCA_WIKI_URL_PAGE_RAW_EDIT));

        // These events are supported by our API
        responseHandlerMap.put("pull_request", new PullRequestHandler(githubApi));
        responseHandlerMap.put("issue_comment", new IssuecommentRequestHandler(githubApi));
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
            @HeaderParam("X-Github-Signature") String signatureUsingSecret,
            String payload) throws IOException {

        Handler actionHandler = responseHandlerMap.get(eventType);
        if (actionHandler == null) {
            return Response
                    .status(Response.Status.NOT_IMPLEMENTED)
                    .entity(String.format("The provided eventType :'%s' is not supported at the moment.", eventType))
                    .build();
        }

        try {
            Response response = actionHandler.handle(ocaChecker, payload);
            if (response != null) {
                return response;
            }
            return Response.status(Response.Status.OK).build();
        } catch (IOException | URISyntaxException io) {
            // TODO MVR LOG
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
