package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.SensorData;
import com.b1project.udooneo.net.NeoJavaProtocol;

public class ResponseAccelerometerData extends ResponseMessage {

	public SensorData content;
	
	public ResponseAccelerometerData(String info, SensorData sensorData) {
		super(NeoJavaProtocol.RESP_ACCELEROMETER,info);
		this.content = sensorData;
	}

}
