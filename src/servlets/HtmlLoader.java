package servlets;

import server.RequestParser.RequestInfo;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlLoader implements Servlet {
    private final String htmlDirectory;
    
    public HtmlLoader(String htmlDirectory) {
        this.htmlDirectory = htmlDirectory;
    }
    
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            // Parse the requested path from URI segments
            String[] uriSegments = ri.getUriSegments();
            
            // Remove "app" from the path if present (since servlet is mapped to /app/)
            String filename;
            if (uriSegments.length > 1 && "app".equals(uriSegments[0])) {
                // Build filename from remaining segments
                StringBuilder pathBuilder = new StringBuilder();
                for (int i = 1; i < uriSegments.length; i++) {
                    if (i > 1) pathBuilder.append("/");
                    pathBuilder.append(uriSegments[i]);
                }
                filename = pathBuilder.toString();
            } else if (uriSegments.length == 1) {
                filename = uriSegments[0];
            } else {
                // Default to index.html if no specific file requested
                filename = "index.html";
            }
            
            // If no extension specified, assume .html
            if (!filename.contains(".")) {
                filename += ".html";
            }
            
            // Construct file path
            Path filePath = Paths.get(htmlDirectory, filename);
            
            // Security check: ensure the file is within the HTML directory
            Path htmlDirPath = Paths.get(htmlDirectory).toAbsolutePath().normalize();
            Path requestedPath = filePath.toAbsolutePath().normalize();
            
            if (!requestedPath.startsWith(htmlDirPath)) {
                sendErrorResponse(toClient, 403, "Access denied");
                return;
            }
            
            // Check if file exists
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                sendFileNotFoundResponse(toClient, filename);
                return;
            }
            
            // Read and serve the file
            byte[] fileContent = Files.readAllBytes(filePath);
            String contentType = getContentType(filename);
            
            sendFileResponse(toClient, fileContent, contentType);
            
        } catch (Exception e) {
            sendErrorResponse(toClient, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    private String getContentType(String filename) {
        String extension = "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            extension = filename.substring(lastDot + 1).toLowerCase();
        }
        
        switch (extension) {
            case "html":
            case "htm":
                return "text/html; charset=UTF-8";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            case "json":
                return "application/json";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml";
            default:
                return "text/plain";
        }
    }
    
    private void sendFileResponse(OutputStream toClient, byte[] content, String contentType) throws IOException {
        String headers = String.format(
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: %s\r\n" +
            "Content-Length: %d\r\n" +
            "Cache-Control: no-cache\r\n" +
            "\r\n",
            contentType, content.length
        );
        
        toClient.write(headers.getBytes("UTF-8"));
        toClient.write(content);
        toClient.flush();
    }
    
    private void sendFileNotFoundResponse(OutputStream toClient, String filename) throws IOException {
        String html = String.format(
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>File Not Found</title>\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; padding: 40px; text-align: center; background-color: #f8f9fa; }\n" +
            "        .error-container { background-color: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); max-width: 500px; margin: 0 auto; }\n" +
            "        .error-icon { font-size: 64px; margin-bottom: 20px; }\n" +
            "        .error-title { color: #333; margin-bottom: 10px; }\n" +
            "        .error-message { color: #666; margin-bottom: 20px; }\n" +
            "        .error-details { background-color: #f8f9fa; padding: 15px; border-radius: 4px; font-family: monospace; }\n" +
            "        .back-link { color: #1976d2; text-decoration: none; }\n" +
            "        .back-link:hover { text-decoration: underline; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"error-container\">\n" +
            "        <div class=\"error-icon\">FILE</div>\n" +
            "        <h1 class=\"error-title\">File Not Found</h1>\n" +
            "        <p class=\"error-message\">The requested file could not be found on this server.</p>\n" +
            "        <div class=\"error-details\">\n" +
            "            Requested file: %s<br>\n" +
            "            Directory: %s\n" +
            "        </div>\n" +
            "        <p><a href=\"index.html\" class=\"back-link\">&lt;- Back to Main Page</a></p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            escapeHtml(filename), escapeHtml(htmlDirectory)
        );
        
        sendErrorResponse(toClient, 404, html);
    }
    
    private void sendErrorResponse(OutputStream toClient, int statusCode, String content) throws IOException {
        String statusText = getStatusText(statusCode);
        boolean isHtml = content.trim().startsWith("<!DOCTYPE") || content.trim().startsWith("<html>");
        String contentType = isHtml ? "text/html; charset=UTF-8" : "text/plain";
        
        String response = String.format(
            "HTTP/1.1 %d %s\r\n" +
            "Content-Type: %s\r\n" +
            "Content-Length: %d\r\n" +
            "\r\n" +
            "%s",
            statusCode, statusText, contentType, content.getBytes("UTF-8").length, content
        );
        
        toClient.write(response.getBytes("UTF-8"));
        toClient.flush();
    }
    
    private String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown";
        }
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    @Override
    public void close() throws IOException {
        // No resources to close
    }
}