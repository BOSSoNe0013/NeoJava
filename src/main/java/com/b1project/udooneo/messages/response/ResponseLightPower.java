package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.LightPower;
import com.b1project.udooneo.net.NeoJavaProtocol;

@SuppressWarnings("unused")
public class ResponseLightPower extends ResponseMessage {

	private final LightPower content;

	public ResponseLightPower(String info, LightPower power) {
		super(NeoJavaProtocol.RESP_LIGHT_POWER,info);
		this.content = power;
	}

	public LightPower getLightPower() {
		return content;
	}
}
