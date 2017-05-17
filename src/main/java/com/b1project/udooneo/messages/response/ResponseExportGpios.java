package com.b1project.udooneo.messages.response;

import java.util.List;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.Pin;
import com.b1project.udooneo.net.NeoJavaProtocol;

@SuppressWarnings("unused")
public class ResponseExportGpios extends ResponseMessage {

	private List<Pin> content;
	
	public ResponseExportGpios(String info, List<Pin> gpios) {
		super(NeoJavaProtocol.RESP_GPIOS_EXPORT,info);
		this.content = gpios;
	}

	public List<Pin> getContent() {
		return content;
	}

	public void setContent(List<Pin> content) {
		this.content = content;
	}
}
