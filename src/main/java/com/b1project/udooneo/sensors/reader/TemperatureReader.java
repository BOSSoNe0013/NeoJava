package com.b1project.udooneo.sensors.reader;

import com.b1project.udooneo.sensors.BarometerSensor;
import com.b1project.udooneo.sensors.TemperatureSensor;
import com.b1project.udooneo.sensors.callback.TemperatureReaderCallBack;

public class TemperatureReader implements Runnable{
    TemperatureReaderCallBack callBack;

    public TemperatureReader(TemperatureReaderCallBack callBack){
        this.callBack = callBack;
    }

    @Override
    public void run() {
        try {
            Float temp = TemperatureSensor.getTemperature();
            Float pressure = BarometerSensor.getPressure();
            if(callBack != null){
                callBack.onRequestComplete(temp, pressure);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
