package opt.api.utils.http;

import java.util.HashMap;
import java.util.Map;

public class HttpException extends Exception {
	
    private static final long serialVersionUID = 7297807951434727398L;
    
	private String status;
    private String errorMessage;
    private Map<String, String> extraResponseHeaders = new HashMap<>();

    public HttpException(String status, String errorMessage) {
        super(errorMessage);
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public HttpException(String status, String errorMessage, Map<String, String> extraResponseHeaders) {
        super(errorMessage);
        this.status = status;
        this.errorMessage = errorMessage;
        this.extraResponseHeaders = extraResponseHeaders;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Map<String, String> getExtraResponseHeaders() {
        return extraResponseHeaders;
    }
    
}
