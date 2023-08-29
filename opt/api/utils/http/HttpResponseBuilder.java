package opt.api.utils.http;

import java.util.Map;

import opt.api.routing.Router;

public class HttpResponseBuilder {

	private Router router = new Router();

    public String build (Map<String, String> head, Map<String, String> body) {
        String content;
        Map<String, String> responseHeaders;
        String status = "200 OK";

        try {
            HttpResponse httpResponse = router.handle(head, body);
            content = httpResponse.getContent();
            responseHeaders = httpResponse.getResponseHeaders();
        } catch (HttpException e) {
            content = e.getErrorMessage();
            responseHeaders = e.getExtraResponseHeaders();
            status = e.getStatus();
        }
        
        return composeHttpString(head.get("http"), status, content, responseHeaders);
    }

    private String composeHttpString(
        String httpVersion,
        String status,
        String content,
        Map<String, String> responseHeaders
    ) {
        String stringResponse = httpVersion + " " + status +"\n"
        + "Content-Type: text/plain\n"
        + "Access-Control-Allow-Origin: *\n"
        + "Content-Length: " + content.length() + "\n";

        for (String key : responseHeaders.keySet()) {
            stringResponse += key + ": " + responseHeaders.get(key) + "\n";
        }

        stringResponse += "\n"
        + content
        + "\n";

        return stringResponse;
    }

}
