package main

import (
	"fmt"
	"log"
	"os"
	"os/exec"
	"path/filepath"
)

//go:generate goversioninfo -gofile=versioninfo.go

const JDK_PATH = "jre\\bin\\javaw.exe"
const JAR_FILE = "scia-reto.jar"

func main() {
	fmt.Printf("%v %v\n%v\n", versionInfo.StringFileInfo.ProductName, versionInfo.StringFileInfo.ProductVersion, versionInfo.StringFileInfo.LegalCopyright)
	path, err := os.Executable()
	if err == nil {
		base_folder := filepath.Dir(path)
		cmd := exec.Command(base_folder+"\\"+JDK_PATH, "-Xmx2G", "-Dsun.java2d.opengl=true", "-jar", base_folder+"\\"+JAR_FILE)
		err = cmd.Start()
		if err != nil {
			log.Fatal(err)
		}
	} else {
		log.Fatal(err)
	}
}
