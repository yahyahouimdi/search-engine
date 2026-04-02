#!/usr/bin/env sh
# Compile et lance le projet.
# Usage : ./run.sh [gui|cli|test|jar] [arguments...]
set -e
cd "$(dirname "$0")"

MODE="${1:-gui}"
[ $# -gt 0 ] && shift

# Séparateur de classpath (";" sous Windows / Git Bash)
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
  *) SEP=":" ;;
esac

rm -rf build/classes
mkdir -p build/classes
find src/main/java -name '*.java' > build/sources.txt
javac -encoding UTF-8 --release 17 -d build/classes @build/sources.txt

case "$MODE" in
  gui)  java -cp build/classes moteur.ui.InterfaceRecherche "$@" ;;
  cli)  java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp build/classes moteur.cli.Main "$@" ;;
  test)
    rm -rf build/test-classes
    mkdir -p build/test-classes
    find src/test/java -name '*.java' > build/test-sources.txt
    javac -encoding UTF-8 --release 17 -cp build/classes -d build/test-classes @build/test-sources.txt
    java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "build/classes${SEP}build/test-classes" moteur.TestsMoteur
    ;;
  jar)
    jar --create --file build/moteur-de-recherche.jar --main-class moteur.ui.InterfaceRecherche -C build/classes .
    echo "Créé : build/moteur-de-recherche.jar  (java -jar build/moteur-de-recherche.jar)"
    ;;
  *) echo "Usage : ./run.sh [gui|cli|test|jar]"; exit 1 ;;
esac
