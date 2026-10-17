@echo off
REM ---------------------------------------------------------------------------
REM  Compila el proyecto, ejecuta las pruebas, copia el WAR a Tomcat 11,
REM  inicia Tomcat si hace falta y abre el panel en el navegador.
REM
REM  Antes de usarlo:
REM    - Inicia MySQL.
REM    - Tomcat 11 escucha en el puerto 8081 para no chocar con el Tomcat 8.5
REM      de XAMPP, que usa el 8080 y NO sirve para este proyecto.
REM ---------------------------------------------------------------------------
setlocal
set "TOMCAT_DIR=D:\Apache Tomcat 11"
set "PUERTO=8081"
set "URL_PANEL=http://localhost:%PUERTO%/tapizados-miura/admin/login"

REM Tomcat y Maven necesitan JAVA_HOME; si no esta definido se usa el JDK instalado
if "%JAVA_HOME%"=="" if exist "C:\Program Files\Java\jdk-25.0.2\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.2"

cd /d "%~dp0"
echo Compilando y ejecutando pruebas...
call mvnw.cmd -q clean package
if errorlevel 1 (
  echo.
  echo La compilacion o las pruebas fallaron. Revisa los mensajes de arriba.
  pause
  exit /b 1
)

if not exist "%TOMCAT_DIR%\webapps" (
  echo No se encontro Tomcat en "%TOMCAT_DIR%". Edita TOMCAT_DIR en este archivo.
  pause
  exit /b 1
)

copy /Y "target\tapizados-miura.war" "%TOMCAT_DIR%\webapps\" >nul
echo WAR copiado a "%TOMCAT_DIR%\webapps".

REM Si Tomcat 11 no esta escuchando en el puerto, se inicia
netstat -ano | findstr /R /C:":%PUERTO% .*LISTENING" /C:":%PUERTO% .*ESCUCHANDO" >nul
if errorlevel 1 (
  echo Iniciando Tomcat 11...
  set "CATALINA_HOME=%TOMCAT_DIR%"
  call "%TOMCAT_DIR%\bin\startup.bat"
)

echo Esperando a que Tomcat despliegue la aplicacion...
timeout /t 12 /nobreak >nul
start "" "%URL_PANEL%"

echo.
echo Sitio publico : http://localhost:%PUERTO%/tapizados-miura/
echo Panel admin   : %URL_PANEL%
pause
