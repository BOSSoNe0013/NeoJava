package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.net.NeoJavaProtocol;

@SuppressWarnings("unused")
public class ResponseSerialValue extends ResponseMessage {

	private String value;

	public ResponseSerialValue(String info, String value) {
		super(NeoJavaProtocol.RESP_SERIAL_VALUE,info);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
