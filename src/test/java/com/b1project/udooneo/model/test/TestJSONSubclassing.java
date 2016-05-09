package com.b1project.udooneo.model.test;

import java.util.ArrayList;
import java.util.List;

import com.b1project.udooneo.gpio.Gpio.PinMode;
import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.messages.response.ResponseExportGpios;
import com.b1project.udooneo.messages.response.ResponseSetPinMode;
import com.b1project.udooneo.model.Pin;
import com.b1project.udooneo.net.NeoJavaProtocol;

class TestJSONSubclassing {
	
	public static void main(String[] args) {

        ResponseMessage m = new ResponseSetPinMode("OK", new Pin(1,PinMode.OUTPUT));
        serializeAndDeserialize(m);
        
        List<Pin> gpios = new ArrayList<>();
        gpios.add(new Pin(1));
		m = new ResponseExportGpios("OK", gpios);
        serializeAndDeserialize(m);
        
    }

	private static void serializeAndDeserialize(ResponseMessage m) {
		String s = NeoJavaProtocol.toJson(m);
        System.out.println(s);

        m =  NeoJavaProtocol.fromJson(s, ResponseMessage.class);
        System.out.println("Deserialized: "+m);
	}
}
