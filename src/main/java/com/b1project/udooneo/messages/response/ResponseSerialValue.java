package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.net.NeoJavaProtocol;

public class ResponseSerialValue extends ResponseMessage {

	public String value;

	public ResponseSerialValue(String info, String value) {
		super(NeoJavaProtocol.RESP_SERIAL_VALUE,info);
		this.value = value;
	}

}
