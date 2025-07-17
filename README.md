# Advanced Programming - Exercise 6: Computational Graph Web Application

## Project Overview

This project implements a sophisticated web-based computational graph system that visualizes and manages data flow between computational agents and topics. Built as the culmination of the Advanced Programming course, it combines publish-subscribe architecture, HTTP server implementation, and interactive web visualization.

### Key Features

- **Real-time Graph Visualization**: Interactive display of computational graphs with automatic layout algorithms
- **Dynamic Agent Management**: Support for various agent types (PlusAgent, IncAgent, ParallelAgent)
- **Web-based Control Panel**: Upload configurations, publish messages, and monitor topics
- **Cycle Detection**: Prevents invalid configurations with circular dependencies
- **Thread-safe Operations**: Concurrent message processing with ParallelAgent wrapper
- **RESTful API**: HTTP server with servlet-based request handling

## Background

This system builds upon fundamental concepts from earlier exercises:
- Exercise 1-2: Basic publish-subscribe architecture with Topics and Agents
- Exercise 3-4: Graph representation and cycle detection algorithms
- Exercise 5: HTTP server implementation with servlet pattern
- Exercise 6: Web UI integration and visualization

The project demonstrates proficiency in:
- Design patterns (Singleton, Decorator, Factory, Observer)
- SOLID principles throughout the architecture
- Multi-threaded programming and synchronization
- Graph algorithms and data structures
- Web development (HTML5, JavaScript, Canvas API)

## Installation

### Prerequisites

- Java JDK 8 or higher
- Terminal/Command Prompt access
- Web browser (Chrome, Firefox, Safari, or Edge)

### Setup Steps

1. **Clone or Download the Project**
   ```bash
   git clone <repository-url>
   cd zzr3
   ```

2. **Verify Project Structure**
   Ensure you have the following directories:
   - `src/` - Contains all Java source files
   - `files_html/` - Contains HTML templates
   - `test_conf/` - Contains example configuration files for testing

3. **Compile and Run the Project**

   **Option A - Using provided batch file (Windows):**
   ```bash
   run_ex6.bat
   ```
   This will compile and automatically start the server.

   **Option B - Manual steps (All platforms):**
   
   Step 1: Compile all Java files
   ```bash
   javac -d . src/graph/*.java src/configs/*.java src/server/*.java src/servlets/*.java src/views/*.java src/*.java
   ```
   
   If successful, you should see no output and new .class files will be created in the current directory.
   
   Step 2: Run the server
   ```bash
   java -cp . Main
   ```
   
   You should see:
   ```
   Server started on port 8080
   Open http://localhost:8080 in your browser
   Press Enter to stop the server...
   ```
   
   The server is now running! Keep this terminal open.

## Using the Application

Once the server is running (either from the batch file or manual steps), you can access it:

### 1. Open the Web Interface

Navigate to:
```
http://localhost:8080
```

### 2. Stop the Server

Press `Ctrl+C` in the terminal/command prompt where the server is running.

### 3. Quick Start Test

After starting the server, test that everything works:

1. Open http://localhost:8080
2. Click "Choose File" and navigate to `test_conf/` folder
3. Select `test_config.conf`
4. Click "Upload Config"
5. In "Topic Name" enter: `A`
6. In "Value" enter: `5`
7. Click "Publish"
8. You should see topic `B` with value `6.0`

### 4. Using the Application

#### Upload a Configuration
1. Click "Choose File" in the Control Panel
2. Navigate to the `test_conf/` folder
3. Select a `.conf` file:
   - `simple_no_cycle.conf` - Basic configuration without cycles
   - `test_config.conf` - Simple test configuration  
   - `complex_math.conf` - Complex mathematical operations
   - `multi_agent_test.conf` - Multiple agents demonstration
   - Additional test files for various scenarios

#### Publish Messages
1. Enter a topic name (e.g., "A", "B", "C")
2. Enter a numeric value
3. Click "Publish"
4. Watch the computational graph update in real-time

#### Monitor Topics
- View all active topics and their current values in the Topics Monitor
- See which agents are publishing and subscribing to each topic

#### Reset System
- Use "Clear Graph" to remove the current configuration
- Use "Reset Topics" to clear all message values

## Test Configurations

The `test_conf/` folder contains various configuration files for testing different aspects of the system:

- **simple_no_cycle.conf** - Basic increment chain (A → B → C)
- **test_config.conf** - Minimal configuration for quick testing
- **complex_math.conf** - Demonstrates multiple PlusAgent operations
- **multi_agent_test.conf** - Shows multiple agents working together
- **new_test_config.conf** - Additional test scenarios

These files are ready to use and demonstrate various features of the computational graph system.

## Configuration File Format

Configuration files use a simple 3-line format per agent:
```
<AgentClassName>
<InputTopic1,InputTopic2,...>
<OutputTopic>
```

Example (`simple_no_cycle.conf`):
```
configs.IncAgent
A
B
configs.IncAgent
B
C
```

## Architecture Overview

### Core Components

1. **Graph Package** (`src/graph/`)
   - `Message`: Immutable data carrier
   - `Topic`: Publish-subscribe channel
   - `Agent`: Interface for computational nodes
   - `TopicManagerSingleton`: Global topic registry
   - `ParallelAgent`: Decorator for async processing

2. **Configuration Package** (`src/configs/`)
   - `GenericConfig`: Dynamic agent loader using reflection
   - `Graph`: Cycle detection implementation
   - `Node`: Graph node representation
   - Various Agent implementations (PlusAgent, IncAgent)

3. **Server Package** (`src/server/`)
   - `MyHTTPServer`: Multi-threaded HTTP server
   - `RequestParser`: HTTP request parsing

4. **Servlets Package** (`src/servlets/`)
   - `TopicDisplayer`: Message publishing and topic viewing
   - `FileUploadServlet`: Configuration file handling
   - `ClearGraphServlet`: System reset functionality

5. **Views Package** (`src/views/`)
   - `HtmlGraphWriter`: Graph to HTML conversion with layout algorithms

## Advanced Features

### Cycle Detection Algorithm
- Uses Depth-First Search (DFS) with white-gray-black coloring
- O(V + E) time complexity
- Prevents infinite loops in computational graphs

### Layout Algorithms
- **Bipartite Layout**: Topics on left, agents on right
- **Circular Layout**: For small homogeneous graphs (≤6 nodes)
- **Grid Layout**: For larger homogeneous graphs (>6 nodes)

### Thread Safety
- `ConcurrentHashMap` for servlet registry
- Thread pool for concurrent request handling
- `ParallelAgent` with blocking queue for async processing

## Troubleshooting

### Compilation Issues

1. **"javac: command not found"**
   - Ensure Java JDK is installed: `java -version`
   - Add Java to PATH environment variable
   - On Windows: Use `set PATH=%PATH%;C:\Program Files\Java\jdk-XX\bin`
   - On Linux/Mac: Use `export PATH=$PATH:/usr/lib/jvm/java-XX/bin`

2. **"cannot find symbol" errors**
   - Ensure you're in the project root directory (zzr3)
   - Compile all packages together, not individual files
   - Use the exact compilation command provided

3. **"package does not exist" errors**
   - Make sure to compile files in the correct order
   - Use: `javac -d . src/**/*.java` (not individual files)

### Runtime Issues

1. **Port Already in Use**
   ```bash
   # Find process using port 8080
   # Windows: netstat -ano | findstr :8080
   # Linux/Mac: lsof -i :8080
   ```
   - Change the port in `Main.java`
   - Or kill the process using the port

2. **"Could not find or load main class Main"**
   - Ensure you're running from the project root
   - Check that compilation was successful
   - Use: `java -cp . Main` (note the dot after -cp)

3. **"NoClassDefFoundError"**
   - Classes not compiled to correct location
   - Re-compile using: `javac -d . src/**/*.java`
   - Ensure classpath is set: `java -cp . Main`

### Application Issues

1. **Graph Not Displaying**
   - Verify the configuration file format (3 lines per agent)
   - Check browser console (F12) for JavaScript errors
   - Ensure no cycles in the configuration
   - Try a simple config file first (test_config.conf)

2. **Topics Not Updating**
   - Verify topic names match exactly (case-sensitive)
   - Check that agents are properly connected
   - Use Reset Topics if values seem stuck
   - Ensure numeric values are entered for publishing

3. **File Upload Not Working**
   - Check file extension is .conf
   - Verify file format (must be divisible by 3 lines)
   - Try with provided example files first

## Testing

Run the provided test configurations to verify functionality:

```bash
# Test basic increment chain
# Upload simple_no_cycle.conf
# Publish to topic "A" with value 10
# Expected: B=11, C=12

# Test mathematical operations
# Upload complex_math.conf
# Publish to topics A=1, B=2, C=3, D=4
# Expected: Sum1=3, Sum2=7, Total=10, Final=22
```

## Development Notes

### Adding New Agent Types

1. Implement the `Agent` interface
2. Add constructor: `(String[] inputs, String[] outputs)`
3. Register topics in constructor
4. Implement `callback()` for message processing
5. Add to configuration file with full class name

### SOLID Principles Demonstrated

- **Single Responsibility**: Each class has one clear purpose
- **Open/Closed**: New agents added without modifying core
- **Liskov Substitution**: All agents interchangeable
- **Interface Segregation**: Clean, minimal interfaces
- **Dependency Inversion**: Depend on abstractions

## Documentation

### Generating Javadoc

The project includes comprehensive Javadoc comments. To generate the HTML documentation:

**Windows:**
```bash
generate_javadoc.bat
```

**Unix/Linux/Mac:**
```bash
chmod +x generate_javadoc.sh
./generate_javadoc.sh
```

**Manual generation:**
```bash
mkdir docs
javadoc -d docs -author -version -use -splitindex -windowtitle "Exercise 6 API" src/**/*.java
```

After generation, open `docs/index.html` in your browser to view the API documentation.

## Credits

Developed for the Advanced Programming course
Exercise 6 - Final Project