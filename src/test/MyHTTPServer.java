package test;

import test.RequestParser.RequestInfo;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

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