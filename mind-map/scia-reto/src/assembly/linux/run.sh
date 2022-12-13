#!/bin/bash

SCIARETO_HOME="$(dirname ${BASH_SOURCE[0]})"
SR_JAVA_HOME=$SCIARETO_HOME/jre

# uncomment the line below if graphics works slowly
# JAVA_EXTRA_GFX_FLAGS="-Dsun.java2d.opengl=true"

JAVA_FLAGS="-client -XX:+IgnoreUnrecognizedVMOptions -Xmx2G --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED -Dsun.java2d.dpiaware=true -Dswing.aatext=true -Dawt.useSystemAAFontSettings=on"
JAVA_LOG_CONFIG=$SCIARETO_HOME/logger.properties
JAVA_RUN=$SR_JAVA_HOME/bin/java

$JAVA_RUN $JAVA_FLAGS $JAVA_EXTRA_GFX_FLAGS "-Djava.util.logging.config.file=$JAVA_LOG_CONFIG" -jar $SCIARETO_HOME/scia-reto.jar $@
