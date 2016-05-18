/**
*  Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
*
*  This file is part of NeoJava Tools for UDOO Neo
*
*  NeoJava Tools for UDOO Neo is free software: you can redistribute it and/or modify
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
package com.b1project.udooneo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

import com.b1project.udooneo.board.BoardInfo;
import com.b1project.udooneo.gpio.Gpio;
import com.b1project.udooneo.gpio.GpiosManager;
import com.b1project.udooneo.lcd.Lcd;
import com.b1project.udooneo.listeners.GpiosManagerListener;
import com.b1project.udooneo.listeners.NeoJavaProtocolListener;
import com.b1project.udooneo.listeners.STDInputListener;
import com.b1project.udooneo.listeners.SerialOutputListener;
import com.b1project.udooneo.model.Pin;
import com.b1project.udooneo.net.NeoJavaProtocol;
import com.b1project.udooneo.net.NeoJavaSecureServer;
import com.b1project.udooneo.net.NeoJavaServer;
import com.b1project.udooneo.sensors.callback.AccelerometerReaderCallBack;
import com.b1project.udooneo.sensors.callback.GyroscopeReaderCallBack;
import com.b1project.udooneo.sensors.callback.MagnetometerReaderCallBack;
import com.b1project.udooneo.sensors.callback.TemperatureReaderCallBack;
import com.b1project.udooneo.sensors.reader.AccelerometerReader;
import com.b1project.udooneo.sensors.reader.GyroscopeReader;
import com.b1project.udooneo.sensors.reader.MagnetometerReader;
import com.b1project.udooneo.sensors.reader.TemperatureReader;
import com.b1project.udooneo.serial.SimpleSerial;

public class NeoJava implements SerialOutputListener, NeoJavaProtocolListener, GpiosManagerListener {

	public static final String DEFAULT_BINDING_TTY = "/dev/ttyS0";
	public static final int DEFAULT_SERVER_PORT = 45045;
	public static final int SERIAL_PORT_BAUD_RATE = 115200;
	
	private static Lcd mLcd;
    private static SimpleSerial mSerial;
    private static String mCurrentMessage = "Java GPIO with\n"+ BoardInfo.getBoardModel();
    private final static String INPUT_COMMAND_QUIT = "/q";
    private final static String INPUT_COMMAND_VERSION = "/v";
    private final static String INPUT_COMMAND_LCD_CLEAR = "/lc";
    private final static String INPUT_COMMAND_LCD_PRINT = "/lp";
    private final static String INPUT_COMMAND_TEMP_REQUEST = "/tp";
    private final static String INPUT_COMMAND_EXPORTED_GPIOS = "/gpios";
    private final static String INPUT_COMMAND_BOARD_ID = "/id";
    private final static String INPUT_COMMAND_BOARD_MODEL = "/model";
    private final static String INPUT_COMMAND_BOARD_NAME = "/name";
    private final static boolean USE_SECURE_SERVER = false;
    private static boolean mLcdPrinting = false;
    private static boolean mPrintingTemperature = false;
    private static boolean mInitComplete = false;
    private static NeoJava instance;
    private static NeoJavaServer server;
    private static NeoJavaSecureServer secureServer;
    private static Gpio gpioNotificationLed;
    private static GpiosManager gpiosManager;
    private final static char[] CUSTOM_CHAR = {
            0b00000,
            0b10010,
            0b00000,
            0b01100,
            0b01100,
            0b00000,
            0b10010,
            0b00000};
	private Properties properties;

    public static void main(String[] args) {
        try{
            System.out.println(
                    getInstance().getVersionString()
                            + " (Java platform tools for "
                            + BoardInfo.getBoardModel() + ")");
            gpiosManager = GpiosManager.getInstance();
            gpiosManager.addListener(getInstance());
            mLcd = initLCD();
            mSerial = new SimpleSerial(DEFAULT_BINDING_TTY, getInstance());
            mSerial.connect();
            if(USE_SECURE_SERVER){
                startNeoJavaSecureServer();
            }
            else {
                startNeoJavaServer();
            }
            startSTDINListener();
            System.out.println("\rInit complete");
            System.out.print("#:");
            mInitComplete = true;
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }
	
    public NeoJava() {
    	properties = new Properties();
    	try {
			properties.load(NeoJava.class.getResourceAsStream("/version.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @Override
    public String getVersionString() {
        return String.format("%s %s", properties.getProperty("app.name"),properties.getProperty("app.version"));
    }

    private static NeoJava getInstance(){
        if(instance == null){
            instance = new NeoJava();
        }
        return instance;
    }
    
    private static void startSTDINListener() throws Exception {
		setupSTDINListener(new DefaultSTDInputListener());
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
    
    private static void startNeoJavaSecureServer(){
        new Thread(){
            @Override
            public void run(){
                secureServer = NeoJavaSecureServer.getInstance(getInstance());
                secureServer.startServer();
            }
        }.start();
    }

    private static Lcd initLCD() throws Exception {
    	gpioNotificationLed = Gpio.getInstance(GpiosManager.GPIO_102);
        gpioNotificationLed.setMode(Gpio.PinMode.OUTPUT);
        for(int i = 0; i < 5; i++){
            gpioNotificationLed.high();
            Thread.sleep(300);
            gpioNotificationLed.low();
            Thread.sleep(50);
        }
        mLcd = new Lcd(GpiosManager.GPIO_20, GpiosManager.GPIO_21, GpiosManager.GPIO_25,
                GpiosManager.GPIO_22, GpiosManager.GPIO_14, GpiosManager.GPIO_15, GpiosManager.GPIO_16, Lcd.NO_RW);
        mLcd.createChar(0x01, CUSTOM_CHAR);
        mLcd.clear();
        if(mCurrentMessage != null) {
            mLcd.print(mCurrentMessage);
        }
        Thread.sleep(1000);
        mLcd.clear();
        mLcd.write((char)0x01);
        mLcd.write((char)0x01);
        mLcd.write((char)0x01);
        return mLcd;
    }
    
    protected String handleLineInput(String line){
        switch (line) {
            case INPUT_COMMAND_LCD_CLEAR:
                try {
                    mLcd.clear();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                System.out.print("#:");
                break;
            case INPUT_COMMAND_QUIT:
                System.out.println("\nGoodbye !");
                try {
                    if(server != null) {
                        server.stopServer();
                    }
                    if(secureServer != null) {
                        secureServer.stopServer();
                    }
                    mLcd.setLcdDisplayState(false);
                    mLcd.setBacklightState(false);
                    mLcd = null;
                    if(mSerial != null){
                        mSerial.disconnect();
                    }
                    gpioNotificationLed.release();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                System.exit(0);
                break;
            case INPUT_COMMAND_VERSION:
            	if(!mLcdPrinting){
            		getInstance().onLCDPrintRequest(getInstance().getVersionString() + "\n");
            	}
            	System.out.print(getInstance().getVersionString() + "\n");
                System.out.print("#:");
                break;
            case INPUT_COMMAND_LCD_PRINT:
                mLcdPrinting = true;
                System.out.print("");
                break;
            case INPUT_COMMAND_TEMP_REQUEST:
                (new Thread(new TemperatureReader(new TemperatureReaderCallBack(){

                    @Override
                    public void onRequestComplete(Float temp, Float pressure) {
                        String tempString = String.format("Temp: %.1fßC\nPres: %.1fkPa", temp, pressure);
                        if(!mLcdPrinting){
                            mLcdPrinting = true;
	                        try {
	                            mLcd.clear();
	                            mLcd.print(tempString);
	                            Thread.sleep(3000);
	                            mLcd.clear();
                                if(mCurrentMessage != null) {
                                    mLcd.print(mCurrentMessage);
                                }
	                        }
	                        catch (Exception e){
	                            System.err.println("\rError: " + e.getMessage());
	                        }
                            mLcdPrinting = false;
                        }else{
                            System.out.print("\r" + tempString.replace("ß","°"));
                        }
                    }
                }))).start();
                System.out.print("#:");
                break;
            case INPUT_COMMAND_EXPORTED_GPIOS:
                System.out.println("\r" + gpiosManager.getExportedGpios());
                System.out.print("#:");
                break;
            case INPUT_COMMAND_BOARD_ID:
                System.out.println("\r" + BoardInfo.getBoardID());
                System.out.print("#:");
                break;
            case INPUT_COMMAND_BOARD_MODEL:
                System.out.println("\r" + BoardInfo.getBoardModel());
                System.out.print("#:");
                break;
            case INPUT_COMMAND_BOARD_NAME:
                System.out.println("\r" + BoardInfo.getBoardName());
                System.out.print("#:");
                break;
            default:
                if(mLcdPrinting) {
                    System.out.print("");
                    return line;
                }
                else{
                    if(!line.equals("")) {
                        System.out.println("\rError: command not found");
                    }
                    System.out.print("#:");
                }
                break;
        }
        return null;
    }

    @Override
    public void onQuitRequest(Socket clientSocket) {
        try {
            if(clientSocket != null) {
                clientSocket.close();
            }
        }
        catch (Exception e){
            System.out.println("\rError while closing socket");
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
            if(message != null) {
                mLcd.print(message);
            }
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
            (new Thread(new TemperatureReader(new TemperatureReaderCallBack() {
                @Override
                public void onRequestComplete(Float temp, Float pressure) {
                    if(server != null) {
                        server.writeOutput(NeoJavaProtocol.makeTemperatureMessage(temp, pressure));
                    }
                    if(secureServer != null) {
                        secureServer.writeOutput(NeoJavaProtocol.makeTemperatureMessage(temp, pressure));
                    }
                }
            }))).start();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onAccelerometerRequest() {
        try{
            (new Thread(new AccelerometerReader(new AccelerometerReaderCallBack() {
                @Override
                public void onRequestComplete(String data) {
                    if(server != null) {
                        server.writeOutput(NeoJavaProtocol.makeAccelerometerMessage(data));
                    }
                    if(secureServer != null) {
                        secureServer.writeOutput(NeoJavaProtocol.makeAccelerometerMessage(data));
                    }
                }
            }))).start();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onMagnetometerRequest() {
        try{
            (new Thread(new MagnetometerReader(new MagnetometerReaderCallBack() {
                @Override
                public void onRequestComplete(String data) {
                    if(server != null) {
                        server.writeOutput(NeoJavaProtocol.makeMagnetometerMessage(data));
                    }
                    if(secureServer != null) {
                        secureServer.writeOutput(NeoJavaProtocol.makeMagnetometerMessage(data));
                    }
                }
            }))).start();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onGyroscopeRequest() {
        try{
            (new Thread(new GyroscopeReader(new GyroscopeReaderCallBack() {
                @Override
                public void onRequestComplete(String data) {
                    if(server != null) {
                        server.writeOutput(NeoJavaProtocol.makeGyroscopeMessage(data));
                    }
                    if(secureServer != null) {
                        secureServer.writeOutput(NeoJavaProtocol.makeGyroscopeMessage(data));
                    }
                }
            }))).start();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public List<Pin> onExportedGpiosRequest() {
        return gpiosManager.getExportedGpios();
    }

    @Override
    public void onStateChanged(int pinId, Gpio.PinState state) {
        /*System.out.println("\rGPIO_" + pinId + " state changed to: " + state);
        System.out.print("#:");*/
        if(server != null) {
            server.writeOutput(NeoJavaProtocol.makePinStateMessage(pinId, state));
        }
        if(secureServer != null) {
            secureServer.writeOutput(NeoJavaProtocol.makePinStateMessage(pinId, state));
        }
    }

    @Override
    public void onModeChanged(int pinId, Gpio.PinMode mode) {
        /*System.out.println("\rGPIO_" + pinId + " mode changed to: " + mode);
        System.out.print("#:");*/
        if(server != null) {
            server.writeOutput(NeoJavaProtocol.makePinModeMessage(pinId, mode));
        }
        if(secureServer != null) {
            secureServer.writeOutput(NeoJavaProtocol.makePinModeMessage(pinId, mode));
        }
    }

    @Override
    public void onExport(int pinId) {
        System.out.println("\rGPIO_" + pinId + " exported");
        System.out.print("#:");
        if(server != null) {
            server.writeOutput(NeoJavaProtocol.makeExportMessage(onExportedGpiosRequest()));
        }
        if(secureServer != null) {
            secureServer.writeOutput(NeoJavaProtocol.makeExportMessage(onExportedGpiosRequest()));
        }
    }

    @Override
    public void onRelease(int pinId) {
        System.out.println("\rGPIO_" + pinId + " released");
        System.out.print("#:");
        if(server != null) {
            server.writeOutput(NeoJavaProtocol.makeExportMessage(onExportedGpiosRequest()));
        }
        if(secureServer != null) {
            secureServer.writeOutput(NeoJavaProtocol.makeExportMessage(onExportedGpiosRequest()));
        }
    }

    private static final class DefaultSTDInputListener implements STDInputListener {
		int l = 0;
		String message;

		@Override
		public void onNewLine(String line){
		    try{
		        if(l == 0){
		            String out = getInstance().handleLineInput(line);
		            if(out != null){
		                message = out;
		                l++;
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
	}

    @Override
    public void onNewLine(String line) {
    	System.out.printf("\rSerial out: %s\n", line.trim());
        System.out.print("#:");
    	if(mInitComplete && line.startsWith("0x")) {
            int code = Integer.parseInt(line.replace("0x", "").trim(), 16);
            switch (code) {
                case 0x01:
                    if(!mPrintingTemperature) {
                        mPrintingTemperature = true;
		                    (new Thread(new TemperatureReader(new TemperatureReaderCallBack(){
		
		                    @Override
		                    public void onRequestComplete(Float temp, Float pressure) {
		                        String tempString = String.format("Temp: %.1fßC\nPres: %.1fkPa", temp, pressure);
		                        try {
                                    if(!mLcdPrinting){
                                        mLcdPrinting = true;
                                        mLcd.clear();
                                        mLcd.print(tempString);
                                        Thread.sleep(3000);
                                        mLcd.clear();
                                        if(mCurrentMessage != null) {
                                            mLcd.print(mCurrentMessage);
                                        }
                                        mPrintingTemperature = false;
                                        mLcdPrinting = false;
                                    }else{
                                        System.out.print("\r" + tempString.replace("ß","°"));
                                        System.out.print("#:");
                                    }
		                        }
		                        catch (Exception e){
		                            System.err.println("\rError: " + e.getMessage());
                                    System.out.print("#:");
		                        }
		                    }
		                }))).start();
                    }
                    break;
            }
        }
    }
}
