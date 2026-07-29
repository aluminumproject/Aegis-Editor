#!/bin/sh

set -e

echo "Cleaning..."
rm -rf out
rm -f aegise.jar

echo "Creating output directory..."
mkdir -p out

echo "Compiling..."
javac -encoding UTF-8 -d out $(find src -name "*.java")

echo "Packaging..."
jar cfe aegise.jar com.aegis.editor.Main -C out .

echo ""
echo "Build successful!"
echo "Output: aegise.jar"