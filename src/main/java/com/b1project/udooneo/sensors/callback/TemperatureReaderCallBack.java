package com.b1project.udooneo.sensors.callback;

public abstract class TemperatureReaderCallBack {
    public abstract void onRequestComplete(Float temp, Float pressure);
}
