package com.b1project.udooneo.messages;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Message {
	
	private String method;
    private String type;

    public Message(String method){
        this.method = method;
        this.type = this.getClass().getSimpleName();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
