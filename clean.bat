@echo off
echo === Cleaning Project ===
echo.

echo Removing all .class files...
for /r %%i in (*.class) do (
    del "%%i" 2>nul
)

echo Removing docs folder...
if exist docs rmdir /s /q docs

echo.
echo === Cleanup Complete ===
pause