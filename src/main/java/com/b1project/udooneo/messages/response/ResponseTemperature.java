package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.Temperature;
import com.b1project.udooneo.net.NeoJavaProtocol;

public class ResponseTemperature extends ResponseMessage {

	public Temperature content;
	
	public ResponseTemperature(String info, Temperature temperature) {
		super(NeoJavaProtocol.RESP_TEMPERATURE,info);
		this.content = temperature;
	}

}
