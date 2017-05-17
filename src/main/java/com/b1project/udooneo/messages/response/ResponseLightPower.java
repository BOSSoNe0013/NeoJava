package com.b1project.udooneo.messages.response;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.LightPower;
import com.b1project.udooneo.net.NeoJavaProtocol;

public class ResponseLightPower extends ResponseMessage {

	public LightPower content;

	public ResponseLightPower(String info, LightPower power) {
		super(NeoJavaProtocol.RESP_LIGHT_POWER,info);
		this.content = power;
	}

}
