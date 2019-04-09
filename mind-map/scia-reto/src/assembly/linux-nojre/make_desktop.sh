#!/bin/bash

# Script just generates free desktop descriptor to start application

SCIARETO_HOME="$(realpath $(dirname ${BASH_SOURCE[0]}))"
TARGET=$SCIARETO_HOME/sciareto.desktop

echo [Desktop Entry] > $TARGET
echo Encoding=UTF-8 >> $TARGET
echo Name=Scia Reto >> $TARGET
echo Comment=Free mind map editor >> $TARGET
echo GenericName=Free mind map editor >> $TARGET
echo Exec=$SCIARETO_HOME/run.sh >> $TARGET
echo Terminal=false >> $TARGET
echo Type=Application >> $TARGET
echo Icon=$SCIARETO_HOME/icon.png >> $TARGET
echo Categories=Application; >> $TARGET
echo StartupWMClass=Scia Reto editor >> $TARGET
echo StartupNotify=true >> $TARGET


chmod +x $TARGET

echo Desktop script has been generated: $TARGET
