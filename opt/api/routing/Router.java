package opt.api.routing;

import java.util.Map;

import opt.api.actions.handlers.Interpret;
import opt.api.utils.http.HttpException;
import opt.api.utils.http.HttpResponse;

public class Router {

    public HttpResponse handle ( Map<String, String> head, Map<String, String> body ) throws HttpException {
        Route route = Route.fromString(head.get("route"));

        switch (route) {
            case INTERPRET:
                return new Interpret().handle(head, body);
            default:
                throw new HttpException("404 Not Found", "This API route is not handled");
        }        
    }
    
}
