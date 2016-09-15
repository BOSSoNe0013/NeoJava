package com.b1project.udooneo.net;

import java.net.Socket;
import java.util.List;

import com.b1project.udooneo.NeoJava;
import com.b1project.udooneo.board.BoardInfo;

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
import com.b1project.udooneo.gpio.Gpio.PinState;
import com.b1project.udooneo.gpio.GpiosManager;
import com.b1project.udooneo.listeners.NeoJavaProtocolListener;
import com.b1project.udooneo.messages.Message;
import com.b1project.udooneo.messages.RequestMessage;
import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.messages.response.*;
import com.b1project.udooneo.model.Pin;
import com.b1project.udooneo.model.SensorData;
import com.b1project.udooneo.model.Temperature;
import com.b1project.udooneo.pwm.Pwm;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

public class NeoJavaProtocol {

	public final static String REQ_HELP = "help";
	public final static String REQ_VERSION = "version";
	public final static String REQ_QUIT = "quit";
	public static final String REQ_SERVER_ACTION = "message/server";
	public final static String REQ_SENSORS_TEMPERATURE = "sensors/temperature";
	public final static String REQ_SENSORS_MAGNETOMETER = "sensors/magnetometer";
	public final static String REQ_SENSORS_ACCELEROMETER = "sensors/accelerometer";
	public final static String REQ_SENSORS_GYROSCOPE = "sensors/gyroscope";
	public final static String REQ_GPIOS_EXPORT = "gpios/exported";
	public final static String REQ_GPIO_SET_MODE = "gpios/mode";
	public final static String REQ_GPIO_SET_STATE = "gpios/state";
	public final static String REQ_GPIO_RELEASE = "gpios/release";
	public final static String REQ_LCD_CLEAR = "lcd/clear";
	public final static String REQ_LCD_PRINT = "lcd/print";
	public static final String REQ_BOARD_ID = "board/id";
	public static final String REQ_BOARD_MODEL = "board/model";
	public static final String REQ_BOARD_NAME = "board/name";
	public static final String REQ_PWM_VALUE = "pwm/value";
	public static final String REQ_SERIAL_VALUE = "tty/value";
	public static final String REQ_SERIAL_RGB_VALUE = "tty/rgb";


	public static final String RESP_HELP = "resp/"+REQ_HELP;
	public static final String RESP_VERSION = "resp/"+REQ_VERSION;
	public final static String RESP_QUIT = "resp/"+REQ_QUIT;
	public static final String RESP_GPIOS_EXPORT = "resp/"+REQ_GPIOS_EXPORT;
	public static final String RESP_SET_PIN_MODE = "resp/"+REQ_GPIO_SET_MODE;
	public static final String RESP_SET_PIN_STATE = "resp/"+REQ_GPIO_SET_STATE;
	public static final String RESP_TEMPERATURE = "resp/"+REQ_SENSORS_TEMPERATURE;
	public static final String RESP_GYROSCOPE = "resp/"+REQ_SENSORS_GYROSCOPE;
	public static final String RESP_ACCELEROMETER = "resp/"+REQ_SENSORS_ACCELEROMETER;
	public static final String RESP_MAGNETOMETER = "resp/"+REQ_SENSORS_MAGNETOMETER;
	public static final String RESP_LCD_CLEAR = "resp/"+REQ_LCD_CLEAR;
	public static final String RESP_LCD_PRINT = "resp/"+REQ_LCD_PRINT;
	public static final String RESP_BOARD_ID = "resp/"+REQ_BOARD_ID;
	public static final String RESP_BOARD_MODEL = "resp/"+REQ_BOARD_MODEL;
	public static final String RESP_BOARD_NAME = "resp/"+REQ_BOARD_NAME;
	public static final String RESP_PWM_VALUE = "resp/"+REQ_PWM_VALUE;
	public static final String RESP_SERIAL_VALUE = "resp/"+REQ_SERIAL_VALUE;


	public static final String ERROR = "error";

	private NeoJavaProtocolListener listener;
	private Socket clientSocket;
        private final GpiosManager mGpiosManager;

	private static final RuntimeTypeAdapterFactory<Message> msgAdapter = RuntimeTypeAdapterFactory.of(Message.class)
			.registerSubtype(RequestMessage.class)
			.registerSubtype(ResponseMessage.class);
	
	private static final RuntimeTypeAdapterFactory<ResponseMessage> responseAdapter = RuntimeTypeAdapterFactory.of(ResponseMessage.class)
			.registerSubtype(ResponseOutputMessage.class)
			.registerSubtype(ResponseExportGpios.class)
			.registerSubtype(ResponseAccelerometerData.class)
			.registerSubtype(ResponseGyroscopeData.class)
			.registerSubtype(ResponseMagnetometer.class)
			.registerSubtype(ResponseSetPinMode.class)
			.registerSubtype(ResponseSetPinState.class)
			.registerSubtype(ResponseTemperature.class);

	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapterFactory(msgAdapter)
			.registerTypeAdapterFactory(responseAdapter)
			.create();
	
	public NeoJavaProtocol(Socket clientSocket, NeoJavaProtocolListener listener) {
		this.listener = listener;
		this.clientSocket = clientSocket;
        mGpiosManager = GpiosManager.getInstance();
	}

	public static String toJson(Object value) {
		return gson.toJson(value);
	}

	public static <T> T fromJson(String value, Class<T> clazz) {
		return gson.fromJson(value, clazz);
	}

	ResponseMessage processInput(String input) {
		if(NeoJava.DEBUG) {
			System.out.println("\r--------------------\n" + input);
			System.out.print("#:");
		}
		try {
			RequestMessage m = fromJson(input, RequestMessage.class);
			String output;
			String responseMethod = m.method;
			if (!m.method.isEmpty()) {
				switch (m.method) {
					case REQ_HELP:
						output = REQ_HELP + " - this help\\n";
						output += REQ_VERSION + " - show version\\n";
						output += REQ_QUIT + " - quit\\n";
						output += REQ_BOARD_ID + " - request board ID\\n";
						output += REQ_BOARD_MODEL + " - request board model\\n";
						output += REQ_BOARD_NAME + " - request board name\\n";
						output += REQ_GPIOS_EXPORT + " - list exported gpios\\n";
						output += REQ_GPIO_SET_MODE  + " - set gpio mode\\n";
						output += REQ_GPIO_SET_STATE + " - set gpio state\\n";
						output += REQ_GPIO_RELEASE + " - release gpio\\n";
						output += REQ_PWM_VALUE + " - set PWM value\\n";
						output += REQ_SERIAL_VALUE + " - write data to serial port\\n";
						output += REQ_SERIAL_RGB_VALUE + " - write comma separated rgb value to serial port\\n";
						output += REQ_LCD_CLEAR + " - clear LCD\\n";
						output += REQ_LCD_PRINT + " - print message on 16x2 LCD screen\\n";
						output += REQ_SENSORS_TEMPERATURE + " - request temperature and pressure\\n";
						output += REQ_SENSORS_ACCELEROMETER + " - request accelerometer data\\n";
						output += REQ_SENSORS_MAGNETOMETER + " - request magnetometer data\\n";
						output += REQ_SENSORS_GYROSCOPE + " - request gyroscope data";
						responseMethod = RESP_HELP;
						break;
					case REQ_VERSION:
						if (listener != null) {
							output = listener.getVersionString();
							responseMethod = RESP_VERSION;
						}
						else{
							output = "No board manager";
							responseMethod = ERROR;
						}
						break;
					case REQ_QUIT:
						if (listener != null) {
							listener.onQuitRequest(clientSocket);
							output = "Goodbye !";
							responseMethod = RESP_QUIT;
						}
						else{
							output = "No board manager";
							responseMethod = ERROR;
						}
						break;
					case REQ_BOARD_ID:
						output = BoardInfo.getBoardID();
						responseMethod = RESP_BOARD_ID;
						break;
					case REQ_BOARD_MODEL:
						output = BoardInfo.getBoardModel();
						responseMethod = RESP_BOARD_MODEL;
						break;
					case REQ_BOARD_NAME:
						output = BoardInfo.getBoardName();
						responseMethod = RESP_BOARD_NAME;
						break;
					case REQ_PWM_VALUE:
						long value = m.value;
						Pwm pwm = Pwm.getInstance(0);
						pwm.set8BitValue(value);
						return new ResponsePwm("OK", m.value);
					case REQ_GPIOS_EXPORT:
						if (listener != null) {
							List<Pin> gpios = listener.onExportedGpiosRequest();
							return new ResponseExportGpios("OK", gpios);
						}
						output = "No board manager";
						responseMethod = ERROR;
						break;
					case REQ_GPIO_SET_MODE:
						try {
							Gpio.PinMode mode = (m.mode != null)?m.mode: Gpio.PinMode.OUTPUT;
							Gpio gpio = mGpiosManager.getGpio(m.pinId);
							gpio.setMode(mode);
							output = "OK";
						} catch (Exception e) {
							output = "Invalid set GPIO mode: " + input;
							System.err.println("Error: " + e.getMessage());
							System.out.print("#:");
							responseMethod = ERROR;
						}
						break;
					case REQ_GPIO_SET_STATE:
						try {
							Gpio gpio = mGpiosManager.getGpio(m.pinId);
							if (gpio.getMode() == Gpio.PinMode.OUTPUT) {
								gpio.write(m.state);
								output = "OK";
							} else {
								output = "PIN is in INPUT mode, can't write value";
								responseMethod = ERROR;
							}
						} catch (Exception e) {
							output = "invalid set GPIO state: " + input;
							responseMethod = ERROR;
						}
						break;
					case REQ_GPIO_RELEASE:
						try {
							Gpio gpio = mGpiosManager.getGpio(m.pinId);
							gpio.release();
							output = "OK";
						} catch (Exception e) {
							output = "error during PIN release: " + e.getMessage();
							responseMethod = ERROR;
						}
						break;
                    case REQ_SERIAL_VALUE:
						if (listener != null) {
							listener.onSerialPortWriteRequest(m.detailMessage);
                            output = "OK";
                        }
                        else{
							output = "No sensor manager";
							responseMethod = ERROR;
                        }
                        break;
                    case REQ_SERIAL_RGB_VALUE:
						if (listener != null) {
							String[] rgb = m.detailMessage.split(",");
							int red = Integer.parseInt(rgb[0]);
							int green = Integer.parseInt(rgb[1]);
							int blue = Integer.parseInt(rgb[2]);
                            System.out.println("\r" + red + " " + green + " " + blue);
                            System.out.print("#:");
							listener.onSerialPortWriteRequest(255);
							listener.onSerialPortWriteRequest(red);
							listener.onSerialPortWriteRequest(green);
							listener.onSerialPortWriteRequest(blue);
                            output = "OK";
                        }
                        else{
							output = "No sensor manager";
							responseMethod = ERROR;
                        }
                        break;
					case REQ_SENSORS_TEMPERATURE:
						if (listener != null) {
							listener.onTemperatureRequest();
							output = "Reading temperature";
						}
						else{
							output = "No sensor manager";
							responseMethod = ERROR;
						}
						break;
					case REQ_SENSORS_ACCELEROMETER:
						if (listener != null) {
							listener.onAccelerometerRequest();
							output = "Reading accelerometer data";
						}
						else{
							output = "No sensor manager";
							responseMethod = ERROR;
						}
						break;
					case REQ_SENSORS_MAGNETOMETER:
						if (listener != null) {
							listener.onMagnetometerRequest();
							output = "Reading magnetometer data";
						}
						else{
							output = "No sensor manager";
							responseMethod = ERROR;
						}
						break;
					case REQ_SENSORS_GYROSCOPE:
						if (listener != null) {
							listener.onGyroscopeRequest();
							output = "Reading gyroscope data";
						}
						else{
							output = "No sensor manager";
							responseMethod = ERROR;
						}
						break;
					case REQ_LCD_CLEAR:
						if (listener != null) {
							listener.onClearLCDRequest();
							output = "OK";
							responseMethod = RESP_LCD_CLEAR;
						}
						else{
							output = "No LCD screen manager";
							responseMethod = ERROR;
						}
						break;
					case REQ_LCD_PRINT:
						if (listener != null) {
							listener.onLCDPrintRequest(m.detailMessage);
							output = "OK";
							responseMethod = RESP_LCD_PRINT;
						}
						else{
							output = "No LCD screen manager";
							responseMethod = ERROR;
						}
						break;
					default:
						output = "Unknown method: " + m.method;
						responseMethod = ERROR;
				}
				if(NeoJava.DEBUG) {
					System.out.println("\r" + output);
					System.out.print("#:");
				}
				return new ResponseOutputMessage(responseMethod, output);
			}
			return new ResponseOutputMessage(ERROR, "Empty method: " + input.replace("\"", "\\\""));
		} catch (Exception e) {
			System.err.println("\rError: " + e.getMessage());
			System.out.print("#:");
			return new ResponseOutputMessage(ERROR, "Invalid request: " + input);
		}
	}

	public static ResponseExportGpios makeExportMessage(List<Pin> gpios) {
		return new ResponseExportGpios("OK", gpios);
	}

	public static ResponseSetPinMode makePinModeMessage(int pinId, Gpio.PinMode mode) {
		return new ResponseSetPinMode("OK", new Pin(pinId, mode));
	}

	public static ResponseSetPinState makePinStateMessage(int pinId, PinState state) {
		return new ResponseSetPinState("OK", new Pin(pinId,state));
	}

	public static ResponseMagnetometer makeMagnetometerMessage(String sensorDataStr) {
		return new ResponseMagnetometer("OK", new SensorData(sensorDataStr));
	}

	public static ResponseAccelerometerData makeAccelerometerMessage(String sensorDataStr) {
		return new ResponseAccelerometerData("OK", new SensorData(sensorDataStr));
	}

	public static ResponseGyroscopeData makeGyroscopeMessage(String sensorDataStr) {
		return new ResponseGyroscopeData("OK", new SensorData(sensorDataStr));
	}

	public static ResponseTemperature makeTemperatureMessage(Float temp, Float pressure) {
		return new ResponseTemperature("OK", new Temperature(temp, pressure));
	}

	public static ResponseOutputMessage makeShutdownMessage() {
		return new ResponseOutputMessage(REQ_SERVER_ACTION, "shutdown");
	}

}
