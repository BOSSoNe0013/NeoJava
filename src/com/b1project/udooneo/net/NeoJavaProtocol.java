package com.b1project.udooneo.net;

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

import java.net.*;
import java.io.*;

public class NeoJavaProtocol {

    final static String INPUT_COMMAND_HELP = "/h";
    final static String INPUT_COMMAND_VERSION = "/v";
    final static String INPUT_COMMAND_QUIT = "/q";
    final static String INPUT_COMMAND_LCD_CLEAR = "/lc";
    final static String INPUT_COMMAND_LCD_PRINT = "/lp";
    final static String INPUT_COMMAND_TEMP_REQUEST = "/tp";
    private NeoJavaProtocolListener listener;
    private String message = "";
    private int messageLines = 0;
    final static int MAX_MESSAGE_LINES = 2;
    Socket clientSocket;

    private enum State{
        WAITING,
        WRITING,
        RUNNING
    }

    private State state = State.WAITING;

    public NeoJavaProtocol(Socket clientSocket, NeoJavaProtocolListener listener){
        super();
        this.listener = listener;
        this.clientSocket = clientSocket;
    }

    public String processInput(String input){
        String output = null;
        if(input != null){
            switch (input) {
                case INPUT_COMMAND_HELP:
                    output = "/h this help\n";
                    output += "/v show version\n";
                    output += "/q quit\n";
                    output += "/lc clear LCD\n";
                    output += "/lp print message on 16x2 LCD screen\n";
                    output += "/tp print temperature on LCD screen";
                    break;
                case INPUT_COMMAND_VERSION:
                    if(listener != null) {
                        output = listener.getVersionString();
                    }
                    break;
                case INPUT_COMMAND_LCD_CLEAR:
                    if(listener != null){
                        listener.onClearLCDRequest();
                    }
                    output = "#:";
                    break;
                case INPUT_COMMAND_QUIT:
                    if(listener != null){
                        listener.onQuitRequest(clientSocket);
                    }
                    output = "\nGoodbye !";
                    break;
                case INPUT_COMMAND_LCD_PRINT:
                    state = State.WRITING;
                    output = null;
                    break;
                case INPUT_COMMAND_TEMP_REQUEST:
                    if(listener != null){
                        listener.onTemperatureRequest();
                    }
                    output = "Displaying temperature";
                    break;
                default:
                    if(state == State.WRITING) {
                        message += input + "\n";
                        messageLines++;
                        if(messageLines == MAX_MESSAGE_LINES) {
                            if (listener != null) {
                                listener.onLCDPrintRequest(message);
                                state = State.WAITING;
                                messageLines = 0;
                                message = "";
                            }
                        }
                    }
                    else{
                        output = "Error: command not found\n";
                        output += "#:";
                    }
                    break;
            }
        }
        return output;
    }
}
