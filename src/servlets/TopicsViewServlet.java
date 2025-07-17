package servlets;

import server.RequestParser.RequestInfo;
import graph.TopicManagerSingleton;
import graph.Topic;
import graph.Message;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class TopicsViewServlet implements Servlet {
    
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            // Get TopicManager
            var topicManager = TopicManagerSingleton.get();
            
            // Check if this is a reset request
            boolean showResetMessage = ri.getParameters().containsKey("reset");
            
            // Generate HTML response with topics table
            String htmlResponse = generateTopicsTable(topicManager.getTopics(), showResetMessage);
            
            // Send HTTP response
            sendHtmlResponse(toClient, htmlResponse);
            
        } catch (Exception e) {
            sendErrorResponse(toClient, "Error displaying topics: " + e.getMessage());
        }
    }
    
    private String generateTopicsTable(Collection<Topic> topics, boolean showResetMessage) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Topics Monitor</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; padding: 10px; margin: 0; background-color: #f8f9fa; height: 100vh; overflow: hidden; box-sizing: border-box; }\n");
        html.append("        .table-container { background-color: white; border-radius: 8px; padding: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); height: calc(100vh - 20px); box-sizing: border-box; display: flex; flex-direction: column; }\n");
        html.append("        .table-title { color: #333; margin-bottom: 10px; font-size: 16px; font-weight: bold; text-align: center; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; flex-grow: 1; }\n");
        html.append("        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append("        th { background-color: #f8f9fa; font-weight: bold; color: #333; }\n");
        html.append("        tr:hover { background-color: #f5f5f5; }\n");
        html.append("        .topic-name { font-weight: bold; color: #1976d2; }\n");
        html.append("        .topic-value { font-family: monospace; color: #2e7d32; }\n");
        html.append("        .no-data { text-align: center; color: #666; font-style: italic; padding: 20px; }\n");
        html.append("        .timestamp { font-size: 12px; color: #666; }\n");
        html.append("        .reset-message { background-color: #e8f5e9; color: #2e7d32; padding: 10px; margin-bottom: 10px; border-radius: 4px; text-align: center; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"table-container\">\n");
        html.append("        <div class=\"table-title\">Topic Values</div>\n");
        
        // Show reset message if requested
        if (showResetMessage) {
            html.append("        <div class=\"reset-message\">Topics have been reset successfully!</div>\n");
        }
        
        if (topics.isEmpty()) {
            html.append("        <div class=\"no-data\">No topics available</div>\n");
        } else {
            html.append("        <table>\n");
            html.append("            <thead>\n");
            html.append("                <tr>\n");
            html.append("                    <th>Topic Name</th>\n");
            html.append("                    <th>Latest Value</th>\n");
            html.append("                </tr>\n");
            html.append("            </thead>\n");
            html.append("            <tbody>\n");
            
            for (Topic topic : topics) {
                html.append("                <tr>\n");
                html.append("                    <td class=\"topic-name\">").append(escapeHtml(topic.name)).append("</td>\n");
                html.append("                    <td class=\"topic-value\">").append(getTopicValue(topic)).append("</td>\n");
                html.append("                </tr>\n");
            }
            
            html.append("            </tbody>\n");
            html.append("        </table>\n");
        }
        
        html.append("        <div class=\"timestamp\">Last updated: ").append(new java.util.Date()).append("</div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    private String getTopicValue(Topic topic) {
        Message lastMessage = topic.getLastMessage();
        if (lastMessage == null) {
            return "No data";
        }
        
        // Try to display as double first, then as text
        if (!Double.isNaN(lastMessage.asDouble)) {
            return String.valueOf(lastMessage.asDouble);
        } else if (lastMessage.asText != null && !lastMessage.asText.isEmpty()) {
            return lastMessage.asText;
        } else {
            return "Binary data";
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
            "<body><h1>Error</h1><p>%s</p></body></html>",
            escapeHtml(error)
        );
        sendHtmlResponse(toClient, html);
    }
    
    @Override
    public void close() throws IOException {
        // No resources to close
    }
}