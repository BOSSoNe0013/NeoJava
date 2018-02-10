JAVA tools for UDOO Neo QUAD board
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

Copy the /usr/share/java/RXTXcomm.jar in NeoJava/libs 

Now symlink _/dev/ttyMCC_ (_/dev/ttymxc3_ for Udoo Quad) to _/dev/ttyS0_ to allow UDOO's serial port binding (the NeoJava.sh script does it for you if not already done).

2 - Open a terminal and navigate to the folder where you want to download NeoJava source code:

    git clone https://github.com/BOSSoNe0013/NeoJava.git
    cd NeoJava/

3 - Compile 

	mvn package -DskipTests (or use build script build.sh)

4 - and run the app using the bash script NeoJava.sh:

    ./NeoJava.sh

The NeoJava.sh script runs the NeoJava app as root (with sudo) which is needed to load modules and could be needed to write on GPIOs (depending of your udev rules)
