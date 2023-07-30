package opt.api.utils.http;

public class HttpException extends Exception {

    private String response;
    private String errorMessage;

    public HttpException(String response, String errorMessage) {
        super(errorMessage);
        this.response = response;
        this.errorMessage = errorMessage;
    }

    public String getResponse() {
        return response;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
}
