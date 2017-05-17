package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.Temperature;
import com.b1project.udooneo.net.NeoJavaProtocol;

@SuppressWarnings("unused")
public class ResponseTemperature extends ResponseMessage {

	private Temperature content;
	
	public ResponseTemperature(String info, Temperature temperature) {
		super(NeoJavaProtocol.RESP_TEMPERATURE,info);
		this.content = temperature;
	}

	public Temperature getContent() {
		return content;
	}

	public void setContent(Temperature content) {
		this.content = content;
	}
}
