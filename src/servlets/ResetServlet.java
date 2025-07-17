package servlets;

import server.RequestParser.RequestInfo;
import graph.TopicManagerSingleton;
import java.io.*;

public class ResetServlet implements Servlet {
    
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        // Clear all topics
        TopicManagerSingleton.get().clear();
        
        // Send success response
        String response = "<!DOCTYPE html><html><head>" +
                         "<meta http-equiv='refresh' content='1;url=temp.html'>" +
                         "</head><body>" +
                         "<h2>System Reset Complete</h2>" +
                         "<p>All topics and agents have been cleared.</p>" +
                         "</body></html>";
        
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                             "Content-Type: text/html\r\n" +
                             "Content-Length: " + response.length() + "\r\n" +
                             "\r\n" +
                             response;
        
        toClient.write(httpResponse.getBytes());
        toClient.flush();
    }
    
    @Override
    public void close() throws IOException {
        // No resources to close
    }
}