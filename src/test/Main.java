package test;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Computational Graph System ===");
        System.out.println("Starting HTTP Server on port 8080...");
        
        // Create HTTP server with 5 worker threads
        HTTPServer server = new MyHTTPServer(8080, 5);
        
        // Register servlets
        System.out.println("Registering servlets...");
        
        // Topic publisher - handles GET /publish requests
        server.addServlet("GET", "/publish", new TopicDisplayer());
        System.out.println("  [OK] TopicDisplayer registered for GET /publish");
        
        // Configuration loader - handles POST /upload requests  
        server.addServlet("POST", "/upload", new ConfLoader());
        System.out.println("  [OK] ConfLoader registered for POST /upload");
        
        // Static file server - handles GET /app/* requests
        server.addServlet("GET", "/app/", new HtmlLoader("files_html"));
        System.out.println("  [OK] HtmlLoader registered for GET /app/*");
        
        // Start the server
        System.out.println("\nStarting server...");
        server.start();
        
        System.out.println("[OK] Server started successfully!");
        System.out.println();
        System.out.println("Open your browser and navigate to:");
        System.out.println("   http://localhost:8080/app/index.html");
        System.out.println();
        System.out.println("Available features:");
        System.out.println("   * Upload configuration files to create computational graphs");
        System.out.println("   * Publish messages to topics and see real-time results");
        System.out.println("   * Visual graph display with topics and agents");
        System.out.println("   * Live monitoring of topic values");
        System.out.println();
        System.out.println("Press ENTER to stop the server...");
        
        // Wait for user input to stop
        System.in.read();
        
        // Shutdown
        System.out.println("\n[STOP] Shutting down server...");
        server.close();
        System.out.println("[OK] Server stopped. Goodbye!");
    }
}