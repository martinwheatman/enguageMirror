package opt.api.actions;

import java.util.HashMap;
import java.util.Map;

import opt.api.utils.http.HttpException;
import opt.api.utils.http.HttpResponse;


public abstract class ActionHandler {

    public HttpResponse handle( Map<String, String> head, Map<String, String> body ) throws HttpException {
        String method = head.get("method");
        Action action = Action.valueOf(method);

        switch (action) {
            case GET:
                return get(head, body);
            case POST:
                return post(head, body);
            case PUT:
                return put(head, body);
            case DELETE:
                return delete(head, body);
            case PATCH:
                return patch(head, body);
            case OPTIONS:
                return options(head, body);
            default:
                throw new HttpException("500", "API only supports " + Action.values());
        }
    }

    protected HttpResponse get( Map<String, String> head, Map<String, String> body ) throws HttpException {
        throw new HttpException("500", "GET method not implemented");
    }

    protected HttpResponse post( Map<String, String> head, Map<String, String> body ) throws HttpException {
        throw new HttpException("500", "POST method not implemented");
    }

    protected HttpResponse put( Map<String, String> head, Map<String, String> body ) throws HttpException {
        throw new HttpException("500", "PUT method not implemented");
    }

    protected HttpResponse delete( Map<String, String> head, Map<String, String> body ) throws HttpException {
        throw new HttpException("500", "DELETE method not implemented");
    }

    protected HttpResponse patch( Map<String, String> head, Map<String, String> body ) throws HttpException {
        throw new HttpException("500", "PATCH method not implemented");
    }

    protected HttpResponse options( Map<String, String> head, Map<String, String> body ) {
        Map<String, String> responseHeaders = new HashMap<String, String>();
        responseHeaders.put("Allow", Action.getCommaSeparatedString());
        return new HttpResponse("", responseHeaders);
    }
}
