package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.SensorData;
import com.b1project.udooneo.net.NeoJavaProtocol;

@SuppressWarnings("unused")
public class ResponseMagnetometer extends ResponseMessage {

	private SensorData content;
	
	public ResponseMagnetometer(String info, SensorData sensorData) {
		super(NeoJavaProtocol.RESP_MAGNETOMETER, info);
		this.content = sensorData;
	}

	public SensorData getContent() {
		return content;
	}

	public void setContent(SensorData content) {
		this.content = content;
	}
}
