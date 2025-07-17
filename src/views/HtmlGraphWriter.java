package views;

import configs.Graph;
import configs.Node;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Converts computational graphs to HTML visualization format.
 * Responsible for graph layout algorithms and HTML generation.
 * 
 * Key features:
 * - Multiple layout strategies (bipartite, circular, grid)
 * - Automatic node type detection and visualization
 * - Space-efficient node labeling (operation symbols instead of full names)
 * - Fallback mechanisms for robustness
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles graph-to-HTML conversion
 * - Open/Closed: New layout algorithms can be added without modifying existing code
 * - Dependency Inversion: Works with abstract Graph/Node interfaces
 * 
 * Design Pattern: Strategy Pattern for layout algorithms
 * 
 * @author Advanced Programming Course
 */
public class HtmlGraphWriter {
    
    public static List<String> getGraphHTML(Graph graph) {
        try {
            // Load the graph template
            String template = loadTemplate();
            
            // Generate graph data
            GraphData graphData = analyzeGraph(graph);
            
            // Replace placeholders in template
            String html = template
                .replace("{{GRAPH_TITLE}}", escapeHtml("Computational Graph - " + graphData.nodes.size() + " nodes"))
                .replace("{{CANVAS_WIDTH}}", "700")
                .replace("{{CANVAS_HEIGHT}}", "500")
                .replace("{{GRAPH_DATA}}", generateGraphDataJson(graphData))
                .replace("{{GRAPH_INFO}}", generateGraphInfo(graphData));
            
            return Arrays.asList(html);
            
        } catch (Exception e) {
            // Fallback to simple HTML if template loading fails
            return generateFallbackHTML(graph, e.getMessage());
        }
    }
    
    private static String loadTemplate() throws IOException {
        // Try to load the graph template
        try {
            return new String(Files.readAllBytes(Paths.get("files_html/graph.html")), "UTF-8");
        } catch (IOException e) {
            // Fallback template if file not found
            return generateBuiltInTemplate();
        }
    }
    
    private static String generateBuiltInTemplate() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>{{GRAPH_TITLE}}</title>\n" +
               "    <style>\n" +
               "        body { font-family: Arial, sans-serif; margin: 0; padding: 10px; background-color: #f8f9fa; height: 100vh; overflow: hidden; box-sizing: border-box; }\n" +
               "        .graph-container { background-color: white; border-radius: 8px; padding: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); height: calc(100vh - 20px); box-sizing: border-box; display: flex; flex-direction: column; }\n" +
               "        .graph-title { text-align: center; margin-bottom: 20px; color: #333; }\n" +
               "        #graphCanvas { border: 1px solid #ddd; border-radius: 4px; display: block; margin: 0 auto; flex-grow: 1; max-width: 100%; }\n" +
               "        .legend { display: flex; justify-content: center; gap: 30px; margin-top: 15px; font-size: 14px; }\n" +
               "        .legend-item { display: flex; align-items: center; gap: 8px; }\n" +
               "        .legend-shape { width: 20px; height: 20px; border: 2px solid #333; }\n" +
               "        .topic-shape { background-color: #e3f2fd; }\n" +
               "        .agent-shape { background-color: #fff3e0; border-radius: 50%; }\n" +
               "        .graph-info { margin-top: 20px; padding: 15px; background-color: #f8f9fa; border-radius: 4px; font-size: 14px; color: #666; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"graph-container\">\n" +
               "        <h2 class=\"graph-title\">{{GRAPH_TITLE}}</h2>\n" +
               "        <canvas id=\"graphCanvas\" width=\"{{CANVAS_WIDTH}}\" height=\"{{CANVAS_HEIGHT}}\"></canvas>\n" +
               "        <div class=\"legend\">\n" +
               "            <div class=\"legend-item\"><div class=\"legend-shape topic-shape\"></div><span>Topics</span></div>\n" +
               "            <div class=\"legend-item\"><div class=\"legend-shape agent-shape\"></div><span>Agents</span></div>\n" +
               "        </div>\n" +
               "        <div class=\"graph-info\"><strong>Graph Information:</strong><br>{{GRAPH_INFO}}</div>\n" +
               "    </div>\n" +
               "    <script>\n" +
               "        const graphData = {{GRAPH_DATA}};\n" +
               "        const canvas = document.getElementById('graphCanvas');\n" +
               "        const ctx = canvas.getContext('2d');\n" +
               "        \n" +
               "        function drawGraph() {\n" +
               "            ctx.clearRect(0, 0, canvas.width, canvas.height);\n" +
               "            drawConnections();\n" +
               "            drawNodes();\n" +
               "        }\n" +
               "        \n" +
               "        function drawNodes() {\n" +
               "            graphData.nodes.forEach(node => {\n" +
               "                if (node.type === 'topic') drawTopic(node);\n" +
               "                else if (node.type === 'agent') drawAgent(node);\n" +
               "            });\n" +
               "        }\n" +
               "        \n" +
               "        function drawTopic(node) {\n" +
               "            ctx.fillStyle = '#e3f2fd'; ctx.strokeStyle = '#1976d2'; ctx.lineWidth = 2;\n" +
               "            ctx.fillRect(node.x - 40, node.y - 15, 80, 30);\n" +
               "            ctx.strokeRect(node.x - 40, node.y - 15, 80, 30);\n" +
               "            ctx.fillStyle = '#1976d2'; ctx.font = '12px Arial'; ctx.textAlign = 'center';\n" +
               "            ctx.fillText(node.name, node.x, node.y + 4);\n" +
               "        }\n" +
               "        \n" +
               "        function drawAgent(node) {\n" +
               "            ctx.fillStyle = '#fff3e0'; ctx.strokeStyle = '#f57c00'; ctx.lineWidth = 2;\n" +
               "            ctx.beginPath(); ctx.arc(node.x, node.y, 25, 0, 2 * Math.PI); ctx.fill(); ctx.stroke();\n" +
               "            \n" +
               "            // Draw text with truncation for long names\n" +
               "            ctx.fillStyle = '#f57c00';\n" +
               "            ctx.font = '11px Arial';\n" +
               "            ctx.textAlign = 'center';\n" +
               "            \n" +
               "            // Measure text and truncate if needed\n" +
               "            const maxWidth = 40; // Maximum width for text\n" +
               "            let text = node.name;\n" +
               "            let metrics = ctx.measureText(text);\n" +
               "            \n" +
               "            if (metrics.width > maxWidth) {\n" +
               "                // Truncate text and add ellipsis\n" +
               "                while (metrics.width > maxWidth && text.length > 0) {\n" +
               "                    text = text.substring(0, text.length - 1);\n" +
               "                    metrics = ctx.measureText(text + '...');\n" +
               "                }\n" +
               "                text = text + '...';\n" +
               "            }\n" +
               "            \n" +
               "            ctx.fillText(text, node.x, node.y + 4);\n" +
               "        }\n" +
               "        \n" +
               "        function drawConnections() {\n" +
               "            ctx.strokeStyle = '#666'; ctx.lineWidth = 2;\n" +
               "            graphData.edges.forEach(edge => {\n" +
               "                const fromNode = graphData.nodes.find(n => n.id === edge.from);\n" +
               "                const toNode = graphData.nodes.find(n => n.id === edge.to);\n" +
               "                if (fromNode && toNode) drawArrow(fromNode.x, fromNode.y, toNode.x, toNode.y);\n" +
               "            });\n" +
               "        }\n" +
               "        \n" +
               "        function drawArrow(fromX, fromY, toX, toY) {\n" +
               "            const dx = toX - fromX, dy = toY - fromY, length = Math.sqrt(dx*dx + dy*dy);\n" +
               "            const unitX = dx / length, unitY = dy / length;\n" +
               "            const startX = fromX + unitX * 30, startY = fromY + unitY * 30;\n" +
               "            const endX = toX - unitX * 30, endY = toY - unitY * 30;\n" +
               "            ctx.beginPath(); ctx.moveTo(startX, startY); ctx.lineTo(endX, endY); ctx.stroke();\n" +
               "            const arrowLength = 10, arrowAngle = Math.PI / 6;\n" +
               "            ctx.beginPath(); ctx.moveTo(endX, endY);\n" +
               "            ctx.lineTo(endX - arrowLength * Math.cos(Math.atan2(dy, dx) - arrowAngle),\n" +
               "                      endY - arrowLength * Math.sin(Math.atan2(dy, dx) - arrowAngle));\n" +
               "            ctx.moveTo(endX, endY);\n" +
               "            ctx.lineTo(endX - arrowLength * Math.cos(Math.atan2(dy, dx) + arrowAngle),\n" +
               "                      endY - arrowLength * Math.sin(Math.atan2(dy, dx) + arrowAngle));\n" +
               "            ctx.stroke();\n" +
               "        }\n" +
               "        \n" +
               "        window.onload = function() { drawGraph(); };\n" +
               "    </script>\n" +
               "</body>\n" +
               "</html>";
    }
    
    /**
     * Analyzes the graph structure and prepares it for visualization.
     * Converts abstract graph nodes to visual elements with positions.
     * 
     * Processing steps:
     * 1. Node classification (Topics vs Agents)
     * 2. Name simplification (operation symbols for agents)
     * 3. Layout calculation based on graph characteristics
     * 4. Edge creation from node relationships
     * 
     * @param graph The computational graph to analyze
     * @return GraphData containing positioned nodes and edges
     */
    private static GraphData analyzeGraph(Graph graph) {
        GraphData data = new GraphData();
        Map<String, GraphNode> nodeMap = new HashMap<>();
        
        // Create nodes from the graph
        int nodeIndex = 0;
        for (Node node : graph) {
            GraphNode graphNode = new GraphNode();
            graphNode.id = "node_" + nodeIndex++;
            graphNode.name = node.getName();
            
            // Determine node type based on name prefix and content
            if (node.getName().startsWith("T")) {
                graphNode.type = "topic";
                graphNode.name = node.getName().substring(1); // Remove T prefix
            } else if (node.getName().startsWith("A")) {
                graphNode.type = "agent";
                String agentName = node.getName().substring(1); // Remove A prefix
                
                // Display operation symbol instead of full name
                if (agentName.startsWith("PlusAgent")) {
                    graphNode.name = "+";
                } else if (agentName.startsWith("IncAgent")) {
                    graphNode.name = "+1";
                } else if (agentName.contains("minus") || agentName.contains("Minus")) {
                    graphNode.name = "-";
                } else if (agentName.contains("mul") || agentName.contains("Mul")) {
                    graphNode.name = "×";
                } else if (agentName.contains("div") || agentName.contains("Div")) {
                    graphNode.name = "÷";
                } else {
                    // For other agents, try to extract a short name
                    graphNode.name = agentName.length() > 10 ? agentName.substring(0, 8) + ".." : agentName;
                }
            } else {
                // Enhanced classification logic
                String name = node.getName().toLowerCase();
                if (name.contains("agent") || name.contains("plus") || name.contains("inc") || name.contains("minus") || name.contains("mul")) {
                    graphNode.type = "agent";
                } else {
                    graphNode.type = "topic";
                }
                graphNode.name = node.getName();
            }
            
            data.nodes.add(graphNode);
            nodeMap.put(node.getName(), graphNode);
        }
        
        // Position nodes in a layout
        layoutNodes(data.nodes);
        
        // Create edges from the graph
        for (Node node : graph) {
            GraphNode fromNode = nodeMap.get(node.getName());
            if (fromNode != null) {
                for (Node edge : node.getEdges()) {
                    GraphNode toNode = nodeMap.get(edge.getName());
                    if (toNode != null) {
                        GraphEdge graphEdge = new GraphEdge();
                        graphEdge.from = fromNode.id;
                        graphEdge.to = toNode.id;
                        data.edges.add(graphEdge);
                    }
                }
            }
        }
        
        return data;
    }
    
    /**
     * Intelligently positions nodes on the canvas based on their type and quantity.
     * Uses different layout algorithms for optimal visualization:
     * - Single node: Centered
     * - Mixed (topics + agents): Bipartite layout (topics left, agents right)
     * - Homogeneous small (≤6): Circular layout for clear connections
     * - Homogeneous large (>6): Grid layout for space efficiency
     * 
     * SOLID: Strategy Pattern - Different layout strategies based on graph characteristics
     * 
     * @param nodes List of graph nodes to position
     */
    private static void layoutNodes(List<GraphNode> nodes) {
        if (nodes.isEmpty()) return;
        
        // Canvas dimensions with margin for edge visibility
        int canvasWidth = 700;
        int canvasHeight = 500;
        int margin = 80;  // Prevents nodes from being too close to edges
        
        // Calculate usable drawing area
        int minX = margin;
        int maxX = canvasWidth - margin;
        int minY = margin;
        int maxY = canvasHeight - margin;
        
        // Separate nodes by type for bipartite layout
        java.util.List<GraphNode> topics = nodes.stream()
            .filter(n -> "topic".equals(n.type))
            .collect(java.util.stream.Collectors.toList());
        java.util.List<GraphNode> agents = nodes.stream()
            .filter(n -> "agent".equals(n.type))
            .collect(java.util.stream.Collectors.toList());
        
        // Special case: Single node centered
        if (nodes.size() == 1) {
            nodes.get(0).x = canvasWidth / 2;
            nodes.get(0).y = canvasHeight / 2;
            return;
        }
        
        // Bipartite layout for mixed graphs (most common case)
        if (!topics.isEmpty() && !agents.isEmpty()) {
            // Topics on left half - inputs to the computation
            int topicStartX = minX + 20;
            int topicEndX = canvasWidth / 2 - 40;
            layoutNodesInColumns(topics, topicStartX, topicEndX, minY, maxY);
            
            // Agents on right half - processors of the computation
            int agentStartX = canvasWidth / 2 + 40;
            int agentEndX = maxX - 20;
            layoutNodesInColumns(agents, agentStartX, agentEndX, minY, maxY);
        }
        // Homogeneous graphs (only topics or only agents)
        else {
            List<GraphNode> allNodes = topics.isEmpty() ? agents : topics;
            
            if (allNodes.size() <= 6) {
                // Circular layout for small graphs - shows connections clearly
                layoutNodesInMaximalCircle(allNodes, minX, maxX, minY, maxY);
            } else {
                // Grid layout for larger graphs - space efficient
                layoutNodesInGrid(allNodes, minX, maxX, minY, maxY);
            }
        }
    }
    
    /**
     * Arranges nodes in a column-based layout within specified bounds.
     * Optimizes for readability by limiting to max 3 columns and maximizing spacing.
     * 
     * Algorithm:
     * 1. Calculate optimal rows/cols based on node count (max 3 columns)
     * 2. Maximize spacing between nodes
     * 3. Center single rows/columns within their area
     * 
     * @param nodes Nodes to layout
     * @param startX Left boundary
     * @param endX Right boundary  
     * @param minY Top boundary
     * @param maxY Bottom boundary
     */
    private static void layoutNodesInColumns(List<GraphNode> nodes, int startX, int endX, int minY, int maxY) {
        if (nodes.isEmpty()) return;
        
        int usableHeight = maxY - minY;
        int usableWidth = endX - startX;
        
        // Determine grid dimensions
        // Limit to 3 columns max for readability, use sqrt for balanced aspect ratio
        int cols = Math.max(1, Math.min((int)Math.ceil(Math.sqrt(nodes.size())), 3));
        int rows = (int)Math.ceil((double)nodes.size() / cols);
        
        // Calculate spacing to maximize distance between nodes
        int colSpacing = cols > 1 ? usableWidth / (cols - 1) : 0;
        int rowSpacing = rows > 1 ? usableHeight / (rows - 1) : 0;
        
        // Center single column/row within available space
        int startColOffset = cols == 1 ? usableWidth / 2 : 0;
        int startRowOffset = rows == 1 ? usableHeight / 2 : 0;
        
        // Position each node in the grid
        int nodeIndex = 0;
        for (int row = 0; row < rows && nodeIndex < nodes.size(); row++) {
            for (int col = 0; col < cols && nodeIndex < nodes.size(); col++) {
                nodes.get(nodeIndex).x = startX + startColOffset + col * colSpacing;
                nodes.get(nodeIndex).y = minY + startRowOffset + row * rowSpacing;
                nodeIndex++;
            }
        }
    }
    
    /**
     * Arranges nodes in a circular layout for optimal connection visibility.
     * Used for small homogeneous graphs (≤6 nodes) to clearly show all connections.
     * 
     * Algorithm:
     * 1. Find center point of available canvas area
     * 2. Calculate maximum radius that fits within bounds
     * 3. Distribute nodes evenly around circle (360° / node count)
     * 4. Start from top (-π/2) for intuitive reading order
     * 
     * Mathematical approach:
     * - Each node position: (centerX + r*cos(θ), centerY + r*sin(θ))
     * - Angle θ = 2π * i/n - π/2 (where i is node index, n is total nodes)
     * - Ensures equal angular spacing for balanced appearance
     * 
     * @param nodes List of nodes to position
     * @param minX Left boundary
     * @param maxX Right boundary
     * @param minY Top boundary
     * @param maxY Bottom boundary
     */
    private static void layoutNodesInMaximalCircle(List<GraphNode> nodes, int minX, int maxX, int minY, int maxY) {
        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2;
        
        // Calculate maximum possible radius
        int maxRadiusX = (maxX - minX) / 2 - 20;
        int maxRadiusY = (maxY - minY) / 2 - 20;
        int radius = Math.min(maxRadiusX, maxRadiusY);
        
        // Distribute nodes evenly around the circle
        for (int i = 0; i < nodes.size(); i++) {
            double angle = 2 * Math.PI * i / nodes.size() - Math.PI / 2; // Start from top
            nodes.get(i).x = (int)(centerX + radius * Math.cos(angle));
            nodes.get(i).y = (int)(centerY + radius * Math.sin(angle));
        }
    }
    
    /**
     * Arranges nodes in a grid layout for space-efficient display.
     * Used for larger homogeneous graphs (>6 nodes) to prevent overlap.
     * 
     * Algorithm:
     * 1. Calculate optimal grid dimensions using golden ratio approximation
     *    - cols = ceil(sqrt(n * 1.3)) gives slightly wider grids
     *    - This creates more aesthetically pleasing rectangular layouts
     * 2. Maximize spacing between nodes for clarity
     * 3. Fill grid left-to-right, top-to-bottom
     * 
     * Space optimization:
     * - Distributes nodes across entire canvas area
     * - Equal spacing prevents clustering
     * - Rectangular aspect ratio (more columns) matches typical screen dimensions
     * 
     * SOLID: Interface Segregation - This method handles only grid positioning,
     * not node rendering or graph logic.
     * 
     * @param nodes List of nodes to position in grid
     * @param minX Left boundary of drawable area
     * @param maxX Right boundary of drawable area
     * @param minY Top boundary of drawable area
     * @param maxY Bottom boundary of drawable area
     */
    private static void layoutNodesInGrid(List<GraphNode> nodes, int minX, int maxX, int minY, int maxY) {
        int count = nodes.size();
        int cols = (int)Math.ceil(Math.sqrt(count * 1.3)); // Slightly more columns than rows
        int rows = (int)Math.ceil((double)count / cols);
        
        int usableWidth = maxX - minX;
        int usableHeight = maxY - minY;
        
        // Calculate spacing to maximize distance
        int colSpacing = cols > 1 ? usableWidth / (cols - 1) : 0;
        int rowSpacing = rows > 1 ? usableHeight / (rows - 1) : 0;
        
        // If fewer nodes than grid spaces, center the grid
        int startX = minX;
        int startY = minY;
        
        int nodeIndex = 0;
        for (int row = 0; row < rows && nodeIndex < count; row++) {
            for (int col = 0; col < cols && nodeIndex < count; col++) {
                nodes.get(nodeIndex).x = startX + col * colSpacing;
                nodes.get(nodeIndex).y = startY + row * rowSpacing;
                nodeIndex++;
            }
        }
    }
    
    private static String generateGraphDataJson(GraphData data) {
        StringBuilder json = new StringBuilder();
        json.append("{\"nodes\":[");
        
        for (int i = 0; i < data.nodes.size(); i++) {
            if (i > 0) json.append(",");
            GraphNode node = data.nodes.get(i);
            json.append(String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"type\":\"%s\",\"x\":%d,\"y\":%d}",
                escapeJson(node.id), escapeJson(node.name), escapeJson(node.type), node.x, node.y
            ));
        }
        
        json.append("],\"edges\":[");
        
        for (int i = 0; i < data.edges.size(); i++) {
            if (i > 0) json.append(",");
            GraphEdge edge = data.edges.get(i);
            json.append(String.format(
                "{\"from\":\"%s\",\"to\":\"%s\"}",
                escapeJson(edge.from), escapeJson(edge.to)
            ));
        }
        
        json.append("]}");
        return json.toString();
    }
    
    private static String generateGraphInfo(GraphData data) {
        long topicCount = data.nodes.stream().filter(n -> "topic".equals(n.type)).count();
        long agentCount = data.nodes.stream().filter(n -> "agent".equals(n.type)).count();
        
        return String.format(
            "Topics: %d | Agents: %d | Connections: %d<br>" +
            "Layout: Circular | Status: Active",
            topicCount, agentCount, data.edges.size()
        );
    }
    
    private static List<String> generateFallbackHTML(Graph graph, String error) {
        String html = String.format(
            "<!DOCTYPE html><html><head><title>Graph Visualization</title>" +
            "<style>body{font-family:Arial;padding:20px;text-align:center;}" +
            ".error{color:#f44336;background:#ffebee;padding:15px;border-radius:4px;margin:20px 0;}" +
            ".graph-summary{background:#e8f5e8;padding:15px;border-radius:4px;}" +
            "</style></head><body>" +
            "<h2>Computational Graph Loaded</h2>" +
            "<div class='graph-summary'>Graph contains %d nodes</div>" +
            "<div class='error'>Visualization error: %s</div>" +
            "<p>Configuration has been loaded successfully despite visualization issues.</p>" +
            "</body></html>",
            graph.size(), escapeHtml(error)
        );
        return Arrays.asList(html);
    }
    
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // Helper classes for graph data
    private static class GraphData {
        List<GraphNode> nodes = new ArrayList<>();
        List<GraphEdge> edges = new ArrayList<>();
    }
    
    private static class GraphNode {
        String id;
        String name;
        String type;
        int x, y;
    }
    
    private static class GraphEdge {
        String from;
        String to;
    }
}