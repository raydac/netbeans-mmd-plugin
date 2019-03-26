set "SCIARETO_HOME=%cd%"
set "LOG_FILE=%SCIARETO_HOME%/console.log"

rem uncomment the line below if graphics works slowly
rem set "JAVA_EXTRA_GFX_FLAGS=-Dsun.java2d.opengl=true"

set "JAVA_FLAGS=-client -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED -Dsun.java2d.dpiaware=true -Dswing.aatext=true -Dawt.useSystemAAFontSettings=on"
set "JAVA_RUN=java.exe"

echo %%JAVA_RUN%%=%JAVA_RUN% > %LOG_FILE%

echo ------JAVA_VERSION------ >> %LOG_FILE%

%JAVA_RUN% -version 2>> %LOG_FILE%

echo ------------------------ >> %LOG_FILE%

%SCIARETO_HOME%\jre\bin\%JAVA_RUN% %JAVA_FLAGS% %JAVA_EXTRA_GFX_FLAGS% -jar %SCIARETO_HOME%\scia-reto.jar %* 2>> %LOG_FILE%
