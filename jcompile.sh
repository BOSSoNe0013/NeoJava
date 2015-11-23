#!/bin/bash

rm -rf build
rm -rf tmp
mkdir build
mkdir tmp
javac -d build -s tmp  -cp /usr/share/java/RXTXcomm.jar -sourcepath src src/NeoJava.java
jar -cvfm NeoJava.jar Manifest.txt -C build .
