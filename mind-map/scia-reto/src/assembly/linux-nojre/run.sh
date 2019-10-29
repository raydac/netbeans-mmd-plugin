#!/bin/bash

SCIARETO_HOME="$(dirname ${BASH_SOURCE[0]})"
LOG_FILE=$SCIARETO_HOME/console.log

# uncomment the line below if graphics works slowly
# JAVA_EXTRA_GFX_FLAGS="-Dsun.java2d.opengl=true"

JAVA_FLAGS="-client -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED -Dsun.java2d.dpiaware=true -Dswing.aatext=true -Dawt.useSystemAAFontSettings=on"

if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo detected JAVA_HOME variable
    JAVA_RUN="$JAVA_HOME/bin/java"
elif type -p java; then
    echo detected Java in PATH
    JAVA_RUN=java

    $JAVA_RUN -version
    retVal=$?
    if [ $retVal -ne 0 ]; then
	echo Problems with start $JAVA_RUN
	exit $retVal
    fi
else
    echo "can't find any installed java"
exit 1
fi

JAVA_RUN=java

echo \$JAVA_RUN=$JAVA_RUN &>$LOG_FILE

echo ------JAVA_VERSION------ &>>$LOG_FILE

$JAVA_RUN -version &>>$LOG_FILE

echo ------------------------ &>>$LOG_FILE

$JAVA_RUN $JAVA_FLAGS $JAVA_EXTRA_GFX_FLAGS -jar $SCIARETO_HOME/scia-reto.jar $@ &>>$LOG_FILE&
