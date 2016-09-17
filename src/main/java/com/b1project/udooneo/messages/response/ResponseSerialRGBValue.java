package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.net.NeoJavaProtocol;

public class ResponseSerialRGBValue extends ResponseMessage {

	public String value;

	public ResponseSerialRGBValue(String info, String value) {
		super(NeoJavaProtocol.RESP_SERIAL_RGB_VALUE,info);
		this.value = value;
	}

}
