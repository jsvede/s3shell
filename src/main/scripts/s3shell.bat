@echo off
if "%1" == "-h" (
  echo Usage %0
  echo Starts the s3sh shell for managing S3 buckets
  GOTO:EOF
)

if "%1" == "--debug" (
  set REMOTE_DEBUGGER="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=38080,suspend=y"
)

set CURRENTDIR=%cd%

set JAVA_OPTIONS=-Xms1024m -Xmx1024m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:PermSize=256m -XX:MaxPermSize=256m

set COMMAND=java %JAVA_OPTIONS% %REMOTE_DEBUGGER% -Dspring.profiles.active=s3shell.h2 -Dfile.encoding=UTF-8 -cp %CURRENTDIR%;%CURRENTDIR%/s3shell-0.6.0-all.jar jds.s3shell.S3Shell

echo %COMMAND%

%COMMAND%