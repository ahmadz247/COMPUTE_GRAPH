package server;

import servlets.Servlet;

/**
 * Interface for HTTP server implementations.
 * Defines the contract for a multi-threaded HTTP server with servlet support.
 * 
 * Extends Runnable to allow the server to run in its own thread.
 * Uses the Front Controller pattern with servlet routing.
 * 
 * @author Advanced Programming Course
 */
public interface HTTPServer extends Runnable {
    /**
     * Registers a servlet to handle requests for a specific HTTP method and URI pattern.
     * Uses longest-prefix matching for URI patterns.
     * 
     * @param httpCommand The HTTP method (GET, POST, DELETE)
     * @param uri The URI pattern to match (e.g., "/api/", "/")
     * @param s The servlet instance to handle matching requests
     */
    void addServlet(String httpCommand, String uri, Servlet s);
    
    /**
     * Unregisters a servlet for the specified method and URI pattern.
     * 
     * @param httpCommand The HTTP method
     * @param uri The URI pattern
     */
    void removeServlet(String httpCommand, String uri);
    
    /**
     * Starts the HTTP server and begins accepting connections.
     * This method should return immediately after starting the server thread.
     */
    void start();
    
    /**
     * Gracefully shuts down the server, closing all connections and resources.
     * Should close all registered servlets and stop accepting new connections.
     */
    void close();
}