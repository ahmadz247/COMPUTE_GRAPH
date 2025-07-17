package server;

import servlets.Servlet;
import server.RequestParser.RequestInfo;

import java.io.*;
import java.net.*;
import java.util.*;

public class Ex5Test {
    
    // Simple test servlet that echoes parameters
    static class EchoServlet implements Servlet {
        private boolean closed = false;
        
        @Override
        public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
            StringBuilder response = new StringBuilder();
            response.append("Echo Servlet Response\n");
            response.append("HTTP Command: ").append(ri.getHttpCommand()).append("\n");
            response.append("URI: ").append(ri.getUri()).append("\n");
            response.append("URI Components: ").append(Arrays.toString(ri.getUriComponents())).append("\n");
            response.append("Parameters:\n");
            
            for (Map.Entry<String, String> param : ri.getParameters().entrySet()) {
                response.append("  ").append(param.getKey()).append(" = ").append(param.getValue()).append("\n");
            }
            
            if (ri.getContent().length > 0) {
                response.append("Content: ").append(new String(ri.getContent())).append("\n");
            }
            
            String body = response.toString();
            String httpResponse = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: " + body.length() + "\r\n" +
                                "\r\n" +
                                body;
            
            toClient.write(httpResponse.getBytes());
            toClient.flush();
        }
        
        @Override
        public void close() throws IOException {
            closed = true;
            System.out.println("EchoServlet closed");
        }
        
        public boolean isClosed() {
            return closed;
        }
    }
    
    // Calculator servlet for testing
    static class CalculatorServlet implements Servlet {
        @Override
        public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
            Map<String, String> params = ri.getParameters();
            String operation = params.get("op");
            double a = Double.parseDouble(params.getOrDefault("a", "0"));
            double b = Double.parseDouble(params.getOrDefault("b", "0"));
            
            double result = 0;
            switch (operation) {
                case "add":
                    result = a + b;
                    break;
                case "subtract":
                    result = a - b;
                    break;
                case "multiply":
                    result = a * b;
                    break;
                case "divide":
                    result = b != 0 ? a / b : Double.POSITIVE_INFINITY;
                    break;
            }
            
            String body = String.format("Result: %.2f", result);
            String httpResponse = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: " + body.length() + "\r\n" +
                                "\r\n" +
                                body;
            
            toClient.write(httpResponse.getBytes());
            toClient.flush();
        }
        
        @Override
        public void close() throws IOException {
            System.out.println("CalculatorServlet closed");
        }
    }
    
    public static void testRequestParser() {
        System.out.println("Test 1: RequestParser");
        
        String request = "GET /api/resource?id=123&name=test HTTP/1.1\r\n" +
                        "Host: example.com\r\n" +
                        "Content-Length: 12\r\n" +
                        "\r\n" +
                        "filename=\"hello_world.txt\"\r\n" +
                        "\r\n" +
                        "hello world!";
        
        try (BufferedReader reader = new BufferedReader(new StringReader(request))) {
            RequestInfo info = RequestParser.parseRequest(reader);
            
            System.out.println("[PASS] HTTP Command: " + info.getHttpCommand());
            System.out.println("[PASS] URI: " + info.getUri());
            System.out.println("[PASS] URI Components: " + Arrays.toString(info.getUriComponents()));
            System.out.println("[PASS] Parameters: " + info.getParameters());
            System.out.println("[PASS] Content: " + new String(info.getContent()));
            
        } catch (IOException e) {
            System.out.println("[FAIL] " + e.getMessage());
        }
        
        System.out.println();
    }
    
    public static void testBasicServer() {
        System.out.println("Test 2: Basic Server Functionality");
        
        int port = 8080;
        MyHTTPServer server = new MyHTTPServer(port, 2);
        EchoServlet echoServlet = new EchoServlet();
        
        // Add servlet
        server.addServlet("GET", "/echo", echoServlet);
        
        // Check thread count before start
        int threadsBefore = Thread.activeCount();
        System.out.println("[PASS] Threads before start: " + threadsBefore);
        
        // Start server
        server.start();
        
        try {
            Thread.sleep(100); // Give server time to start
        } catch (InterruptedException e) {}
        
        int threadsAfter = Thread.activeCount();
        System.out.println("[PASS] Threads after start: " + threadsAfter);
        System.out.println("[PASS] Additional threads created: " + (threadsAfter - threadsBefore));
        
        // Test client connection
        try (Socket client = new Socket("localhost", port);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            
            // Send HTTP request
            out.println("GET /echo?test=hello&value=123 HTTP/1.1");
            out.println("Host: localhost");
            out.println();
            
            // Read response
            String line;
            System.out.println("\nServer Response:");
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println("  " + line);
            }
            
            // Read body
            System.out.println("\nResponse Body:");
            while ((line = in.readLine()) != null) {
                System.out.println("  " + line);
            }
            
        } catch (IOException e) {
            System.out.println("[FAIL] Client connection: " + e.getMessage());
        }
        
        // Close server
        server.close();
        
        try {
            Thread.sleep(2000); // Wait for shutdown
        } catch (InterruptedException e) {}
        
        int threadsAfterClose = Thread.activeCount();
        System.out.println("\n[PASS] Threads after close: " + threadsAfterClose);
        System.out.println("[PASS] Servlet closed: " + echoServlet.isClosed());
        
        System.out.println();
    }
    
    public static void testLongestPrefixMatching() {
        System.out.println("Test 3: Longest Prefix Matching");
        
        int port = 8081;
        MyHTTPServer server = new MyHTTPServer(port, 2);
        
        // Add servlets with different prefixes
        server.addServlet("GET", "/", new EchoServlet());
        server.addServlet("GET", "/api", new EchoServlet());
        server.addServlet("GET", "/api/calc", new CalculatorServlet());
        
        server.start();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
        
        // Test /api/calc/operation - should match /api/calc
        try (Socket client = new Socket("localhost", port);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            
            out.println("GET /api/calc/operation?op=add&a=10&b=5 HTTP/1.1");
            out.println("Host: localhost");
            out.println();
            
            // Skip headers
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {}
            
            // Read result
            line = in.readLine();
            System.out.println("[PASS] Calculator result: " + line);
            
        } catch (IOException e) {
            System.out.println("[FAIL] " + e.getMessage());
        }
        
        server.close();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        
        System.out.println();
    }
    
    public static void testMultipleClients() {
        System.out.println("Test 4: Multiple Concurrent Clients");
        
        int port = 8082;
        MyHTTPServer server = new MyHTTPServer(port, 4);
        server.addServlet("GET", "/calc", new CalculatorServlet());
        
        server.start();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
        
        // Create multiple client threads
        Thread[] clients = new Thread[3];
        for (int i = 0; i < clients.length; i++) {
            final int clientId = i;
            clients[i] = new Thread(() -> {
                try (Socket client = new Socket("localhost", port);
                     PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                    
                    int a = clientId * 10;
                    int b = clientId * 5;
                    out.println("GET /calc?op=add&a=" + a + "&b=" + b + " HTTP/1.1");
                    out.println("Host: localhost");
                    out.println();
                    
                    // Skip headers
                    String line;
                    while ((line = in.readLine()) != null && !line.isEmpty()) {}
                    
                    // Read result
                    line = in.readLine();
                    System.out.println("[PASS] Client " + clientId + " result: " + line);
                    
                } catch (IOException e) {
                    System.out.println("[FAIL] Client " + clientId + ": " + e.getMessage());
                }
            });
            clients[i].start();
        }
        
        // Wait for all clients
        for (Thread client : clients) {
            try {
                client.join();
            } catch (InterruptedException e) {}
        }
        
        server.close();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        
        System.out.println();
    }
    
    public static void testPostRequest() {
        System.out.println("Test 5: POST Request with Content");
        
        int port = 8083;
        MyHTTPServer server = new MyHTTPServer(port, 2);
        server.addServlet("POST", "/upload", new EchoServlet());
        
        server.start();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
        
        try (Socket client = new Socket("localhost", port);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            
            String content = "This is test content";
            out.println("POST /upload?filename=test.txt HTTP/1.1");
            out.println("Host: localhost");
            out.println("Content-Length: " + content.length());
            out.println();
            out.println("metadata=test");
            out.println();
            out.print(content);
            out.flush();
            
            // Read response
            String line;
            System.out.println("Response:");
            while ((line = in.readLine()) != null) {
                System.out.println("  " + line);
            }
            
        } catch (IOException e) {
            System.out.println("[FAIL] " + e.getMessage());
        }
        
        server.close();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        
        System.out.println();
    }
    
    public static void main(String[] args) {
        System.out.println("=== Exercise 5 Test Suite ===\n");
        
        testRequestParser();
        testBasicServer();
        testLongestPrefixMatching();
        testMultipleClients();
        testPostRequest();
        
        System.out.println("=== All tests completed ===");
        
        // Final thread check
        System.out.println("\nFinal thread count: " + Thread.activeCount());
    }
}