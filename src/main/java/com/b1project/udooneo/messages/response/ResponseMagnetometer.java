package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.SensorData;
import com.b1project.udooneo.net.NeoJavaProtocol;

public class ResponseMagnetometer extends ResponseMessage {

	public SensorData content;
	
	public ResponseMagnetometer(String info, SensorData sensorData) {
		super(NeoJavaProtocol.RESP_MAGNETOMETER, info);
		this.content = sensorData;
	}

}
