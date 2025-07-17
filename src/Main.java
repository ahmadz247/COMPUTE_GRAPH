import server.HTTPServer;
import server.MyHTTPServer;
import servlets.TopicDisplayer;
import servlets.ConfLoader;
import servlets.HtmlLoader;
import servlets.ResetServlet;
import servlets.ResetTopicsServlet;
import servlets.TopicsViewServlet;

public class Main {
    public static void main(String[] args) throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);

        server.addServlet("GET" , "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload" , new ConfLoader());
        server.addServlet("GET" , "/app/"  , new HtmlLoader("files_html"));
        server.addServlet("GET" , "/"      , new HtmlLoader("files_html"));  // Also serve from root
        server.addServlet("GET" , "/reset"  , new ResetServlet());
        server.addServlet("GET" , "/reset-topics", new ResetTopicsServlet());
        server.addServlet("GET" , "/topics", new TopicsViewServlet());

        server.start();
        System.out.println("Server started on port 8080");
        System.out.println("Open http://localhost:8080 in your browser");
        System.out.println("Press Enter to stop the server...");
        System.in.read();   // wait for key-press
        server.close();
        System.out.println("Server stopped");
    }
}