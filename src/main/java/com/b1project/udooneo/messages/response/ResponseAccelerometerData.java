package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.SensorData;
import com.b1project.udooneo.net.NeoJavaProtocol;

@SuppressWarnings("unused")
public class ResponseAccelerometerData extends ResponseMessage {

	private SensorData content;
	
	public ResponseAccelerometerData(String info, SensorData sensorData) {
		super(NeoJavaProtocol.RESP_ACCELEROMETER,info);
		this.content = sensorData;
	}

	public SensorData getContent() {
		return content;
	}

	public void setContent(SensorData content) {
		this.content = content;
	}
}
