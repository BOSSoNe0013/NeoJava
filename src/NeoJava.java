/**
*  Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
*
*  This file is part of NeoJava examples for UDOO
*
*  NeoJava examples for UDOO is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This libraries are distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/

import com.b1project.udooneo.listeners.NeoJavaProtocolListener;
import com.b1project.udooneo.listeners.STDInputListener;
import com.b1project.udooneo.gpio.Gpio;
import com.b1project.udooneo.lcd.Lcd;
import com.b1project.udooneo.listeners.SerialOutputListener;
import com.b1project.udooneo.net.NeoJavaServer;
import com.b1project.udooneo.sensors.BarometerSensor;
import com.b1project.udooneo.sensors.TemperatureSensor;
import com.b1project.udooneo.serial.Serial;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NeoJava implements SerialOutputListener, NeoJavaProtocolListener {

    static Lcd mLcd;
    static Serial mSerial;
    static String mCurrentMessage = "";
    public final static String APP_NAME = "NeoJava";
    public final static String VERSION = "0.0.1";
    final static String INPUT_COMMAND_QUIT = "/q";
    final static String INPUT_COMMAND_VERSION = "/v";
    final static String INPUT_COMMAND_LCD_CLEAR = "/lc";
    final static String INPUT_COMMAND_LCD_PRINT = "/lp";
    final static String INPUT_COMMAND_TEMP_REQUEST = "/tp";
    static boolean mLcdPrinting = false;
    static NeoJava instance;
    static NeoJavaServer server;
    static Gpio gpioNotificationLed;
    final static char[] CUSTOM_CHAR = {
            0b00000,
            0b10010,
            0b00000,
            0b01100,
            0b01100,
            0b00000,
            0b10010,
            0b00000};

    public NeoJava() {
        super();
    }

    static NeoJava getInstance(){
        if(instance == null){
            instance = new NeoJava();
        }
        return instance;
    }
    
    public static Lcd initLCD() throws Exception{
        gpioNotificationLed = Gpio.getInstance(106);
        gpioNotificationLed.setMode(Gpio.PinMode.OUTPUT);
        for(int i = 0; i < 5; i++){
            gpioNotificationLed.high();
            Thread.sleep(300);
            gpioNotificationLed.low();
            Thread.sleep(50);
        }
        mLcd = new Lcd(20, 21, 25, 22, 14, 15);
        mLcd.createChar(0x01, CUSTOM_CHAR);
        mLcd.clear();
        mLcd.print("Hello Java GPIO\nwith UDOO Neo !!");
        Thread.sleep(1000);
        mLcd.clear();
        mLcd.write((char)0x01);
        mLcd.write((char)0x01);
        mLcd.write((char)0x01);
        return mLcd;
    }
    
    public static void setupSTDINListener(final STDInputListener listener) throws Exception{
        new Thread(){
            @Override
            public void run(){
                try{
                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                    String s;
                    while ((s = in.readLine()) != null){
                        listener.onNewLine(s);
                    }
                    Thread.sleep(50);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void startNeoJavaServer(){
        new Thread(){
            @Override
            public void run(){
                server = NeoJavaServer.getInstance(getInstance());
                server.startServer();
            }
        }.start();
    }
    
    public static void main(String[] args) {
        try{
            System.out.print(getInstance().getVersionString());
            System.out.println("Java platform tools for UDOO Neo");
            mLcd = initLCD();
            mSerial = new Serial("/dev/ttyS0", getInstance());
            mSerial.connect();
            startNeoJavaServer();
            setupSTDINListener(new STDInputListener(){
                int l = 0;
                String message;
                @Override
                public void onNewLine(String line){
                    try{
                        if(l == 0){
                            switch (line) {
                                case INPUT_COMMAND_LCD_CLEAR:
                                    mLcd.clear();
                                    System.out.print("#:");
                                    break;
                                case INPUT_COMMAND_QUIT:
                                    System.out.println("\nGoodbye !");
                                    if(server != null) {
                                        server.stopServer();
                                    }
                                    mLcd.setLcdDisplayState(false);
                                    mLcd.setBacklightState(false);
                                    mLcd = null;
                                    gpioNotificationLed.unexport();
                                    System.exit(0);
                                    break;
                                case INPUT_COMMAND_VERSION:
                                    getInstance().onLCDPrintRequest(getInstance().getVersionString() + "\n");
                                    System.out.print(getInstance().getVersionString());
                                    System.out.print("#:");
                                    break;
                                case INPUT_COMMAND_LCD_PRINT:
                                    mLcdPrinting = true;
                                    System.out.print(" ");
                                    break;
                                case INPUT_COMMAND_TEMP_REQUEST:
                                    (new Thread(new TempReader())).start();
                                    System.out.print("#:");
                                    break;
                                default:
                                    if(mLcdPrinting) {
                                        message = line;
                                        System.out.print(" ");
                                        l++;
                                    }
                                    else{
                                        System.out.println("Error: command not found");
                                        System.out.print("#:");
                                    }
                                    break;
                            }
                        }
                        else{
                            if(mLcdPrinting) {
                                message += "\n" + line;
                                l = 0;
                                getInstance().onLCDPrintRequest(message);
                            }
                            System.out.print("#:");
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("Init complete");
            System.out.print("#:");
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }

    }

    @Override
    public String getVersionString() {
        return String.format("%s %s\n", APP_NAME, VERSION);
    }

    @Override
    public void onQuitRequest(Socket clientSocket) {
        try {
            if(clientSocket != null) {
                clientSocket.close();
            }
        }
        catch (Exception e){
            System.out.println("Error while closing socket");
            e.printStackTrace();
        }
    }

    @Override
    public void onClearLCDRequest() {
        try{
            mLcd.clear();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLCDPrintRequest(String message) {
        try{
            mLcdPrinting = true;
            mLcd.clear();
            mLcd.print(message);
            mCurrentMessage = message;
            mLcdPrinting = false;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onTemperatureRequest() {
        try{
            (new Thread(new TempReader())).start();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    static class TempReader implements Runnable{

        @Override
        public void run() {
            try {
                Float temp = TemperatureSensor.getTemperature();
                Float pressure = BarometerSensor.getPressure();
                String tempString = String.format("Temp: %.1fßC\nPres: %.1f", temp, pressure);
                mLcd.clear();
                mLcd.print(tempString);
                Thread.sleep(3000);
                mLcd.clear();
                mLcd.print(mCurrentMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNewLine(String line) {
        System.out.printf("\nSerial out: %s\n", line.trim());
        if(line.startsWith("0x")) {
            int code = Integer.parseInt(line.replace("0x", "").trim(), 16);
            switch (code) {
                case 0x01:
                    (new Thread(new TempReader())).start();
                    break;
            }
        }
        System.out.print("#:");
    }
}
