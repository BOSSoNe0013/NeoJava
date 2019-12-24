package com.b1project.udooneo.serial;

import com.b1project.udooneo.NeoJava;
import com.b1project.udooneo.listeners.SerialOutputListener;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
@SuppressWarnings("unused")
public class Serial {
    private String mDeviceUri = NeoJava.DEFAULT_BINDING_TTY;
    private int mBaudRate = NeoJava.SERIAL_PORT_BAUD_RATE;
    private static OutputStream mOutputStream;
    private static InputStream mInputStream;
    private SerialOutputListener mListener;
    private SerialPort mSerialPort;
    private SerialReader mReaderTask;

    public Serial(String deviceUri, SerialOutputListener listener){
        super();
        if(deviceUri != null) {
            this.mDeviceUri = deviceUri;
        }
        this.mListener = listener;
    }

    public void setBaudRate(int baudRate) {
        this.mBaudRate = baudRate;
    }

    public int getBaudRate() {
        return mBaudRate;
    }

    public void connect() throws Exception {
        System.out.println("\rConnecting to serial port...");
        System.out.print("#:");
        try {
            CommPortIdentifier mCommPortIdentifier = CommPortIdentifier.getPortIdentifier(mDeviceUri);
            if (mCommPortIdentifier.isCurrentlyOwned()) {
                System.err.println("\rError: Port currently in use");
                System.out.print("#:");
            } else {
                CommPort mCommPort = mCommPortIdentifier.open(this.getClass().getName(), 2000);
                if(mCommPort instanceof SerialPort) {
                    mSerialPort = (SerialPort) mCommPort;
                    mSerialPort.setSerialPortParams(mBaudRate,
                                                    SerialPort.DATABITS_8,
                                                    SerialPort.STOPBITS_1,
                                                    SerialPort.PARITY_NONE);
                    mSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                    mOutputStream = mSerialPort.getOutputStream();
                    mInputStream  = mSerialPort.getInputStream();
                    mReaderTask = new SerialReader(mInputStream, mListener);
                    (new Thread(mReaderTask)).start();

                    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                disconnect();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }));
                    System.out.println("\rSerial port connected");
                    System.out.print("#:");
                } else {
                    System.err.println("\rError: Only serial ports are handled");
                    System.out.print("#:");
                }
            }
        }
        catch (NoSuchPortException e) {
                System.err.println("\rError: No serial ports found");
                System.out.print("#:");
        }
    }

    public void disconnect() throws Exception{
        if(mSerialPort != null){
            System.out.println("\rDisconnecting from serial port...");
            System.out.print("#:");
            mReaderTask.cancel();
            mOutputStream.close();
            mInputStream.close();
            new Thread(){
                @Override
                public void run(){
                    mSerialPort.removeEventListener();
                    mSerialPort.close();
                    System.out.println("\rSerial port disconnected");
                    System.out.print("#:");
                }
            }.start();
        }
    }

    public void write(int b) throws IOException{
        if(mSerialPort != null){
            if(mOutputStream == null) {
                mOutputStream = mSerialPort.getOutputStream();
            }
            if(mOutputStream != null) {
               mOutputStream.write(b);
            }
            else{
                System.err.println("\rError: can't get output stream");
                System.out.print("#:");
            }
        }
    }

    public void write(byte[] buffer) throws IOException {
        if(mSerialPort != null){
            if(mOutputStream == null) {
                mOutputStream = mSerialPort.getOutputStream();
            }
            if(mOutputStream != null) {
               mOutputStream.write(buffer);
            }
            else{
                System.err.println("\rError: can't get output stream");
                System.out.print("#:");
            }
        }
    }

    public void print(String message) throws Exception{
        this.write(message.getBytes());
    }

    public void println(String message) throws Exception{
        this.write(message.concat("\r\n").getBytes());
    }

    public void print(long l) throws Exception{
        if(mSerialPort != null){
            if(mOutputStream == null) {
                mOutputStream = mSerialPort.getOutputStream();
            }
            if(mOutputStream != null) {
                byte[] b = new byte[8];
                int size = Long.SIZE / Byte.SIZE;
                for (int i = 0; i < size; ++i) {
                    b[i] = (byte) (l >> (size - i - 1 << 3));
                }
                mOutputStream.write(b);
            }
            else{
                System.err.println("\rError: can't get output stream");
                System.out.print("#:");
            }
        }
    }

}
