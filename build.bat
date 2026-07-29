@echo off

echo Cleaning...
if exist out rmdir /s /q out
if exist aegise.jar del /f /q aegise.jar

echo Creating output directory...
mkdir out

echo Collecting source files...
if exist sources.txt del /f /q sources.txt
for /R src %%f in (*.java) do echo %%f>>sources.txt

echo Compiling...
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 goto error

echo Packaging...
jar cfe aegise.jar com.aegis.editor.Main -C out .
if errorlevel 1 goto error

del sources.txt

echo.
echo Build successful!
echo Output: aegise.jar
goto end

:error
echo.
echo Build failed!
if exist sources.txt del /f /q sources.txt
exit /b 1

:end
pause