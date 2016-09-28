#!/bin/bash

SCIARETO_HOME="$(dirname ${BASH_SOURCE[0]})"
LOG_FILE=$SCIARETO_HOME/console.log
SCIARETO_PLUGINS=$SCIARETO_HOME/plugins

# JAVA_EXTRA_GFX_FLAGS="-Dsun.java2d.opengl=true"

JAVA_FLAGS="-client -Dsun.java2d.dpiaware=true -Dswing.aatext=true -Dawt.useSystemAAFontSettings=on"

if [ -z $JAVA_HOME ]; then
    echo \$JAVA_HOME is undefined &>$LOG_FILE
    JAVA_RUN=java
else
    echo Detected \$JAVA_HOME : $JAVA_HOME &>$LOG_FILE
    JAVA_RUN=$JAVA_HOME/bin/java
fi

if [ -f $SCIARETO_HOME/.pid ];
then
    SAVED_PID=$(cat $SCIARETO_HOME/.pid)
    if [ -f /proc/$SAVED_PID/exe ];
    then
        echo Editor already started! if it is wrong, just delete the .pid file in the editor folder root!
	exit 1
    fi
fi    

echo \$JAVA_RUN=$JAVA_RUN &>>$LOG_FILE

$JAVA_RUN $JAVA_FLAGS $JAVA_EXTRA_GFX_FLAGS -Dnbmmd.plugin.folder=$SCIARETO_PLUGINS -jar $SCIARETO_HOME/sciareto.jar $@ &>> $SCIARETO_HOME/console.log&
THE_PID=$!
echo $THE_PID>$SCIARETO_HOME/.pid
wait $THE_PID
rm $SCIARETO_HOME/.pid
exit 0

