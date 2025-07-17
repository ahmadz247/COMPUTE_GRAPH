package test;

import test.RequestParser.RequestInfo;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConfLoader implements Servlet {
    private GenericConfig currentConfig;
    
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            // Extract file content from request
            byte[] content = ri.getContent();
            
            if (content == null || content.length == 0) {
                sendErrorResponse(toClient, "No file content received");
                return;
            }
            
            // Convert content to string (assuming text file)
            String configContent = new String(content, "UTF-8").trim();
            
            if (configContent.isEmpty()) {
                sendErrorResponse(toClient, "Configuration file is empty");
                return;
            }
            
            // Generate a unique filename for the uploaded config
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = "uploaded_config_" + timestamp + ".conf";
            
            // Save the configuration to a temporary file
            Path tempConfigPath = Paths.get(filename);
            Files.write(tempConfigPath, configContent.getBytes("UTF-8"));
            
            try {
                // Create and load the configuration
                currentConfig = new GenericConfig();
                currentConfig.setConfFile(filename);
                
                // Close any existing configuration
                if (currentConfig != null) {
                    try {
                        currentConfig.close();
                    } catch (Exception e) {
                        // Ignore close errors from previous config
                    }
                }
                
                // Load the new configuration
                currentConfig.create();
                
                // Create graph from the configuration
                Graph graph = new Graph();
                graph.createFromTopics();
                
                // Check for cycles
                if (graph.hasCycles()) {
                    sendErrorResponse(toClient, "Configuration contains cycles and cannot be loaded");
                    return;
                }
                
                // Generate graph visualization HTML
                List<String> graphHtml = HtmlGraphWriter.getGraphHTML(graph);
                String htmlResponse = String.join("\n", graphHtml);
                
                // Send successful response
                sendHtmlResponse(toClient, htmlResponse);
                
            } catch (Exception e) {
                sendErrorResponse(toClient, "Error loading configuration: " + e.getMessage());
            } finally {
                // Clean up temporary file
                try {
                    Files.deleteIfExists(tempConfigPath);
                } catch (IOException e) {
                    // Ignore cleanup errors
                }
            }
            
        } catch (Exception e) {
            sendErrorResponse(toClient, "Error processing upload: " + e.getMessage());
        }
    }
    
    private void sendHtmlResponse(OutputStream toClient, String html) throws IOException {
        String response = String.format(
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "Content-Length: %d\r\n" +
            "Cache-Control: no-cache\r\n" +
            "\r\n" +
            "%s",
            html.getBytes("UTF-8").length, html
        );
        toClient.write(response.getBytes("UTF-8"));
        toClient.flush();
    }
    
    private void sendErrorResponse(OutputStream toClient, String error) throws IOException {
        String html = String.format(
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Configuration Error</title>\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; padding: 40px; text-align: center; background-color: #f8f9fa; }\n" +
            "        .error-container { background-color: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); max-width: 600px; margin: 0 auto; }\n" +
            "        .error-icon { font-size: 64px; margin-bottom: 20px; color: #f44336; }\n" +
            "        .error-title { color: #333; margin-bottom: 15px; }\n" +
            "        .error-message { color: #666; margin-bottom: 20px; line-height: 1.5; }\n" +
            "        .error-details { background-color: #ffebee; padding: 15px; border-radius: 4px; color: #c62828; text-align: left; font-family: monospace; white-space: pre-wrap; }\n" +
            "        .retry-info { margin-top: 20px; color: #666; font-size: 14px; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"error-container\">\n" +
            "        <div class=\"error-icon\">WARNING</div>\n" +
            "        <h1 class=\"error-title\">Configuration Load Error</h1>\n" +
            "        <p class=\"error-message\">There was an error loading your configuration file.</p>\n" +
            "        <div class=\"error-details\">%s</div>\n" +
            "        <div class=\"retry-info\">\n" +
            "            Please check your configuration file format and try again.<br>\n" +
            "            Make sure the file follows the required format with agent class names and topic mappings.\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            escapeHtml(error)
        );
        
        sendHtmlResponse(toClient, html);
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
        if (currentConfig != null) {
            try {
                currentConfig.close();
            } catch (Exception e) {
                // Log error but don't throw
                System.err.println("Error closing configuration: " + e.getMessage());
            }
        }
    }
}