package com.b1project.udooneo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.*;
import java.util.prefs.Preferences;

import com.b1project.udooneo.board.BoardInfo;
import com.b1project.udooneo.gpio.Gpio;
import com.b1project.udooneo.gpio.GpiosManager;
import com.b1project.udooneo.lcd.Lcd;
import com.b1project.udooneo.listeners.GpiosManagerListener;
import com.b1project.udooneo.listeners.NeoJavaProtocolListener;
import com.b1project.udooneo.listeners.STDInputListener;
import com.b1project.udooneo.listeners.SerialOutputListener;
import com.b1project.udooneo.model.Pin;
import com.b1project.udooneo.net.InetDevice;
import com.b1project.udooneo.net.NeoJavaProtocol;
import com.b1project.udooneo.net.NeoJavaSecureServer;
import com.b1project.udooneo.net.NeoJavaServer;
import com.b1project.udooneo.pwm.Pwm;
import com.b1project.udooneo.sensors.callback.*;
import com.b1project.udooneo.sensors.reader.*;
import com.b1project.udooneo.serial.Serial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class NeoJava implements SerialOutputListener, NeoJavaProtocolListener, GpiosManagerListener {

    private static final String PREF_PWM_ENABLE = "pwm_enable";
    private static final String PREF_LCD_ENABLE = "lcd_enable";
    private static final String PREF_USE_SECURE_SERVER = "use_secure_server";
    private static final String PREF_SERIAL_COM_ENABLE = "serial_com_enable";
    private static final String PREF_DEBUG_ENABLE = "debug_enable";
    public static boolean DEBUG = false;

    public static final String DEFAULT_BINDING_TTY = "/dev/ttyS0";
    public static final int DEFAULT_SERVER_PORT = 45045;
    public static final int SERIAL_PORT_BAUD_RATE = 115200;

    private static Lcd mLcd;
    private static Serial mSerial;
    private static String mCurrentMessage = "Java GPIO with\n"+ BoardInfo.getBoardModel();
    private final static String INPUT_COMMAND_QUIT = "/q";
    private final static String INPUT_COMMAND_HELP = "/h";
    private final static String INPUT_COMMAND_VERSION = "/v";
    private final static String INPUT_COMMAND_LCD_CLEAR = "/lc";
    private final static String INPUT_COMMAND_LCD_PRINT = "/lp";
    private final static String INPUT_COMMAND_TEMP_REQUEST = "/tp";
    private final static String INPUT_COMMAND_LIGHT_POWER_REQUEST = "/lpw";
    private final static String INPUT_COMMAND_EXPORTED_GPIOS = "/gpios";
    private final static String INPUT_COMMAND_PWM = "/pwm";
    private final static String INPUT_COMMAND_SERIAL = "/tty";
    private final static String INPUT_COMMAND_BOARD_REBOOT = "/reboot";
    private final static String INPUT_COMMAND_BOARD_ID = "/id";
    private final static String INPUT_COMMAND_BOARD_NET = "/net";
    private final static String INPUT_COMMAND_BOARD_MODEL = "/model";
    private final static String INPUT_COMMAND_BOARD_NAME = "/name";
    private final static String INPUT_COMMAND_DEBUG = "/debug";
    public static String CURRENT_SERIAL_RGB_VALUE = "0,0,0|0,0,0";
    private static boolean mLcdPrinting = false;
    private static boolean mPrintingTemperature = false;
    private static boolean mInitComplete = false;
    private static NeoJava instance;
    private static NeoJavaServer mServer;
    private static NeoJavaSecureServer mSecureServer;
    private static Gpio gpioNotificationLed;
    private static GpiosManager mGpiosManager;
    private final static char[] CUSTOM_CHAR = {
            0b00000,
            0b10010,
            0b00000,
            0b01100,
            0b01100,
            0b00000,
            0b10010,
            0b00000};
    private final Properties mProperties;
    private static Preferences mPreferences;
    private Thread mShutdownHookThread;
    public static final Logger logger = LoggerFactory.getLogger(NeoJava.class);

    public static void main(String[] args) {
        try{
            logger.info(
                    getInstance().getVersionString()
                            + " (Java platform tools for "
                            + BoardInfo.getBoardModel() + ")");
            mPreferences = Preferences.userRoot().node(NeoJava.class.getSimpleName());
            DEBUG = mPreferences.getBoolean(PREF_DEBUG_ENABLE, false);
            mGpiosManager = GpiosManager.getInstance();
            mGpiosManager.addListener(getInstance());

            if(mPreferences.getBoolean(PREF_PWM_ENABLE, true)) {
                Pwm pwm = Pwm.getInstance(0);
                pwm.configure(1000000, 0);
                logger.info("\rPWM setup complete");
                System.out.print("#:");
            }

            if(mPreferences.getBoolean(PREF_LCD_ENABLE, true)) {
                mLcd = initLCD();
            }

            if(mPreferences.getBoolean(PREF_SERIAL_COM_ENABLE, true)) {
                mSerial = new Serial(DEFAULT_BINDING_TTY, getInstance());
                mSerial.connect();
            }

            if(mPreferences.getBoolean(PREF_USE_SECURE_SERVER, false)){
                startNeoJavaSecureServer();
            }
            else {
                startNeoJavaServer();
            }

            startSTDINListener();
            logger.info("\rInit complete");
            System.out.print("#:");
            mInitComplete = true;
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    public NeoJava() {
        mProperties = new Properties();
        try {
            mProperties.load(NeoJava.class.getResourceAsStream("/version.properties"));
            mShutdownHookThread = new Thread(){
                @Override
                public void run() {
                    getInstance().quit();
                }
            };
            Runtime.getRuntime().addShutdownHook(mShutdownHookThread);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getVersionString() {
        return String.format("%s %s", mProperties.getProperty("app.name"), mProperties.getProperty("app.version"));
    }

    private static NeoJava getInstance(){
        if(instance == null){
            instance = new NeoJava();
        }
        return instance;
    }
    
    private static void startSTDINListener() {
        setupSTDINListener(new DefaultSTDInputListener());
    }
    
    private static void setupSTDINListener(final STDInputListener listener){
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

    private static void startNeoJavaServer(){
        new Thread(){
            @Override
            public void run(){
                mServer = NeoJavaServer.getInstance(getInstance());
                mServer.startServer();
            }
        }.start();
    }
    
    private static void startNeoJavaSecureServer(){
        new Thread(){
            @Override
            public void run(){
                mSecureServer = NeoJavaSecureServer.getInstance(getInstance());
                mSecureServer.startServer();
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
    
    private String handleLineInput(String line){
        String[] words = line.split(" ");
        switch (words[0]) {
            case INPUT_COMMAND_HELP:
                System.out.println("\r" + INPUT_COMMAND_HELP + " - this help");
                System.out.println(INPUT_COMMAND_QUIT + " - quit");
                System.out.println(INPUT_COMMAND_DEBUG + " on|off - turn on/off debug mode");
                System.out.println(INPUT_COMMAND_VERSION + " - show version");
                System.out.println(INPUT_COMMAND_BOARD_NET + " - show network devices");
                System.out.println(INPUT_COMMAND_EXPORTED_GPIOS + " - list exported gpios");
                System.out.println(INPUT_COMMAND_PWM + " - read/write PWM value");
                System.out.println(INPUT_COMMAND_LCD_CLEAR + " - clear LCD");
                System.out.println(INPUT_COMMAND_LCD_PRINT + " - print message on 16x2 LCD screen");
                System.out.println(INPUT_COMMAND_TEMP_REQUEST + " - request temperature and pressure");
                System.out.print("#:");
                break;
            case INPUT_COMMAND_DEBUG:
                if(words.length >= 2){
                    switch (words[1]) {
                        case "on":
                            DEBUG = true;
                            break;
                        case "off":
                            DEBUG = false;
                            break;
                        default:
                            logger.warn("\rError: Invalid args");
                            return null;
                    }
                }
                mPreferences.putBoolean(PREF_DEBUG_ENABLE, DEBUG);
                System.out.print("\rDEBUG is ");
                System.out.println(DEBUG?"ON":"OFF");
                System.out.print("#:");
                break;
            case INPUT_COMMAND_LCD_CLEAR:
                if(mPreferences.getBoolean(PREF_LCD_ENABLE, true)) {
                    try {
                        mLcd.clear();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.print("#:");
                break;
            case INPUT_COMMAND_QUIT:
                quit();
                break;
            case INPUT_COMMAND_VERSION:
                if(mPreferences.getBoolean(PREF_LCD_ENABLE, true) && !mLcdPrinting){
                    mLcdPrinting = true;
                    try {
                        mLcd.clear();
                        mLcd.print(getInstance().getVersionString() + "\n");
                        Thread.sleep(3000);
                        mLcd.clear();
                        if(mCurrentMessage != null) {
                            mLcd.print(mCurrentMessage);
                        }
                    } catch (Exception e) {
                        logger.warn("\rError: " + e.getMessage());
                        mLcdPrinting = false;
                    }
                    finally {
                        mLcdPrinting = false;
                    }
                }
                System.out.println(getInstance().getVersionString());
                System.out.print("#:");
                break;
            case INPUT_COMMAND_LCD_PRINT:
                mLcdPrinting = true;
                System.out.print("");
                break;
            case INPUT_COMMAND_TEMP_REQUEST:
                System.out.print("#:");
                (new Thread(new TemperatureReader(new TemperatureReaderCallBack(){

                    @Override
                    public void onRequestComplete(Float temp, Float pressure) {
                        String tempString = String.format("Temp: %.1fßC\nPres: %.1fkPa", temp, pressure);
                        if(mPreferences.getBoolean(PREF_LCD_ENABLE, true) && !mLcdPrinting){
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
                                logger.warn("\rError: " + e.getMessage());
                                mLcdPrinting = false;
                            }
                            finally {
                                mLcdPrinting = false;
                            }
                        }
                        System.out.println("\r" + tempString.replace("ß","°"));
                        System.out.print("#:");
                    }
                }))).start();
                break;
            case INPUT_COMMAND_LIGHT_POWER_REQUEST:
                System.out.print("#:");
                (new Thread(new LightPowerReader(new LightPowerReaderCallback(){

                    @Override
                    public void onRequestComplete(Float power) {
                        String lpwString = String.format("Light power: %.1flm\n", power);
                        if(mPreferences.getBoolean(PREF_LCD_ENABLE, true) && !mLcdPrinting){
                            mLcdPrinting = true;
                            try {
                                mLcd.clear();
                                mLcd.print(lpwString);
                                Thread.sleep(3000);
                                mLcd.clear();
                                if(mCurrentMessage != null) {
                                    mLcd.print(mCurrentMessage);
                                }
                            }
                            catch (Exception e){
                                logger.warn("\rError: " + e.getMessage());
                                mLcdPrinting = false;
                            }
                            finally {
                                mLcdPrinting = false;
                            }
                        }
                        System.out.println("\r" + lpwString);
                        System.out.print("#:");
                    }
                }))).start();
                break;
            case INPUT_COMMAND_EXPORTED_GPIOS:
                System.out.println("\r" + mGpiosManager.getExportedGpios());
                System.out.print("#:");
                break;
            case INPUT_COMMAND_PWM:
                if(mPreferences.getBoolean(PREF_PWM_ENABLE, true)) {
                    try {
                        Pwm pwm = Pwm.getInstance(0);
                        if (words.length >= 2) {
                            pwm.set8BitValue(Long.parseLong(words[1]));
                        }
                        System.out.println("\rPWM: " + pwm.get8BitValue());
                        System.out.print("#:");
                    } catch (Exception e) {
                        logger.warn("\rError: " + e.getMessage());
                    }
                }
                break;
            case INPUT_COMMAND_SERIAL:
                if(mPreferences.getBoolean(PREF_SERIAL_COM_ENABLE, true)) {
                    try {
                        if (words.length >= 3) {
                            final String dataType = words[1];
                            System.out.println("\rLine: " + line);
                            System.out.println("\rType: " + dataType);
                            String ttyCmd = line.replaceAll(INPUT_COMMAND_SERIAL + " " + dataType + " ", "");
                            System.out.println("\rCmd: " + ttyCmd);
                            switch (dataType) {
                                case "INT":
                                    String[] values = ttyCmd.split(";");
                                    System.out.println("\rValues: " + Arrays.toString(values));
                                    for (String value : values) {
                                        System.out.println("\rValue: " + value);
                                        mSerial.write(Integer.decode(value));
                                    }
                                    break;
                                case "STR":
                                    mSerial.print(ttyCmd);
                                    break;
                            }
                        } else {
                            logger.warn("\rError: no data provided");
                        }
                        System.out.print("#:");
                    } catch (Exception e) {
                        logger.warn("\rError: " + e.getMessage());
                    }
                }
                break;
            case INPUT_COMMAND_BOARD_NET:
                if(mPreferences.getBoolean(PREF_LCD_ENABLE, true) && !mLcdPrinting){
                    mLcdPrinting = true;
                    try {
                        InetDevice device = new InetDevice(InetDevice.Dev.WLAN);
                        mLcd.clear();
                        mLcd.print(InetDevice.Dev.WLAN + ": " + device.getState() + "\n");
                        if (device.getState() == InetDevice.State.DOWN){
                            mLcd.print("reboot needed...");
                        }
                        Thread.sleep(3000);
                        mLcd.clear();
                        if(mCurrentMessage != null) {
                            mLcd.print(mCurrentMessage);
                        }
                    } catch (Exception e) {
                        System.err.println("\rError: " + e.getMessage());
                        mLcdPrinting = false;
                    }
                    finally {
                        mLcdPrinting = false;
                    }
                }
                List<NetworkInterface> nets = InetDevice.listAll();
                if (nets != null) {
                    System.out.print("\r");
                    for (NetworkInterface inet : nets) {
                        Enumeration<InetAddress> ips = inet.getInetAddresses();
                        System.out.println(inet.getName() + ": ");
                        for(InetAddress ip: Collections.list(ips)) {
                            System.out.println("\t" + ip);
                        }
                    }
                    System.out.print("#:");
                }
                break;
            case INPUT_COMMAND_BOARD_REBOOT:
                System.out.println("\r" + BoardInfo.reboot());
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
                        logger.warn("\rError: command not found");
                    }
                    System.out.print("#:");
                }
                break;
        }
        return null;
    }

    private void quit() {
        try {
            System.out.println("\nGoodbye !");
            if (mShutdownHookThread != null) {
                Runtime.getRuntime().removeShutdownHook(mShutdownHookThread);
            }
            if(mServer != null) {
                mServer.stopServer();
            }
            if(mSecureServer != null) {
                mSecureServer.stopServer();
            }
            if(mPreferences.getBoolean(PREF_LCD_ENABLE, true)&& mLcd != null) {
                mLcd.print("Goodbye !\n");
                Thread.sleep(3);
                mLcd.setLcdDisplayState(false);
                mLcd.setBacklightState(false);
                Thread.sleep(3);
                mLcd = null;
            }
            if(mSerial != null){
                mSerial.disconnect();
            }
            gpioNotificationLed.release();
            System.exit(0);
        }
        catch (Exception e){
            logger.warn("\rError :" + e.getLocalizedMessage());
            System.exit(0);
        }
    }

    @Override
    public void onQuitRequest(Socket clientSocket) {
        try {
            if(clientSocket != null) {
                clientSocket.close();
            }
            quit();
        }
        catch (Exception e){
            logger.warn("\rError while closing socket");
            e.printStackTrace();
        }
    }

    @Override
    public void onClearLCDRequest() {
        if(mPreferences.getBoolean(PREF_LCD_ENABLE, true)) {
            try {
                mLcd.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLCDPrintRequest(String message) {
        if(mPreferences.getBoolean(PREF_LCD_ENABLE, true)) {
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
    }

    @Override
    public void onTemperatureRequest() {
        try{
            (new Thread(new TemperatureReader(new TemperatureReaderCallBack() {
                @Override
                public void onRequestComplete(Float temp, Float pressure) {
                    if(mServer != null) {
                        mServer.writeOutput(NeoJavaProtocol.makeTemperatureMessage(temp, pressure));
                    }
                    if(mSecureServer != null) {
                        mSecureServer.writeOutput(NeoJavaProtocol.makeTemperatureMessage(temp, pressure));
                    }
                }
            }))).start();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLightPowerRequest() {
        try {
            (new Thread(new LightPowerReader(new LightPowerReaderCallback() {
                @Override
                public void onRequestComplete(Float power) {
                    if(mServer != null) {
                        mServer.writeOutput(NeoJavaProtocol.makeLightPowerMessage(power));
                    }
                    if(mSecureServer != null) {
                        mSecureServer.writeOutput(NeoJavaProtocol.makeLightPowerMessage(power));
                    }
                }
            }))).start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccelerometerRequest() {
        try{
            (new Thread(new AccelerometerReader(new AccelerometerReaderCallBack() {
                @Override
                public void onRequestComplete(String data) {
                    if(mServer != null) {
                        mServer.writeOutput(NeoJavaProtocol.makeAccelerometerMessage(data));
                    }
                    if(mSecureServer != null) {
                        mSecureServer.writeOutput(NeoJavaProtocol.makeAccelerometerMessage(data));
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
                    if(mServer != null) {
                        mServer.writeOutput(NeoJavaProtocol.makeMagnetometerMessage(data));
                    }
                    if(mSecureServer != null) {
                        mSecureServer.writeOutput(NeoJavaProtocol.makeMagnetometerMessage(data));
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
                    if(mServer != null) {
                        mServer.writeOutput(NeoJavaProtocol.makeGyroscopeMessage(data));
                    }
                    if(mSecureServer != null) {
                        mSecureServer.writeOutput(NeoJavaProtocol.makeGyroscopeMessage(data));
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
        return mGpiosManager.getExportedGpios();
    }

    @Override
    public void onSerialPortWriteRequest(int b) {
        if(mPreferences.getBoolean(PREF_SERIAL_COM_ENABLE, true)) {
            try {
                mSerial.write(b);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSerialPortWriteRequest(byte[] bytes) {
        if(mPreferences.getBoolean(PREF_SERIAL_COM_ENABLE, true)) {
            try {
                mSerial.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSerialPortWriteRequest(String message) {
        if(mPreferences.getBoolean(PREF_SERIAL_COM_ENABLE, true)) {
            if (message != null) {
                try {
                    mSerial.println(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onStateChanged(int pinId, Gpio.PinState state) {
        if(DEBUG) {
            logger.info("\rGPIO_" + pinId + " state changed to: " + state);
            System.out.print("#:");
        }
        if(mServer != null) {
            mServer.writeOutput(NeoJavaProtocol.makePinStateMessage(pinId, state));
        }
        if(mSecureServer != null) {
            mSecureServer.writeOutput(NeoJavaProtocol.makePinStateMessage(pinId, state));
        }
    }

    @Override
    public void onModeChanged(int pinId, Gpio.PinMode mode) {
        if(DEBUG) {
            logger.info("\rGPIO_" + pinId + " mode changed to: " + mode);
            System.out.print("#:");
        }
        if(mServer != null) {
            mServer.writeOutput(NeoJavaProtocol.makePinModeMessage(pinId, mode));
        }
        if(mSecureServer != null) {
            mSecureServer.writeOutput(NeoJavaProtocol.makePinModeMessage(pinId, mode));
        }
    }

    @Override
    public void onExport(int pinId) {
        if(DEBUG) {
            logger.info("\rGPIO_" + pinId + " exported");
            System.out.print("#:");
        }
        if(mServer != null) {
            mServer.writeOutput(NeoJavaProtocol.makeExportMessage(onExportedGpiosRequest()));
        }
        if(mSecureServer != null) {
            mSecureServer.writeOutput(NeoJavaProtocol.makeExportMessage(onExportedGpiosRequest()));
        }
    }

    @Override
    public void onRelease(int pinId) {
        if(DEBUG) {
            logger.info("\rGPIO_" + pinId + " released");
            System.out.print("#:");
        }
        if(mServer != null) {
            mServer.writeOutput(NeoJavaProtocol.makeExportMessage(onExportedGpiosRequest()));
        }
        if(mSecureServer != null) {
            mSecureServer.writeOutput(NeoJavaProtocol.makeExportMessage(onExportedGpiosRequest()));
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
        if(mInitComplete && line.startsWith("0x")) {
            int code = Integer.decode(line);
            switch (code) {
                case 0x01:
                    if(!mPrintingTemperature) {
                        mPrintingTemperature = true;
                            (new Thread(new TemperatureReader(new TemperatureReaderCallBack(){

                            @Override
                            public void onRequestComplete(Float temp, Float pressure) {
                                String tempString = String.format("Temp: %.1fßC\nPres: %.1fkPa", temp, pressure);
                                try {
                                    if(mPreferences.getBoolean(PREF_LCD_ENABLE, true) && !mLcdPrinting){
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
                                    logger.warn("\rError: " + e.getMessage());
                                    System.out.print("#:");
                                }
                            }
                        }))).start();
                    }
                    break;
            }
        }
        else if(mInitComplete) {
            System.out.printf("\rSerial OUT: %s\n", line.trim());
            System.out.print("#:");
        }
        else{
            logger.warn("\rError: system init not completed");
            System.out.print("#:");
        }
    }

}
