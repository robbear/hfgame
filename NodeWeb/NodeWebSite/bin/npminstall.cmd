@echo on

echo npm LOG > npmlog.txt
cd ..
"%programfiles%\nodejs\npm" install 1>> npmlog.txt 2>> npmlog_error.txt
cd bin

exit /b 0


