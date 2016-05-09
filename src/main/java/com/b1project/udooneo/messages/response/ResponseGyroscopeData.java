package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.SensorData;
import com.b1project.udooneo.net.NeoJavaProtocol;

public class ResponseGyroscopeData extends ResponseMessage {

	public SensorData content;
	
	public ResponseGyroscopeData(String info, SensorData sensorData) {
		super(NeoJavaProtocol.RESP_GYROSCOPE,info);
		this.content = sensorData;
	}

}
