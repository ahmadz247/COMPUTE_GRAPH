package test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {
    
    public static class RequestInfo {
        private String httpCommand;
        private String uri;
        private String[] uriComponents;
        private Map<String, String> parameters;
        private byte[] content;
        
        public RequestInfo(String httpCommand, String uri, String[] uriComponents, 
                          Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriComponents = uriComponents;
            this.parameters = parameters;
            this.content = content;
        }
        
        public String getHttpCommand() {
            return httpCommand;
        }
        
        public String getUri() {
            return uri;
        }
        
        public String[] getUriComponents() {
            return uriComponents;
        }
        
        public String[] getUriSegments() {
            return uriComponents;
        }
        
        public Map<String, String> getParameters() {
            return parameters;
        }
        
        public byte[] getContent() {
            return content;
        }
    }
    
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        // Read the request line
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }
        
        // Parse request line: "GET /api/resource?id=123&name=test HTTP/1.1"
        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            return null;
        }
        
        String httpCommand = parts[0];
        String fullUri = parts[1];
        
        // Split URI and query string
        String uri;
        Map<String, String> parameters = new HashMap<>();
        int queryIndex = fullUri.indexOf('?');
        
        if (queryIndex != -1) {
            uri = fullUri.substring(0, queryIndex);
            String queryString = fullUri.substring(queryIndex + 1);
            parseQueryString(queryString, parameters);
        } else {
            uri = fullUri;
        }
        
        // Parse URI components (remove leading slash if present)
        String[] uriComponents;
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        if (uri.isEmpty()) {
            uriComponents = new String[0];
        } else {
            uriComponents = uri.split("/");
        }
        
        // Read headers
        String line;
        int contentLength = 0;
        String contentType = "";
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.substring(15).trim());
            } else if (line.toLowerCase().startsWith("content-type:")) {
                contentType = line.substring(13).trim();
            }
        }
        
        // Read content if present
        byte[] content = new byte[0];
        if (contentLength > 0) {
            if (contentType.toLowerCase().contains("multipart/form-data")) {
                content = parseMultipartContent(reader, contentLength, contentType);
            } else {
                // Regular content reading
                char[] buffer = new char[contentLength];
                int bytesRead = reader.read(buffer, 0, contentLength);
                if (bytesRead > 0) {
                    String contentStr = new String(buffer, 0, bytesRead);
                    content = contentStr.getBytes();
                }
            }
        }
        
        // Restore the full URI path for the RequestInfo
        String restoredUri = "/" + String.join("/", uriComponents);
        if (queryIndex != -1) {
            restoredUri = fullUri.substring(0, queryIndex);
        }
        
        return new RequestInfo(httpCommand, restoredUri, uriComponents, parameters, content);
    }
    
    private static void parseQueryString(String queryString, Map<String, String> parameters) {
        if (queryString == null || queryString.isEmpty()) {
            return;
        }
        
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int equalIndex = pair.indexOf('=');
            if (equalIndex != -1) {
                String key = pair.substring(0, equalIndex);
                String value = pair.substring(equalIndex + 1);
                parameters.put(key, value);
            } else {
                parameters.put(pair, "");
            }
        }
    }
    
    private static byte[] parseMultipartContent(BufferedReader reader, int contentLength, String contentType) throws IOException {
        // Extract boundary from content type
        String boundary = null;
        if (contentType.contains("boundary=")) {
            boundary = "--" + contentType.substring(contentType.indexOf("boundary=") + 9);
        }
        
        if (boundary == null) {
            return new byte[0];
        }
        
        // Read all content
        char[] buffer = new char[contentLength];
        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = reader.read(buffer, totalRead, contentLength - totalRead);
            if (read == -1) break;
            totalRead += read;
        }
        
        String content = new String(buffer, 0, totalRead);
        
        // Find file content between boundaries
        String[] parts = content.split(boundary);
        for (String part : parts) {
            if (part.contains("filename=")) {
                // Find the actual file content after the headers
                int doubleNewline = part.indexOf("\r\n\r\n");
                if (doubleNewline != -1) {
                    String fileContent = part.substring(doubleNewline + 4);
                    // Remove trailing boundary markers
                    fileContent = fileContent.replaceAll("--\\s*$", "").trim();
                    return fileContent.getBytes("UTF-8");
                }
            }
        }
        
        return new byte[0];
    }
}