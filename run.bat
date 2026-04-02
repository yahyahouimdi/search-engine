@echo off
rem Compile et lance le projet.
rem Usage : run.bat [gui^|cli^|test^|jar] [arguments...]
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "MODE=%~1"
if "%MODE%"=="" set "MODE=gui"
set ARGS=%2 %3 %4

if exist build\classes rmdir /s /q build\classes
mkdir build\classes
call :lister src\main\java build\sources.txt
javac -encoding UTF-8 --release 17 -d build\classes @build\sources.txt || exit /b 1

if /i "%MODE%"=="gui" (
    java -cp build\classes moteur.ui.InterfaceRecherche !ARGS!
) else if /i "%MODE%"=="cli" (
    chcp 65001 >nul
    java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp build\classes moteur.cli.Main !ARGS!
) else if /i "%MODE%"=="test" (
    chcp 65001 >nul
    if exist build\test-classes rmdir /s /q build\test-classes
    mkdir build\test-classes
    call :lister src\test\java build\test-sources.txt
    javac -encoding UTF-8 --release 17 -cp build\classes -d build\test-classes @build\test-sources.txt || exit /b 1
    java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "build\classes;build\test-classes" moteur.TestsMoteur
) else if /i "%MODE%"=="jar" (
    jar --create --file build\moteur-de-recherche.jar --main-class moteur.ui.InterfaceRecherche -C build\classes .
    echo Cree : build\moteur-de-recherche.jar  ^(java -jar build\moteur-de-recherche.jar^)
) else (
    echo Usage : run.bat [gui^|cli^|test^|jar]
    exit /b 1
)
exit /b %errorlevel%

rem Ecrit la liste des sources .java (chemins relatifs, entre guillemets) dans un fichier d'arguments javac
:lister
if exist "%~2" del "%~2"
for /r "%~1" %%f in (*.java) do (
    set "chemin=%%f"
    set "chemin=!chemin:%CD%\=!"
    set "chemin=!chemin:\=/!"
    echo "!chemin!">>"%~2"
)
exit /b 0
