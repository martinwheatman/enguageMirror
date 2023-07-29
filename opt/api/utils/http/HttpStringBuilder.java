package opt.api.utils.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class HttpStringBuilder {
    
    public static String build(BufferedReader in) throws IOException {
        StringBuilder requestBuilder = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
            requestBuilder.append(line).append("\r\n");
        }
        String PostHeaderCrlf = "\r\n";
        requestBuilder.append(PostHeaderCrlf);

        int contentLength = getBodyContentLength(requestBuilder.toString().trim());
        char[] buffer = new char[contentLength];
        int bytesRead = in.read(buffer, 0, contentLength);
            
        if (bytesRead > 0) {
            requestBuilder.append(buffer, 0, bytesRead);
        }
        
    
        return requestBuilder.toString().trim();
    }

        // Function to extract the Content-Length from the request headers
        private static int getBodyContentLength(String request) {
            String[] headerLines = request.split("\r\n");
            for (String line : headerLines) {
                if (line.contains("Content-Length: ")) {
                    int contentLength = Integer.parseInt(line.split("Content-Length: ")[1]);
                    return contentLength;
                }
            }
    
            return -1;
        }
    
    public static void main(String[] args) throws IOException {
    	String req =
    			"POST HTTP/1.1 /route\n"
		    	+ "Header-One: Value\n"
		    	+ "Header-Two: Value\n"
		    	+ "\n"
		    	+ "{\"body\": \"value\"}\n";
    	
    	Reader r = new StringReader( req );
    	BufferedReader br = new BufferedReader(r);
    	System.out.println( "request is:\n"+ build( br ));
    }
}
