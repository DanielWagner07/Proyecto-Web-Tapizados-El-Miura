@echo off
REM ---------------------------------------------------------------------------
REM  Enciende Tomcat 11 (puerto 8081) SIN recompilar y abre el panel.
REM  Uselo cuando no haya cambiado el codigo, por ejemplo despues de reiniciar
REM  el computador. Si cambio el codigo, use desplegar.bat.
REM  Requisito: MySQL encendido.
REM ---------------------------------------------------------------------------
setlocal
set "TOMCAT_DIR=D:\Apache Tomcat 11"
set "PUERTO=8081"
set "URL_PANEL=http://localhost:%PUERTO%/tapizados-miura/admin/login"

if "%JAVA_HOME%"=="" if exist "C:\Program Files\Java\jdk-25.0.2\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.2"

netstat -ano | findstr /R /C:":%PUERTO% .*LISTENING" /C:":%PUERTO% .*ESCUCHANDO" >nul
if errorlevel 1 (
  echo Iniciando Tomcat 11...
  set "CATALINA_HOME=%TOMCAT_DIR%"
  call "%TOMCAT_DIR%\bin\startup.bat"
  timeout /t 10 /nobreak >nul
) else (
  echo Tomcat 11 ya estaba encendido.
)

start "" "%URL_PANEL%"
