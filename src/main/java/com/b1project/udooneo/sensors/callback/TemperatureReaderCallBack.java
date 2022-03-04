package com.b1project.udooneo.sensors.callback;

public interface TemperatureReaderCallBack {
    void onRequestComplete(Float temp, Float pressure);
}
