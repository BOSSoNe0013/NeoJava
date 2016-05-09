package com.b1project.udooneo.model;

public class Temperature {
	double temperature;
	double pressure;
	
	public Temperature(double temperature, double pressure) {
		this.temperature = temperature;
		this.pressure = pressure;
	}
	
	public double getTemperature() {
		return temperature;
	}
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	public double getPressure() {
		return pressure;
	}
	public void setPressure(double pressure) {
		this.pressure = pressure;
	}
	
	

}
