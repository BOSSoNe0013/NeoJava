package com.b1project.udooneo.messages;

public class Message {
	
	public String method;
    public String type;

    public Message(String method){
        this.method = method;
        type = this.getClass().getSimpleName();
    }
}
