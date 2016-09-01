#!/bin/bash

SCIARETO_HOME="$(dirname ${BASH_SOURCE[0]})"
SCIARETO_PLUGINS=$SCIARETO_HOME/plugins
JAVA_RUN=java

if [ -f $SCIARETO_HOME/.pid ];
then
    SAVED_PID=$(cat $SCIARETO_HOME/.pid)
    if [ -f /proc/$SAVED_PID/exe ];
    then
        echo Editor already started! if it is wrong, just delete the .pid file in the editor folder root!
	exit 1
    fi
fi    

$JAVA_RUN -Dnbmmd.plugin.folder=$SCIARETO_PLUGINS -jar $SCIARETO_HOME/sciareto.jar $@ &> $SCIARETO_HOME/console.log&
THE_PID=$!
echo $THE_PID>$SCIARETO_HOME/.pid
wait $THE_PID
rm $SCIARETO_HOME/.pid
exit 0

