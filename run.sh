#!/bin/sh
# Compile and run Emergency SOS (Stage 1). Needs JDK 11+ (javac must work).
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -d out -sourcepath src src/com/sos/Main.java || exit 1
echo "Starting server... open http://localhost:8080"
java -cp out com.sos.Main
