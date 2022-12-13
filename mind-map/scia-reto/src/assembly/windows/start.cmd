set "SCIARETO_HOME=%cd%"

rem uncomment the line below if graphics works slowly
rem set "JAVA_EXTRA_GFX_FLAGS=-Dsun.java2d.opengl=true"

set "JAVA_FLAGS=-client -XX:+IgnoreUnrecognizedVMOptions -Xmx2G --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED -Dsun.java2d.dpiaware=true -Dswing.aatext=true -Dawt.useSystemAAFontSettings=on"
set "JAVA_RUN=javaw.exe"
set "JAVA_LOG=-Djava.util.logging.config.file=%SCIARETO_HOME%\logger.properties"

start "SciaReto" "%SCIARETO_HOME%\jre\bin\%JAVA_RUN%" %JAVA_FLAGS% %JAVA_EXTRA_GFX_FLAGS% %JAVA_LOG% -jar "%SCIARETO_HOME%\scia-reto.jar" %*
