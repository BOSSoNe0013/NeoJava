package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.Pin;
import com.b1project.udooneo.net.NeoJavaProtocol;

public class ResponseSetPinMode extends ResponseMessage{

	public Pin pin;
	
	public ResponseSetPinMode(String info, Pin pin) {
		super(NeoJavaProtocol.RESP_SET_PIN_MODE,info);
		this.pin = pin;
	}

}
