package opt.api.actions;

import java.util.Map;

import opt.api.utils.http.HttpException;


public abstract class ActionHandler {

    public String handle( Map<String, String> head, Map<String, String> body ) throws HttpException {
        String method = head.get("method");
        Action action = Action.valueOf(method);

        switch (action) {
            case GET:
                return get(head, body);
            case POST:
                return post(head, body);
            case PUT:
                return post(head, body);
            case DELETE:
                return post(head, body);
            case PATCH:
                return post(head, body);
            default:
                throw new HttpException("500", "API only supports " + Action.values());
        }
    }

    protected String get( Map<String, String> head, Map<String, String> body ) throws HttpException {
        throw new HttpException("500", "GET method not implemented");
    }

    protected String post( Map<String, String> head, Map<String, String> body ) throws HttpException {
        throw new HttpException("500", "POST method not implemented");
    }

    protected String put( Map<String, String> head, Map<String, String> body ) throws HttpException {
        throw new HttpException("500", "PUT method not implemented");
    }

    protected String delete( Map<String, String> head, Map<String, String> body ) throws HttpException {
        throw new HttpException("500", "DELETE method not implemented");
    }

    protected String patch( Map<String, String> head, Map<String, String> body ) throws HttpException {
        throw new HttpException("500", "PATCH method not implemented");
    }

}
