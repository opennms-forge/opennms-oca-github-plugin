package org.opennms.github.plugins.oca;

import org.json.JSONObject;
import org.json.JSONTokener;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by mvrueden on 23/06/15.
 */
public class GithubApi {

    public enum State {
        Error, Success;
    }

    // TODO MVR name me
    private interface Predicate {
        WebTarget updateTarget(WebTarget target);
        Response doRequest(Invocation.Builder invocation);
    }

    private String doRequest(Predicate predicate) throws IOException {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client
                    .target(Config.GITHUB_API_URL)
                    .path("/repos/")
                    .path(Config.GITHUB_USER)
                    .path("/")
                    .path(Config.GITHUB_REPO);

            target = predicate.updateTarget(target);

            Invocation.Builder invocation = target
                    .request("application/vnd.github.VERSION.raw+json")
                    .header("Authorization", String.format("token %s", Config.GITHUB_API_TOKEN));

            Response response = predicate.doRequest(invocation);
            if (!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
                throw new IOException(response.getEntity().toString());
            }
            if (response.hasEntity()) {
                return response.readEntity(String.class);
            }
            return null;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public void updateStatus(String sha, String contributor, State state) throws IOException {
        doRequest(new Predicate() {
            @Override
            public WebTarget updateTarget(WebTarget target) {
                return target.path("/statuses/").path(sha);
            }

            @Override
            public Response doRequest(Invocation.Builder invocation) {

                JSONObject stateObject = new JSONObject(new JSONTokener(getClass().getResourceAsStream("/OCA-status.json")));
                stateObject.put("context", stateObject.getString("context").replaceAll(":githubid:", contributor));
                stateObject.put("description", stateObject.getString("description").replaceAll(":githubid:", contributor));
                stateObject.put("state", state.name().toLowerCase());

                return invocation.post(Entity.entity(stateObject.toString(), MediaType.TEXT_PLAIN_TYPE));
            }
        });
    }

    // POST /repos/:owner/:repo/issues/:number/comments
    public void createCommentOnIssue(String issueNumber, String commentText) throws IOException {
        doRequest(new Predicate() {
            @Override
            public WebTarget updateTarget(WebTarget target) {
                return target.path("/issues/").path(issueNumber).path("comments");
            }

            @Override
            public Response doRequest(Invocation.Builder invocation) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("body", commentText);

                return invocation.post(Entity.entity(jsonObject.toString(), MediaType.TEXT_PLAIN_TYPE));
            }
        });
    }

    // GET /repos/:owner/:repo/pulls/:pullNumber
    public String getPullRequestInfo(String pullNumber) throws IOException {
        return doRequest(new Predicate() {
            @Override
            public WebTarget updateTarget(WebTarget target) {
                return target.path("/pulls/").path(pullNumber);
            }

            @Override
            public Response doRequest(Invocation.Builder invocation) {
                return invocation.get();
            }
        });
    }

    // GET /repos/:owner/:repo/pulls/:pullNumber/commits
    public String getPullRequestCommits(String pullRequestNumber) throws IOException {
        return doRequest(new Predicate() {
            @Override
            public WebTarget updateTarget(WebTarget target) {
                return target.path("/pulls/").path(pullRequestNumber).path("/commits");
            }

            @Override
            public Response doRequest(Invocation.Builder invocation) {
                return invocation.get();
            }
        });
    }

//    // https://api.github.com/repos/mvrueden/opennms/commits
//    public static void main(String[] args) throws IOException, URISyntaxException {
//        final OCAChecker ocaChecker = new OCAChecker(new URL(ContributorAgreementService.OCA_WIKI_RAW_URL));
//        ocaChecker.setShouldReload(false);
//
//        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("oca-status.txt")));
//        Set<String> contributors = Files.readLines(new File("commits.txt"), Charsets.UTF_8, new LineProcessor<Set<String>>() {
//
//            final Set<String> lines = new HashSet<>();
//
//            int i = 1;
//
//            @Override
//            public boolean processLine(String line) throws IOException {
//                System.out.println("Processing line " + i);
//                i++;
//                if (!line.isEmpty()) {
//                    JSONArray array = new JSONArray(new JSONTokener(line));
//                    Set<String> contributors = ContributorAgreementService.extractContributorSet(array);
//                    for (String eachContributor : contributors) {
//                        try {
//                            boolean hasSigned = ocaChecker.hasUserOCASigned(eachContributor);
//                            lines.add(String.format("%s=%s", eachContributor, hasSigned));
//                        } catch (URISyntaxException e) {
//                            throw Throwables.propagate(e);
//                        }
//                    }
//                }
//                return true;
//            }
//
//            @Override
//            public Set<String> getResult() {
//                return lines;
//            }
//        });
//
//        for (String eachContributor : contributors) {
//            if (!eachContributor.contains("=true")) {
//                output.write(eachContributor);
//                output.newLine();
//            }
//        }
//        output.close();
//
////        while (true) {
////            JSONArray array = new JSONArray(new JSONTokener(input));
////            Set<String> contributors = ContributorAgreementService.extractContributorSet(array);
////            for (String eachContributor : contributors) {
////                boolean hasSigned = new OCAChecker(new URL(ContributorAgreementService.OCA_WIKI_RAW_URL)).hasUserOCASigned(eachContributor);
////                output.write(String.format("%s=%s", eachContributor, hasSigned));
////                output.newLine();
////            }
////            output.flush();
////        }
////        output.close();
//
////        FileOutputStream out = new FileOutputStream("commits.txt");
////        Client client = ClientBuilder.newClient();
////        try {
////            String nextPage = null;
////            int page = 1;
////            do {
////                WebTarget target = client
////                        .target(GITHUB_API_URL)
////                        .path("/repos/")
////                        .path(GITHUB_REPO)
////                        .path("/commits")
////                        .queryParam("since", "2010-01-01T00:00:000");
////
////                if (nextPage != null) {
////                    target = target.queryParam("page", page);
////                }
////
////                Response response = target
////                        .request("application/vnd.github.VERSION.raw+json")
////                        .header("Authorization", String.format("token %s", ACCESS_TOKEN))
////                        .get();
////                if (!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
////                    throw new IOException(response.getEntity().toString());
////                }
////                ByteStreams.copy(response.readEntity(InputStream.class), out);
////                out.write("\n\n\n".getBytes());
////                nextPage = response.getHeaderString("Link");
////                page++;
////            } while (nextPage.contains("next"));
////        } finally {
////            if (client != null) {
////                client.close();
////            }
////        }
//    }

}
