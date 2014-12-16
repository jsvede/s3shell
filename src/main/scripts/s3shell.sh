#!/bin/bash

if [ $1 == -h ];
then
    echo Usage $0
    echo   Starts the s3sh shell for managing S3 buckets
    exit;
fi


if [ "$1" == "--debug" ]
then
    REMOTE_DEBUGGER="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=38080,suspend=y"
fi


export JAVA_OPTIONS="-Xms1024m -Xmx1024m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:PermSize=256m -XX:MaxPermSize=256m"

COMMAND="java $JAVA_OPTIONS $REMOTE_DEBUGGER -Dspring.profiles.active=s3shell.h2 -Dfile.encoding=UTF-8 -cp $PWD:$PWD/s3shell-1.0.0-SNAPSHOT-all.jar jds.s3shell.S3Shell"

$COMMAND
