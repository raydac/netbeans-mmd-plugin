#!/bin/bash

SCIARETO_HOME="$(dirname ${BASH_SOURCE[0]})"
SCIARETO_PLUGINS=$SCIARETO_HOME/plugins
JAVA_RUN=java

startedNum="$(pgrep sciareto.sh | wc -l)"

if [ "$startedNum" -lt "3" ]
then
    $JAVA_RUN -Dnbmmd.plugin.folder=$SCIARETO_PLUGINS -jar $SCIARETO_HOME/sciareto.jar $@ &> $SCIARETO_HOME/console.log
else
    echo "Script has been started already!"
fi