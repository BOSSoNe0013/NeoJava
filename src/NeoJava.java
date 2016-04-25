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

import com.b1project.udooneo.board.BoardInfo;
import com.b1project.udooneo.gpio.GpiosManager;
import com.b1project.udooneo.gpio.Pin;
import com.b1project.udooneo.listeners.GpiosManagerListener;
import com.b1project.udooneo.listeners.NeoJavaProtocolListener;
import com.b1project.udooneo.listeners.STDInputListener;
import com.b1project.udooneo.gpio.Gpio;
import com.b1project.udooneo.lcd.Lcd;
import com.b1project.udooneo.listeners.SerialOutputListener;
import com.b1project.udooneo.net.NeoJavaProtocol;
import com.b1project.udooneo.net.NeoJavaServer;
import com.b1project.udooneo.sensors.*;
import com.b1project.udooneo.serial.Serial;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class NeoJava implements SerialOutputListener, NeoJavaProtocolListener, GpiosManagerListener {

    private static Lcd mLcd;
    private static Serial mSerial;
    private static String mCurrentMessage = "Hello Java GPIO\nwith UDOO Neo !!";
    private final static String APP_NAME = "NeoJava Tools";
    private final static String VERSION = "0.0.2";
    private final static String INPUT_COMMAND_QUIT = "/q";
    private final static String INPUT_COMMAND_VERSION = "/v";
    private final static String INPUT_COMMAND_LCD_CLEAR = "/lc";
    private final static String INPUT_COMMAND_LCD_PRINT = "/lp";
    private final static String INPUT_COMMAND_TEMP_REQUEST = "/tp";
    private final static String INPUT_COMMAND_EXPORTED_GPIOS = "/gpios";
    private final static String INPUT_COMMAND_BOARD_ID = "/id";
    private final static String INPUT_COMMAND_BOARD_MODEL = "/model";
    private final static String INPUT_COMMAND_BOARD_NAME = "/name";
    private static boolean mLcdPrinting = false;
    private static NeoJava instance;
    private static NeoJavaServer server;
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

    public NeoJava() {
        super();
    }

    private static NeoJava getInstance(){
        if(instance == null){
            instance = new NeoJava();
        }
        return instance;
    }
    
    private static Lcd initLCD() throws Exception{
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
        mLcd.print(mCurrentMessage);
        Thread.sleep(1000);
        mLcd.clear();
        mLcd.write((char)0x01);
        mLcd.write((char)0x01);
        mLcd.write((char)0x01);
        return mLcd;
    }
    
    private static void setupSTDINListener(final STDInputListener listener) throws Exception{
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
                server = NeoJavaServer.getInstance(getInstance());
                server.startServer();
            }
        }.start();
    }
    
    public static void main(String[] args) {
        try{
            System.out.print(getInstance().getVersionString());
            System.out.println("Java platform tools for UDOO Neo");
            gpiosManager = GpiosManager.getInstance();
            gpiosManager.addListener(getInstance());
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
            });
            System.out.println("Init complete");
            System.out.print("#:");
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }

    }

    private String handleLineInput(String line){
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
                getInstance().onLCDPrintRequest(getInstance().getVersionString() + "\n");
                System.out.print(getInstance().getVersionString() + "\n");
                System.out.print("#:");
                break;
            case INPUT_COMMAND_LCD_PRINT:
                mLcdPrinting = true;
                System.out.print(" ");
                break;
            case INPUT_COMMAND_TEMP_REQUEST:
                (new Thread(new TempReader(new TemperatureReaderCallBack(){

                    @Override
                    public void onRequestComplete(Float temp, Float pressure) {
                        String tempString = String.format("Temp: %.1fßC\nPres: %.1fkPa", temp, pressure);
                        try {
                            mLcd.clear();
                            mLcd.print(tempString);
                            Thread.sleep(3000);
                            mLcd.clear();
                            mLcd.print(mCurrentMessage);
                        }
                        catch (Exception e){
                            System.err.println(e.getMessage());
                        }
                    }
                }))).start();
                System.out.print("#:");
                break;
            case INPUT_COMMAND_EXPORTED_GPIOS:
                System.out.println(gpiosManager.getExportedGpios().toString());
                System.out.print("#:");
                break;
            case INPUT_COMMAND_BOARD_ID:
                System.out.println(BoardInfo.getBoardID());
                System.out.print("#:");
                break;
            case INPUT_COMMAND_BOARD_MODEL:
                System.out.println(BoardInfo.getBoardModel());
                System.out.print("#:");
                break;
            case INPUT_COMMAND_BOARD_NAME:
                System.out.println(BoardInfo.getBoardName());
                System.out.print("#:");
                break;
            default:
                if(mLcdPrinting) {
                    System.out.print(" ");
                    return line;
                }
                else{
                    if(!line.equals("")) {
                        System.out.println("Error: command not found");
                    }
                    System.out.print("#:");
                }
                break;
        }
        return null;
    }

    @Override
    public String getVersionString() {
        return String.format("%s %s", APP_NAME, VERSION);
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
            (new Thread(new TempReader(new TemperatureReaderCallBack() {
                @Override
                public void onRequestComplete(Float temp, Float pressure) {
                    if(server != null) {
                        server.writeOutput(
                                NeoJavaProtocol.makeRequest(
                                        NeoJavaProtocol.INPUT_COMMAND_TEMPERATURE_REQUEST,
                                        String.format(
                                                "{\\\"temp\\\":\\\"%f\\\", \\\"pressure\\\":\\\"%f\\\"}",
                                                temp,
                                                pressure
                                        )
                                )
                        );
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
                        server.writeOutput(
                                NeoJavaProtocol.makeRequest(
                                        NeoJavaProtocol.INPUT_COMMAND_ACCELEROMETER_REQUEST,
                                        String.format(
                                                "{\\\"data\\\":\\\"%s\\\"}",
                                                data
                                        )
                                )
                        );
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
                        server.writeOutput(
                                NeoJavaProtocol.makeRequest(
                                        NeoJavaProtocol.INPUT_COMMAND_MAGNETOMETER_REQUEST,
                                        String.format(
                                                "{\\\"data\\\":\\\"%s\\\"}",
                                                data
                                        )
                                )
                        );
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
                        server.writeOutput(
                                NeoJavaProtocol.makeRequest(
                                        NeoJavaProtocol.INPUT_COMMAND_GYROSCOPE_REQUEST,
                                        String.format(
                                                "{\\\"data\\\":\\\"%s\\\"}",
                                                data
                                        )
                                )
                        );
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
        System.out.println("GPIO_" + pinId + " state changed to: " + state);
        System.out.print("#:");
        if(server != null) {
            server.writeOutput(
                    NeoJavaProtocol.makeRequest(
                            "StateChanged",
                            String.format(
                                    "{\\\"pin\\\":\\\"%d\\\", \\\"state\\\":\\\"%d\\\"}",
                                    pinId,
                                    state.ordinal()
                            )
                    )
            );
        }
    }

    @Override
    public void onModeChanged(int pinId, Gpio.PinMode mode) {
        System.out.println("GPIO_" + pinId + " mode changed to: " + mode);
        System.out.print("#:");
        if(server != null) {
            server.writeOutput(
                    NeoJavaProtocol.makeRequest(
                            "ModeChanged",
                            String.format(
                                    "{\\\"pin\\\":\\\"%d\\\", \\\"mode\\\":\\\"%s\\\"}",
                                    pinId,
                                    (mode == Gpio.PinMode.INPUT)?"in":"out"
                            )
                    )
            );
        }
    }

    @Override
    public void onExport(int pinId) {
        System.out.println("GPIO_" + pinId + " exported");
        System.out.print("#:");
        if(server != null) {
            server.writeOutput(
                    NeoJavaProtocol.makeRequest(
                            INPUT_COMMAND_EXPORTED_GPIOS,
                            onExportedGpiosRequest().toString()
                    )
            );
        }
    }

    @Override
    public void onRelease(int pinId) {
        System.out.println("GPIO_" + pinId + " released");
        System.out.print("#:");
        if(server != null) {
            server.writeOutput(
                    NeoJavaProtocol.makeRequest(
                            NeoJavaProtocol.INPUT_COMMAND_EXPORTED_GPIOS,
                            onExportedGpiosRequest().toString()
                    )
            );
        }
    }

    private abstract class TemperatureReaderCallBack {
        public abstract void onRequestComplete(Float temp, Float pressure);
    }

    private static class TempReader implements Runnable{
        TemperatureReaderCallBack callBack;

        TempReader(TemperatureReaderCallBack callBack){
            this.callBack = callBack;
        }

        @Override
        public void run() {
            try {
                Float temp = TemperatureSensor.getTemperature();
                Float pressure = BarometerSensor.getPressure();
                if(callBack != null){
                    callBack.onRequestComplete(
                            temp,
                            pressure
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private abstract class AccelerometerReaderCallBack {
        public abstract void onRequestComplete(String data);
    }

    private static class AccelerometerReader implements Runnable{
        AccelerometerReaderCallBack callBack;

        AccelerometerReader(AccelerometerReaderCallBack callBack){
            this.callBack = callBack;
        }

        @Override
        public void run() {
            try {
                if(!AccelerometerSensor.isEnabled()){
                    AccelerometerSensor.enableSensor(true);
                }
                String data = AccelerometerSensor.getData();
                if(callBack != null){
                    callBack.onRequestComplete(
                            data
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private abstract class MagnetometerReaderCallBack {
        public abstract void onRequestComplete(String data);
    }

    private static class MagnetometerReader implements Runnable{
        MagnetometerReaderCallBack callBack;

        MagnetometerReader(MagnetometerReaderCallBack callBack){
            this.callBack = callBack;
        }

        @Override
        public void run() {
            try {
                if(!MagnetometerSensor.isEnabled()){
                    MagnetometerSensor.enableSensor(true);
                }
                String data = MagnetometerSensor.getData();
                if(callBack != null){
                    callBack.onRequestComplete(
                            data
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private abstract class GyroscopeReaderCallBack {
        public abstract void onRequestComplete(String data);
    }

    private static class GyroscopeReader implements Runnable{
        GyroscopeReaderCallBack callBack;

        GyroscopeReader(GyroscopeReaderCallBack callBack){
            this.callBack = callBack;
        }

        @Override
        public void run() {
            try {
                if(!GyroscopeSensor.isEnabled()){
                    GyroscopeSensor.enableSensor(true);
                }
                String data = GyroscopeSensor.getData();
                if(callBack != null){
                    callBack.onRequestComplete(
                            data
                    );
                }
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
                    (new Thread(new TempReader(new TemperatureReaderCallBack(){

                    @Override
                    public void onRequestComplete(Float temp, Float pressure) {
                        String tempString = String.format("Temp: %.1fßC\nPres: %.1f", temp, pressure);
                        try {
                            mLcd.clear();
                            mLcd.print(tempString);
                            Thread.sleep(3000);
                            mLcd.clear();
                            mLcd.print(mCurrentMessage);
                        }
                        catch (Exception e){
                            System.err.println(e.getMessage());
                        }
                    }
                }))).start();
                    break;
            }
        }
        System.out.print("#:");
    }
}
