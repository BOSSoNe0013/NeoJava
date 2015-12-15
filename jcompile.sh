#!/bin/bash

rm -rf build
rm -rf tmp
mkdir build
mkdir tmp
javac -d build -s tmp  -cp libs/RXTXcomm.jar:libs/gson-2.5.jar -sourcepath src src/NeoJava.java
jar -cvfm NeoJava.jar Manifest.txt -C build .
