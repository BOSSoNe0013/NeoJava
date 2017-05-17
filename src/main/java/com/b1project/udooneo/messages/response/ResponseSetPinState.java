package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.Pin;
import com.b1project.udooneo.net.NeoJavaProtocol;

@SuppressWarnings("unused")
public class ResponseSetPinState extends ResponseMessage {

	private Pin pin;
	
	public ResponseSetPinState(String info, Pin pin) {
		super(NeoJavaProtocol.RESP_SET_PIN_STATE,info);
		this.pin = pin;
	}

	public Pin getPin() {
		return pin;
	}

	public void setPin(Pin pin) {
		this.pin = pin;
	}
}
