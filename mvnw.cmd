@echo off
REM ---------------------------------------------------------------------------
REM  Lanzador de Maven para Windows.
REM  Si Maven no esta instalado, lo descarga una sola vez en
REM  %USERPROFILE%\.m2\wrapper y luego lo ejecuta con los argumentos recibidos.
REM  Requisito: tener un JDK 17 o superior (java en el PATH o JAVA_HOME).
REM
REM  Ejemplos:  mvnw.cmd clean package      mvnw.cmd test
REM ---------------------------------------------------------------------------
setlocal
set "MAVEN_VERSION=3.9.11"
set "WRAPPER_DIR=%USERPROFILE%\.m2\wrapper"
set "MAVEN_LOCAL=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%"

if exist "%MAVEN_LOCAL%\bin\mvn.cmd" goto ejecutar

echo Descargando Maven %MAVEN_VERSION%, solo ocurre la primera vez...
if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference = 'Stop';" ^
  "$url = 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip';" ^
  "$zip = Join-Path $env:TEMP 'apache-maven-%MAVEN_VERSION%.zip';" ^
  "Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing;" ^
  "Expand-Archive -Path $zip -DestinationPath $env:USERPROFILE\.m2\wrapper -Force;" ^
  "Remove-Item $zip"
if errorlevel 1 (
  echo No fue posible descargar Maven. Revisa tu conexion a internet.
  exit /b 1
)

:ejecutar
call "%MAVEN_LOCAL%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
