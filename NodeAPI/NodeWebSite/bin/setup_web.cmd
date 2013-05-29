@echo on

if exist "success.txt" (
  echo Application installation was already successful. No need to rerun. Exiting.
  exit /b 0
)

cd /d "%~dp0"

if "%EMULATED%"=="true" if DEFINED APPCMD goto emulator_setup
if "%EMULATED%"== "true" exit /b 0

echo Granting permissions for Network Service to the web root directory...
icacls ..\ /grant "Network Service":(OI)(CI)W
if %ERRORLEVEL% neq 0 goto error
echo OK

echo Downloading tools from http://hfapi.blob.core.windows.net/deployment-files
curl -O http://hfapi.blob.core.windows.net/deployment-files/7z.exe
if %ERRORLEVEL% neq 0 goto error
curl -O http://hfapi.blob.core.windows.net/deployment-files/7z.dll
if %ERRORLEVEL% neq 0 goto error
curl -O http://hfapi.blob.core.windows.net/deployment-files/gitinstall.exe
if %ERRORLEVEL% neq 0 goto error
curl -O http://hfapi.blob.core.windows.net/deployment-files/iisnode.msi
if %ERRORLEVEL% neq 0 goto error
curl -O http://hfapi.blob.core.windows.net/deployment-files/node-v0.10.8-x64.msi
if %ERRORLEVEL% neq 0 goto error
curl -O http://hfapi.blob.core.windows.net/deployment-files/vcredist_x64.exe
if %ERRORLEVEL% neq 0 goto error
echo OK

echo Installing Visual Studio 2010 C++ Redistributable Package...
vcredist_x64.exe /q
if %ERRORLEVEL% neq 0 goto error
echo OK

echo Installing Node.js
msiexec.exe /quiet /i node-v0.10.8-x64.msi
if %ERRORLEVEL% neq 0 goto error
echo OK

echo Installing Git
gitinstall.exe /verysilent /nocancel /suppressmsgboxes
timeout 10
if %ERRORLEVEL% neq 0 goto error
echo OK

echo Cloning hfgame
"%ProgramFiles(x86)%\Git\bin\git.exe" clone https://{{GITUSER}}:{{GITPASSWORD}}@github.com/robbear/hfgame.git -b {{GITBRANCH}}
if %ERRORLEVEL% neq 0 goto error
echo OK

echo Building version.txt file
cd hfgame\NodeAPI
"%ProgramFiles(x86)%\Git\bin\git.exe" rev-parse HEAD > NodeWebSite\version.txt
set /p versionstring=<NodeWebSite\version.txt
if %ERRORLEVEL% neq 0 goto error
cd ..\..

echo Building timestamp.txt file
echo %date:~-4,4%%date:~-10,2%%date:~-7,2%%time:~0,2%%time:~3,2%%time:~6,2%| "%ProgramFiles(x86)%\Git\bin\sed.exe" 's/ /0/g' > hfgame\NodeAPI\NodeWebSite\timestamp.txt
set /p timestamp=<hfgame\NodeAPI\NodeWebSite\timestamp.txt
if %ERRORLEVEL% neq 0 goto error

REM echo Replacing staticfiles path string with versionstring
REM "%ProgramFiles(x86)%\Git\bin\find.exe" ./hfgame/NodeAPI/NodeWebSite/views -name "*.html" -exec "%ProgramFiles(x86)%\Git\bin\sed.exe" -i "s/staticfiles/%versionstring%/g" '{}' ;
REM if %ERRORLEVEL% neq 0 goto error

REM echo Renaming staticfiles directory to %versionstring%
REM ren hfgame\NodeAPI\NodeWebSite\public\staticfiles %versionstring%
REM if %ERRORLEVEL% neq 0 goto error

echo Copying NodeWebSite to approot
xcopy hfgame\NodeAPI\NodeWebSite ..\ /c /e /y /EXCLUDE:exclude.txt
md ..\models
xcopy hfgame\models ..\models /c /e
copy /y ..\Web.cloud.config ..\Web.config

echo Running npm install
cd ..
echo npm LOG > npmlog.txt
call npm.cmd install 1>> npmlog.txt 2>> npmlog_error.txt
cd bin
echo OK

echo Installing iisnode...
msiexec.exe /quiet /i iisnode.msi
if %ERRORLEVEL% neq 0 goto error
echo OK

echo Removing repository
rd /s /q hfgame

echo Application installation successful > "success.txt"
echo SUCCESS
exit /b 0

:error

echo FAILED
exit /b -1

:emulator_setup
echo Running in emulator adding iisnode to application host config
FOR /F "tokens=1,2 delims=/" %%a in ("%APPCMD%") DO set FN=%%a&set OPN=%%b
if "%OPN%"=="%OPN:apphostconfig:=%" (
    echo "Could not parse appcmd '%appcmd% for configuration file, exiting"
    goto error
)

set IISNODE_BINARY_DIRECTORY=%programfiles%\Microsoft SDKs\Windows Azure\PowerShell\Azure\x86
if "%PROCESSOR_ARCHITECTURE%"=="AMD64" set IISNODE_BINARY_DIRECTORY=%programfiles(x86)%\Microsoft SDKs\Windows Azure\PowerShell\Azure\x64

echo "Using iisnode binaries location '%IISNODE_BINARY_DIRECTORY%'"
echo installing iisnode module using AppCMD alias %appcmd%
%appcmd% install module /name:"iisnode" /image:"%IISNODE_BINARY_DIRECTORY%\iisnode.dll"

set apphostconfigfile=%OPN:apphostconfig:=%
powershell -c "set-executionpolicy unrestricted"
powershell .\ChangeConfig.ps1 %apphostconfigfile%
if %ERRORLEVEL% neq 0 goto error

if "%PROCESSOR_ARCHITECTURE%"=="AMD64" set
copy /y "%IISNODE_BINARY_DIRECTORY%\iisnode_schema.xml" "%programfiles%\IIS Express\config\schema\iisnode_schema.xml"
if %ERRORLEVEL% neq 0 goto error
exit /b 0
