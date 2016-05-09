package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.Pin;
import com.b1project.udooneo.net.NeoJavaProtocol;

public class ResponseSetPinState extends ResponseMessage {

	public Pin pin;
	
	public ResponseSetPinState(String info, Pin pin) {
		super(NeoJavaProtocol.RESP_SET_PIN_STATE,info);
		this.pin = pin;
	}

}
