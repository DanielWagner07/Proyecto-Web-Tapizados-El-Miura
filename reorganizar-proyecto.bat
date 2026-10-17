@echo off
REM ---------------------------------------------------------------------------
REM  Mueve el frontend (HTML, css, js, img) a src\main\webapp, que es la
REM  carpeta que Maven empaqueta en el WAR. Usa "git mv" para conservar el
REM  historial de cada archivo. Se ejecuta UNA sola vez.
REM ---------------------------------------------------------------------------
setlocal
cd /d "%~dp0"

if not exist "src\main\webapp" mkdir "src\main\webapp"

for %%f in (*.html) do call :mover "%%f" "src\main\webapp\%%~nxf"

for %%d in (css js img) do (
  if exist "%%d" (
    if not exist "src\main\webapp\%%d" mkdir "src\main\webapp\%%d"
    for %%f in ("%%d\*") do call :mover "%%f" "src\main\webapp\%%d\%%~nxf"
  )
)

REM Quita las carpetas viejas si quedaron vacias
for %%d in (css js img) do rd "%%d" 2>nul

echo.
echo Listo. El frontend ahora esta en src\main\webapp.
pause
exit /b 0

:mover
git mv %1 %2 2>nul || move /Y %1 %2 >nul
exit /b 0
