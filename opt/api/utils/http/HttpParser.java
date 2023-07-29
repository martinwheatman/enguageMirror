package opt.api.utils.http;

import java.util.HashMap;
import java.util.Map;

import org.enguage.util.audit.Audit;

public class HttpParser {

    public static Map<String, String> parseHeaders(String httpRequestString) {
        Map<String, String> parsedData = new HashMap<>();

        // Split the request into header and body sections
        String[] requestParts = httpRequestString.split("\\r\\n\\r\\n", 2);
        String headerSection = requestParts[0];

        // Extract the HTTP method and route from the header
        String[] headerLines = headerSection.split("\\r\\n");
        String firstLine = headerLines[0];
        String[] firstLineParts = firstLine.split(" ");

        String method = firstLineParts[0];
        String route = firstLineParts[1].substring(1, firstLineParts[1].length());
        String http = firstLineParts[2];
        
        parsedData.put("method", method);
        parsedData.put("route", route);
        parsedData.put("http", http);
        
        for (int i = 1; i<headerLines.length; i++) {
            String[] header = headerLines[i].split(": ", 2);
            if (header.length == 2) {
                parsedData.put(header[0].trim(), header[1].trim());
            }
        }

        return parsedData;
    }

    public static Map<String, String> parseBody(String httpRequestString) {
        Map<String, String> parsedData = new HashMap<>();

        // Split the request into header and body sections
        String[] requestParts = httpRequestString.split("\r\n\r\n", 2);
        String bodySection = requestParts.length > 1 ? requestParts[1] : "";

        if (bodySection.trim().length() == 0) return parsedData;

        Map<String, String> meta = parseHeaders(httpRequestString);
        String contentType = meta.get("content-type");

        if ("application/json".equalsIgnoreCase(contentType)) {
            // Assuming the body contains JSON data
            Map<String, String> bodyMap = parseJson(bodySection.trim());
            return bodyMap;
        }

        return parsedData;
    }

    private static Map<String, String> parseJson(String jsonBody) {
        Map<String, String> bodyMap = new HashMap<>();
        int i = 1; // Skip the first '{'
        while (i < jsonBody.length() - 1) {
            StringBuilder key = new StringBuilder();
            StringBuilder value = new StringBuilder();
            while (jsonBody.charAt(i) != ':') {
                key.append(jsonBody.charAt(i));
                i++;
            }
            i++; // skip the ':'
            i++; // skip the whitespace after ':'

            // Parse the value
            int braceCount = 0;
            while (i < jsonBody.length() - 1) {
                char currentChar = jsonBody.charAt(i);
                if (currentChar == '{' || currentChar == '[') {
                    braceCount++;
                } else if (currentChar == '}' || currentChar == ']') {
                    braceCount--;
                } else if (currentChar == ',' && braceCount == 0) {
                    break;
                }
                value.append(currentChar);
                i++;
            }
            // Skip the whitespace after the value and the ',' (if any)
            i++;
            while (i < jsonBody.length() && Character.isWhitespace(jsonBody.charAt(i))) {
                i++;
            }

            // Add the key-value pair to the map
            bodyMap.put(
                key.toString().toLowerCase().replace("\"", "").trim(), 
                value.toString().replace("\"", "").trim()
            );

            i++; // move to the next character
        }

        return bodyMap;
    }
}
