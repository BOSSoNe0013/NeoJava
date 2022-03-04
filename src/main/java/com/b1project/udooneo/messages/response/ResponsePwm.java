package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.net.NeoJavaProtocol;

@SuppressWarnings("unused")
public class ResponsePwm extends ResponseMessage {

	private long value;

	public ResponsePwm(String info, long value) {
		super(NeoJavaProtocol.RESP_PWM_VALUE,info);
		this.value = value;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}
}
