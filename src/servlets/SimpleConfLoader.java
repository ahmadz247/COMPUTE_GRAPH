package servlets;

import server.RequestParser.RequestInfo;
import configs.GenericConfig;
import configs.Graph;
import views.HtmlGraphWriter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SimpleConfLoader implements Servlet {
    private GenericConfig currentConfig;
    
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            // For this simple version, we'll use a predefined configuration
            String configContent = "test.PlusAgent\nA,B\nC\ntest.IncAgent\nC\nD";
            
            // Generate a unique filename
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = "config_" + timestamp + ".conf";
            
            // Save the configuration to a temporary file
            Path tempConfigPath = Paths.get(filename);
            Files.write(tempConfigPath, configContent.getBytes("UTF-8"));
            
            try {
                // Close any existing configuration
                if (currentConfig != null) {
                    try {
                        currentConfig.close();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                
                // Create and load the configuration
                currentConfig = new GenericConfig();
                currentConfig.setConfFile(filename);
                currentConfig.create();
                
                // Create graph from the configuration
                Graph graph = new Graph();
                graph.createFromTopics();
                
                // Generate graph visualization HTML
                List<String> graphHtml = HtmlGraphWriter.getGraphHTML(graph);
                String htmlResponse = String.join("\n", graphHtml);
                
                // Send successful response
                sendHtmlResponse(toClient, htmlResponse);
                
            } catch (Exception e) {
                sendErrorResponse(toClient, "Error: " + e.getMessage());
            } finally {
                // Clean up temporary file
                try {
                    Files.deleteIfExists(tempConfigPath);
                } catch (IOException e) {
                    // Ignore
                }
            }
            
        } catch (Exception e) {
            sendErrorResponse(toClient, "Error: " + e.getMessage());
        }
    }
    
    private void sendHtmlResponse(OutputStream toClient, String html) throws IOException {
        String response = String.format(
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "Content-Length: %d\r\n" +
            "\r\n" +
            "%s",
            html.getBytes("UTF-8").length, html
        );
        toClient.write(response.getBytes("UTF-8"));
        toClient.flush();
    }
    
    private void sendErrorResponse(OutputStream toClient, String error) throws IOException {
        String html = String.format(
            "<!DOCTYPE html><html><head><title>Error</title></head>" +
            "<body style='font-family:Arial;padding:20px;'>" +
            "<h2 style='color:#f44336;'>Configuration Error</h2>" +
            "<p>%s</p>" +
            "<p><a href='javascript:history.back()'>Go Back</a></p>" +
            "</body></html>",
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
                System.err.println("Error closing configuration: " + e.getMessage());
            }
        }
    }
}