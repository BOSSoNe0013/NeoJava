package com.b1project.udooneo.sensors.reader;

import com.b1project.udooneo.sensors.MagnetometerSensor;
import com.b1project.udooneo.sensors.callback.MagnetometerReaderCallBack;

public class MagnetometerReader implements Runnable{
    private final MagnetometerReaderCallBack callBack;

    public MagnetometerReader(MagnetometerReaderCallBack callBack){
        this.callBack = callBack;
    }

    @Override
    public void run() {
        try {
            if(!MagnetometerSensor.isEnabled()){
                MagnetometerSensor.enableSensor(true);
            }
            String data = MagnetometerSensor.getData();
            if(callBack != null){
                callBack.onRequestComplete(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}