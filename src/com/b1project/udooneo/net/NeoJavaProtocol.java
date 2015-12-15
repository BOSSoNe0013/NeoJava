package com.b1project.udooneo.net;

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

import com.b1project.udooneo.gpio.Gpio;
import com.b1project.udooneo.gpio.Pin;
import com.b1project.udooneo.listeners.NeoJavaProtocolListener;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NeoJavaProtocol {

    public final static String INPUT_COMMAND_HELP = "help";
    public final static String INPUT_COMMAND_VERSION = "version";
    public final static String INPUT_COMMAND_QUIT = "quit";
    public final static String INPUT_COMMAND_LCD_CLEAR = "lcd/clear";
    public final static String INPUT_COMMAND_LCD_PRINT = "lcd/print";
    public final static String INPUT_COMMAND_TEMP_REQUEST = "sensors/temperature";
    public final static String INPUT_COMMAND_EXPORTED_GPIOS = "gpios/exported";
    public final static String INPUT_COMMAND_SET_GPIO_MODE = "gpios/mode";
    public final static String INPUT_COMMAND_SET_GPIO_STATE = "gpios/state";
    public final static String INPUT_COMMAND_RELEASE_GPIO = "gpios/release";

    private NeoJavaProtocolListener listener;
    Socket clientSocket;
    Gson gson;

    public NeoJavaProtocol(Socket clientSocket, NeoJavaProtocolListener listener){
        super();
        this.listener = listener;
        this.clientSocket = clientSocket;
        this.gson = new Gson();
    }

    public static String makeRequest(String method, String output){
        return String.format("{\"method\":\"%s\", \"output\":\"%s\"}", method, output);
    }

    public String processInput(String input){
        try {
            Message message = gson.fromJson(input, Message.class);
            String output = null;
            if (message.method != null && !message.method.isEmpty()) {
                System.out.println("\nRequest: " + message.method);
                System.out.print("Content: " + message.content + "\n#:");
                switch (message.method) {
                    case INPUT_COMMAND_HELP:
                        output = "help - this help\\n";
                        output += "version - show version\\n";
                        output += "quit - quit\\n";
                        output += "gpios/exported - \\n";
                        output += "gpios/mode - \\n";
                        output += "gpios/state - \\n";
                        output += "gpios/release - \\n";
                        output += "lcd/clear - clear LCD\\n";
                        output += "lcd/print - print message on 16x2 LCD screen\\n";
                        output += "sensors/temperature - print temperature on LCD screen";
                        break;
                    case INPUT_COMMAND_VERSION:
                        if (listener != null) {
                            output = listener.getVersionString();
                        }
                        break;
                    case INPUT_COMMAND_LCD_CLEAR:
                        if (listener != null) {
                            listener.onClearLCDRequest();
                            output = "OK";
                        }
                        break;
                    case INPUT_COMMAND_QUIT:
                        if (listener != null) {
                            listener.onQuitRequest(clientSocket);
                        }
                        output = "Goodbye !";
                        break;
                    case INPUT_COMMAND_LCD_PRINT:
                        if (listener != null) {
                            listener.onLCDPrintRequest((String) message.content);
                        }
                        output = "OK";
                        break;
                    case INPUT_COMMAND_TEMP_REQUEST:
                        if (listener != null) {
                            listener.onTemperatureRequest();
                        }
                        output = "Displaying temperature";
                        break;
                    case INPUT_COMMAND_EXPORTED_GPIOS:
                        List<Pin> gpios = new ArrayList<>();
                        if (listener != null) {
                            gpios = listener.onExportedGpiosRequest();
                        }
                        output = gpios.toString();
                        break;
                    case INPUT_COMMAND_SET_GPIO_MODE:
                        LinkedTreeMap linkedTreeMap = (LinkedTreeMap) message.content;
                        if(linkedTreeMap.containsKey("pinId")) {
                            int pinId = ((Double) linkedTreeMap.get("pinId")).intValue();
                            Gpio.PinMode mode = Gpio.PinMode.OUTPUT;
                            if (linkedTreeMap.get("mode").equals("in")) {
                                mode = Gpio.PinMode.INPUT;
                            }
                            Gpio gpio = Gpio.getInstance(pinId);
                            gpio.setMode(mode);
                            output = "OK";
                        }
                        else{
                            return makeRequest("ERROR", "invalid PIN ID: " + message.content);
                        }
                        break;
                    case INPUT_COMMAND_SET_GPIO_STATE:
                        LinkedTreeMap stateRequest = (LinkedTreeMap) message.content;
                        if(stateRequest.containsKey("pinId")) {
                            int pinId = ((Double) stateRequest.get("pinId")).intValue();
                            Gpio gpio = Gpio.getInstance(pinId);
                            if(gpio.getMode() == Gpio.PinMode.OUTPUT) {
                                gpio.write(((Double) stateRequest.get("state") == 1) ? Gpio.PinState.HIGH : Gpio.PinState.LOW);
                                output = "OK";
                            }
                            else{
                                return makeRequest("ERROR", "PIN is in INPUT mode, can't write value");
                            }
                        }
                        else{
                            return makeRequest("ERROR", "invalid PIN ID: " + message.content);
                        }
                        break;
                    case INPUT_COMMAND_RELEASE_GPIO:
                        LinkedTreeMap releaseRequest = (LinkedTreeMap) message.content;
                        if(releaseRequest.containsKey("pinId")) {
                            int pinId = ((Double) releaseRequest.get("pinId")).intValue();
                            Gpio gpio = Gpio.getInstance(pinId);
                            gpio.release();
                            output = "OK";
                        }
                        break;
                    default:
                        return makeRequest("ERROR", "Unknown method: " + message.method);
                }
                return makeRequest(message.method, output);
            }
            return makeRequest("ERROR", "Empty method: " + input.replace("\"", "\\\""));
        }
        catch (Exception e){
            System.err.println(e.getMessage());
            return makeRequest("ERROR", "invalid request: " + input.replace("\"", "\\\""));
        }
    }
}
