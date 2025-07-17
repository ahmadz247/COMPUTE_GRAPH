package server;

import servlets.Servlet;
import server.RequestParser.RequestInfo;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Multi-threaded HTTP server implementation with servlet support.
 * Handles concurrent client requests using a thread pool architecture.
 * 
 * Key Features:
 * - Concurrent request handling via ExecutorService
 * - Servlet routing with longest-prefix matching algorithm
 * - Graceful shutdown with resource cleanup
 * - Support for GET, POST, DELETE HTTP methods
 * 
 * SOLID Principles:
 * - Single Responsibility: Focuses on HTTP server operations only
 * - Open/Closed: New servlets can be added without modifying core server
 * - Dependency Inversion: Depends on Servlet interface, not implementations
 * 
 * Design Patterns:
 * - Thread Pool Pattern for scalable request handling
 * - Front Controller Pattern with servlet routing
 * 
 * @author Advanced Programming Course
 */
public class MyHTTPServer extends Thread implements HTTPServer {
    private final int port;
    private final int nThreads;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = false;
    
    // Thread-safe maps for servlets
    private final ConcurrentHashMap<String, Servlet> getServlets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Servlet> postServlets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Servlet> deleteServlets = new ConcurrentHashMap<>();
    
    public MyHTTPServer(int port, int nThreads) {
        this.port = port;
        this.nThreads = nThreads;
        this.threadPool = Executors.newFixedThreadPool(nThreads);
    }
    
    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
        switch (httpCommand.toUpperCase()) {
            case "GET":
                getServlets.put(uri, s);
                break;
            case "POST":
                postServlets.put(uri, s);
                break;
            case "DELETE":
                deleteServlets.put(uri, s);
                break;
        }
    }
    
    @Override
    public void removeServlet(String httpCommand, String uri) {
        switch (httpCommand.toUpperCase()) {
            case "GET":
                getServlets.remove(uri);
                break;
            case "POST":
                postServlets.remove(uri);
                break;
            case "DELETE":
                deleteServlets.remove(uri);
                break;
        }
    }
    
    @Override
    public void start() {
        running = true;
        super.start(); // Start the thread
    }
    
    /**
     * Performs graceful server shutdown with complete resource cleanup.
     * 
     * Shutdown sequence:
     * 1. Set running flag to stop accept loop
     * 2. Close all servlet instances (avoiding duplicates with Set)
     * 3. Shutdown thread pool with timeout
     * 4. Close server socket to release port
     * 5. Interrupt main thread if blocked
     * 
     * Resource management:
     * - Uses Set to prevent closing shared servlets multiple times
     * - Two-phase thread pool shutdown (graceful then forced)
     * - Handles all exceptions to ensure complete cleanup
     * 
     * Thread safety:
     * - volatile 'running' flag for visibility across threads
     * - Defensive null checks before closing resources
     * 
     * SOLID: Interface Segregation - Implements only close() from HTTPServer
     */
    @Override
    public void close() {
        running = false;
        
        // Close all servlets (use Set to avoid closing the same servlet twice)
        Set<Servlet> allServlets = new HashSet<>();
        allServlets.addAll(getServlets.values());
        allServlets.addAll(postServlets.values());
        allServlets.addAll(deleteServlets.values());
        
        for (Servlet servlet : allServlets) {
            try {
                servlet.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Shutdown thread pool
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Interrupt this thread if it's waiting
        this.interrupt();
    }
    
    /**
     * Main server loop - accepts incoming connections and delegates to thread pool.
     * 
     * Architecture:
     * - ServerSocket listens on specified port
     * - accept() blocks with 1-second timeout for graceful shutdown
     * - Each connection handled by worker thread from pool
     * 
     * Scalability features:
     * - Thread pool prevents thread creation overhead
     * - Concurrent request handling up to nThreads limit
     * - Non-blocking delegation to worker threads
     * 
     * Graceful shutdown:
     * - Timeout allows periodic check of 'running' flag
     * - Ignores IOExceptions during shutdown
     * - Ensures close() is called in finally block
     * 
     * SOLID: Liskov Substitution - Properly implements Thread.run()
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000); // 1 second timeout
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    
                    // Handle client in thread pool
                    threadPool.execute(() -> handleClient(clientSocket));
                    
                } catch (SocketTimeoutException e) {
                    // Timeout is normal, continue loop to check if still running
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }
    
    /**
     * Handles individual client HTTP requests in a separate thread.
     * Implements the complete request-response cycle:
     * 
     * 1. Parse incoming HTTP request
     * 2. Route to appropriate servlet using longest-prefix matching
     * 3. Execute servlet handler
     * 4. Send response or error
     * 5. Close connection (HTTP/1.0 style)
     * 
     * Error handling:
     * - 400 Bad Request: Malformed HTTP request
     * - 404 Not Found: No matching servlet for URI
     * 
     * Resource management:
     * - Uses try-with-resources for automatic cleanup
     * - Ensures socket is closed even on exceptions
     * 
     * @param clientSocket The client connection to handle
     */
    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream output = clientSocket.getOutputStream()) {
            
            // Parse request
            RequestInfo requestInfo = RequestParser.parseRequest(reader);
            
            if (requestInfo == null) {
                sendErrorResponse(output, 400, "Bad Request");
                return;
            }
            
            // Find matching servlet
            Servlet servlet = findServlet(requestInfo.getHttpCommand(), requestInfo.getUri());
            
            if (servlet == null) {
                sendErrorResponse(output, 404, "Not Found");
                return;
            }
            
            // Handle request with servlet
            servlet.handle(requestInfo, output);
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Implements longest-prefix matching algorithm for servlet routing.
     * This allows flexible URL patterns like:
     * - /api/users -> matches /api/users/123
     * - /api -> matches /api/anything
     * - / -> matches everything (default handler)
     * 
     * Algorithm complexity: O(n) where n is number of registered servlets
     * 
     * Example:
     * If servlets are registered for "/api/", "/api/users/", and "/"
     * URI "/api/users/123" will match "/api/users/" (longest prefix)
     * 
     * Thread safety: Uses ConcurrentHashMap for concurrent access
     * 
     * @param httpCommand The HTTP method (GET, POST, DELETE)
     * @param uri The requested URI path
     * @return The servlet handling the longest matching prefix, or null
     */
    private Servlet findServlet(String httpCommand, String uri) {
        ConcurrentHashMap<String, Servlet> servletMap;
        
        switch (httpCommand.toUpperCase()) {
            case "GET":
                servletMap = getServlets;
                break;
            case "POST":
                servletMap = postServlets;
                break;
            case "DELETE":
                servletMap = deleteServlets;
                break;
            default:
                return null;
        }
        
        // Find longest prefix match
        String longestMatch = "";
        Servlet matchingServlet = null;
        
        for (Map.Entry<String, Servlet> entry : servletMap.entrySet()) {
            String prefix = entry.getKey();
            if (uri.startsWith(prefix) && prefix.length() > longestMatch.length()) {
                longestMatch = prefix;
                matchingServlet = entry.getValue();
            }
        }
        
        return matchingServlet;
    }
    
    private void sendErrorResponse(OutputStream output, int code, String message) throws IOException {
        String response = String.format(
            "HTTP/1.1 %d %s\r\n" +
            "Content-Type: text/plain\r\n" +
            "Content-Length: %d\r\n" +
            "\r\n" +
            "%s",
            code, message, message.length(), message
        );
        output.write(response.getBytes());
        output.flush();
    }
}