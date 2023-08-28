package opt.api.utils.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private String status = "200 OK";
    private String content;
    private Map<String, String> responseHeaders = new HashMap<>();

    public HttpResponse(String content) {
        this.content = content;
    }

    public HttpResponse(String content, String status) {
        this.content = content;
        this.status = status;
    }

    public HttpResponse(String content, Map<String, String> responseHeaders) {
        this.content = content;
        this.responseHeaders = responseHeaders;
    }

    public HttpResponse(String content, String status, Map<String, String> responseHeaders) {
        this.status = status;
        this.content = content;
        this.responseHeaders = responseHeaders;
    }

    public String getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }
    
}
