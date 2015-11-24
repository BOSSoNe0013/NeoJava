JAVA tools for UDOO Neo board
-----------------

![Image of UDOO Neo wiring](http://static.b1project.com/images/NeoJava_UDOO_Neo_wiring.jpg)

This file describes how to compile and run the Java examples contained in this folder.

1 - Install the Java library to manage the serial on your distribution (UDOObuntu 2.0)

Use the standard Vanilla JAVA Libraries.

    sudo apt-get install default-jdk

Install librxtx-java from repositories

    sudo apt-get install librxtx-java

Copy appropriate libraries and symlink them

    sudo cp /usr/lib/jni/librxtxSerial-2.2pre1.so /usr/lib/jvm/java-7-openjdk-armhf/jre/lib/arm/ 
    cd /usr/lib/jvm/java-7-openjdk-armhf/jre/lib/arm/
    sudo ln -s librxtxSerial-2.2pre1.so librxtxSerial.so

Now symlink /dev/ttyMCC to /dev/ttyS0 to allow UDOO's serial port binding

/!\ the jrun.sh script does it for you if not already done ;)

2 - Open a terminal and navigate to this folder:

    cd NeoJava/

3 - Compile the Java file:
 use the bash script jcompile.sh:
 
    ./jcoompile.sh

4 - Run the app
 use the bash script jrun.sh:

    sudo ./jrun.sh

 sudo is needed to write on GPIOs


