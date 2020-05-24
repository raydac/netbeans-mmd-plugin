#!/bin/bash

# Script just generates free desktop descriptor to start application

APP_HOME="$(realpath $(dirname ${BASH_SOURCE[0]}))"
TARGET=$APP_HOME/scia-reto-editor.desktop

echo [Desktop Entry] > $TARGET
echo Encoding=UTF-8 >> $TARGET
echo Version=1.4.9 >> $TARGET
echo Type=Application >> $TARGET
echo Name=SciaReto >> $TARGET
echo GenericName=SciaReto mind-map editor >> $TARGET
echo Icon="$APP_HOME/icon.svg" >> $TARGET
echo Exec="$APP_HOME/run.sh" %f >> $TARGET
echo Comment=Free mind-map editor with PlantUML support >> $TARGET
echo Categories=Development;Editor; >> $TARGET
echo Terminal=false >> $TARGET
echo StartupWMClass=scia-reto-mind-map-editor >> $TARGET
echo OnlyShowIn=Old; >> $TARGET
echo StartupNotify=true >> $TARGET

echo Desktop script has been generated: $TARGET

if [ -d ~/.gnome/apps ]; then
    echo copy to ~/.gnome/apps
    cp -f $TARGET ~/.gnome/apps
fi

if [ -d ~/.local/share/applications ]; then
    echo copy to ~/.local/share/applications
    cp -f $TARGET ~/.local/share/applications
fi

if [ -d ~/Desktop ]; then
    echo copy to ~/Desktop
    cp -f $TARGET ~/Desktop
fi

