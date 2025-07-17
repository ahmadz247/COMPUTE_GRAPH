@echo off
echo === Exercise 6 - Web Application ===
echo.

REM Compile all source files
echo Compiling all source files...
javac -d . src/graph/*.java src/configs/*.java src/server/*.java src/servlets/*.java src/views/*.java src/*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)



REM Run the main application
java -cp . Main


pause