@echo off
echo === Generating Clean Javadoc Documentation ===
echo.

REM Create docs directory if it doesn't exist
if not exist docs mkdir docs

REM Generate Javadoc with only properly documented files
echo Generating documentation...
javadoc -d docs ^
    -windowtitle "Advanced Programming Exercise 6 - Computational Graph" ^
    -doctitle "Computational Graph Web Application API Documentation" ^
    -header "Exercise 6 API" ^
    -author ^
    -version ^
    -use ^
    -splitindex ^
    src/graph/Agent.java ^
    src/graph/Message.java ^
    src/graph/ParallelAgent.java ^
    src/graph/Topic.java ^
    src/graph/TopicManagerSingleton.java ^
    src/configs/Config.java ^
    src/configs/GenericConfig.java ^
    src/configs/Graph.java ^
    src/configs/Node.java ^
    src/server/HTTPServer.java ^
    src/server/MyHTTPServer.java ^
    src/servlets/Servlet.java ^
    src/views/HtmlGraphWriter.java

if %errorlevel% neq 0 (
    echo Javadoc generation failed!
    pause
    exit /b %errorlevel%
)

echo.
echo === Documentation generated successfully! ===
echo Open docs/index.html to view the documentation
echo.
pause