package com.b1project.udooneo.serial;

import com.b1project.udooneo.NeoJava;
import com.b1project.udooneo.listeners.SerialOutputListener;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * Copyright (C) 2015 Cyril BOSSELUT <bossone0013@gmail.com>
 * <p>
 * This file is part of NeoJava examples for UDOO
 * <p>
 * NeoJava examples for UDOO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This libraries are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@SuppressWarnings("unused")
public class SimpleSerial {
    private String mDeviceUri = NeoJava.DEFAULT_BINDING_TTY;
    private final SerialOutputListener mListener;
    private SerialPort mSerialPort;
    private final StringBuilder readBuffer = new StringBuilder();

    public SimpleSerial(String deviceUri, SerialOutputListener listener){
        super();
        if(deviceUri != null) {
            this.mDeviceUri = deviceUri;
        }
        this.mListener = listener;
    }

    public void connect() throws Exception {
        System.out.println("\rConnecting to serial port " + mDeviceUri + "...");
        System.out.print("#:");
        mSerialPort = new SerialPort(mDeviceUri);
        boolean isOpen = mSerialPort.openPort();

        if(isOpen) {
            mSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            mSerialPort.setParams(SerialPort.BAUDRATE_115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            mSerialPort.addEventListener(new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent serialPortEvent) {
                    if (serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue() > 0) {
                        try {
                            readBuffer.append(new String(mSerialPort.readBytes()));
                        } catch (SerialPortException e) {
                            e.printStackTrace();
                        }
                        sendReadData();
                    }
                }
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    disconnect();
                } catch (Exception e) {
                    NeoJava.logger.warn("Error: " + e.getMessage());
                }
            }
        }));
        System.out.println("\rSerial port connected");
        System.out.print("#:");
    }

    private void sendReadData() {
        String bufferContent = readBuffer.toString();
        // if a terminator is configured
        String readTerminator = "\n";
        // consume chunks until terminator string is reached
        while (bufferContent.contains(readTerminator)) {
            int endOfTerminator = bufferContent.indexOf(readTerminator) + readTerminator.length();
            String chunk = bufferContent.substring(0, endOfTerminator);
            //remove this chunk of data from bufferContent
            bufferContent = bufferContent.substring(endOfTerminator);
            if(!chunk.trim().isEmpty() && !chunk.trim().equals("\n")) {
                mListener.onNewLine(chunk);
            }
        }
        // Clear the buffer for sent text
        readBuffer.setLength(0);
        readBuffer.append(bufferContent);
    }

    public void disconnect() {
        if(mSerialPort != null){
            System.out.println("\rDisconnecting from serial port...");
            System.out.print("#:");
            new Thread(){
                @Override
                public void run(){
                    try {
                        mSerialPort.removeEventListener();
                        mSerialPort.closePort();
                    } catch (SerialPortException e) {
                        NeoJava.logger.warn("Error: " + e.getMessage());
                    }
                    System.out.println("\rSerial port disconnected");
                    System.out.print("#:");
                }
            }.start();
        }
    }

    public void write(int b) throws Exception{
        mSerialPort.writeInt(b);
    }

    public void write(byte[] buffer) throws Exception{
        mSerialPort.writeBytes(buffer);
    }

    public void print(String message) throws Exception{
        mSerialPort.writeString(message);
    }

    public void println(String message) throws Exception{
        mSerialPort.writeString(message.concat("\r\n"));
    }
}