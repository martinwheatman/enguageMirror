package opt.api.utils.http;

import java.util.Map;

import opt.api.routing.Router;

public class HttpResponseBuilder {

    private String response = "200 OK";
	private String response() {return response;}
	public  void   response( String s ) {response = s;}

	private Router router = new Router();

    public String build (Map<String, String> head, Map<String, String> body) {
        String reply;
        try {
            reply = router.handle(head, body);
        } catch (HttpException e) {
            reply = e.getErrorMessage();
            response(e.getResponse());
        }
        
        String response = head.get("http") + " " + response() +"\n"
        + "Content-Type: text/plain\n"
        + "Content-Length: " + reply.length() + "\n"
        + "\n"
        + reply
        + "\n";

        return response;
    }
}
