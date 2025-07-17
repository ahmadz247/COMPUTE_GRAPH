package servlets;

import server.RequestParser.RequestInfo;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface for HTTP request handlers (servlets).
 * Each servlet handles requests for a specific URI pattern and HTTP method.
 * 
 * This is a simplified version of the standard Java Servlet interface,
 * designed for educational purposes in the Advanced Programming course.
 * 
 * @author Advanced Programming Course
 */
public interface Servlet {
    /**
     * Handles an HTTP request and writes the response.
     * 
     * @param ri Request information including method, URI, headers, and parameters
     * @param toClient Output stream for writing the HTTP response
     * @throws IOException if an I/O error occurs during request handling
     */
    void handle(RequestInfo ri, OutputStream toClient) throws IOException;
    
    /**
     * Closes the servlet and releases any resources.
     * Called when the server is shutting down or the servlet is being removed.
     * 
     * @throws IOException if an error occurs during cleanup
     */
    void close() throws IOException;
}