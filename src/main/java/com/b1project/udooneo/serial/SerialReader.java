package com.b1project.udooneo.serial;

import com.b1project.udooneo.listeners.SerialOutputListener;

import java.io.IOException;
import java.io.InputStream;

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
class SerialReader implements Runnable{
    private final InputStream in;
    private final SerialOutputListener listener;
    private volatile boolean cancelled;

    SerialReader(InputStream in, SerialOutputListener listener) {
        this.in = in;
        this.listener = listener;
    }

    void cancel(){
        cancelled = true;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[7];
        int len;
        StringBuilder stringReceived = new StringBuilder();
        try {
            while(!cancelled && (len = this.in.read(buffer)) > 0) {
                stringReceived.append(new String(buffer, 0, len));
                if (buffer[len-1] == '\n') {
                    if(listener != null){
                        listener.onNewLine(stringReceived.toString());
                    }
                    stringReceived = new StringBuilder();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
