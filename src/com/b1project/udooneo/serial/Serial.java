package com.b1project.udooneo.serial;
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

import com.b1project.udooneo.listeners.SerialOutputListener;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

public class Serial {
    private String mDeviceUri = "/dev/ttyS0";
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

    public void connect() throws Exception {
        System.out.println("connecting to serial port...");
        CommPortIdentifier mCommPortIdentifier = CommPortIdentifier.getPortIdentifier(mDeviceUri);
        if (mCommPortIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use!");
        } else {
            CommPort mCommPort = mCommPortIdentifier.open(this.getClass().getName(), 2000);
            if(mCommPort instanceof SerialPort) {
                mSerialPort = (SerialPort) mCommPort;
                mSerialPort.setSerialPortParams(115200,
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
                System.out.println("Connected!");
            } else {
                System.out.println("Error: Only serial ports are handled by this example!");
            }
        }
    }

    public void disconnect() throws Exception{
        if(mSerialPort != null){
            System.out.println("disconnecting from serial port...");
            mReaderTask.cancel();
            mOutputStream.close();
            mInputStream.close();
            new Thread(){
                @Override
                public void run(){
                    mSerialPort.removeEventListener();
                    mSerialPort.close();
                    System.out.println("Disconnected!");
                }
            }.start();
        }
    }

    public void write(String message) throws Exception{
        if(mOutputStream != null){
            mOutputStream.write(message.getBytes());
        }
    }

    protected static class SerialReader implements Runnable {
        InputStream in;
        SerialOutputListener listener;
        private volatile boolean cancelled;

        public SerialReader(InputStream in, SerialOutputListener listener) {
            this.in = in;
            this.listener = listener;
        }

        public void cancel(){
            cancelled = true;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[7];
            int len;
            String stringReceived = "";
            try {
                while(!cancelled && (len = this.in.read(buffer)) > 0) {
                    stringReceived += new String(buffer, 0, len);
                    if (buffer[len-1] == '\n') {
                        if(listener != null){
                            listener.onNewLine(stringReceived);
                        }
                        stringReceived = "";
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
