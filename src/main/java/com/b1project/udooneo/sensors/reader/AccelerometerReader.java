package com.b1project.udooneo.sensors.reader;

import com.b1project.udooneo.sensors.AccelerometerSensor;
import com.b1project.udooneo.sensors.callback.AccelerometerReaderCallBack;

@SuppressWarnings("unused")
public class AccelerometerReader implements Runnable{
    private final AccelerometerReaderCallBack callBack;

    public AccelerometerReader(AccelerometerReaderCallBack callBack){
        this.callBack = callBack;
    }

    @Override
    public void run() {
        try {
            if(!AccelerometerSensor.isEnabled()){
                AccelerometerSensor.enableSensor(true);
            }
            String data = AccelerometerSensor.getData();
            if(callBack != null){
                callBack.onRequestComplete(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}