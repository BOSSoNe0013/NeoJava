package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.Pin;
import com.b1project.udooneo.net.NeoJavaProtocol;

import java.util.List;

public class ResponsePwm extends ResponseMessage {

	public long value;

	public ResponsePwm(String info, long value) {
		super(NeoJavaProtocol.RESP_PWM_VALUE,info);
		this.value = value;
	}

}
