#!/bin/bash

# Script just generates free desktop descriptor to start application

APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET="$APP_HOME/scia-reto-editor.desktop"

{
  echo "[Desktop Entry]"
  echo "Version=1.1"
  echo "Type=Application"
  echo "Name=Scia-Reto editor"
  echo "GenericName=Scia-Reto mind-map editor"
  echo "Comment=Free open source mind-map editor with PlantUML support"
  echo "Icon=$APP_HOME/icon.svg"
  echo "Exec=\"$APP_HOME/run.sh\" %f"
  echo "Categories=Office;FlowChart;"
  echo "Keywords=mind;map;scia;reto;uml;plant;kstream;kafka;"
  echo "Terminal=false"
  echo "StartupWMClass=scia.reto.editor"
} >"$TARGET"

echo "Desktop script has been generated: $TARGET"

if [ -d "$HOME/.gnome/apps" ]; then
  echo "copy to ~/.gnome/apps"
  cp -f "$TARGET" "$HOME/.gnome/apps"
fi

if [ -d "$HOME/Desktop" ]; then
  echo "copy to ~/Desktop"
  cp -f "$TARGET" "$HOME/Desktop"
fi

if [ -d "$HOME/.local/share/applications" ]; then
  echo "copy to ~/.local/share/applications"
  cp -f "$TARGET" "$HOME/.local/share/applications"
fi
