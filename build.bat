@echo off
echo Building CineSphere...
if not exist bin mkdir bin
dir /s /B src\*.java > sources.txt
javac -d bin --module-path lib --add-modules javafx.controls,javafx.fxml -cp "lib/*" @sources.txt
if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b %errorlevel%
)
echo Copying FXML files...
xcopy src\views bin\views /E /I /Y >nul 2>&1
echo Compilation successful.
