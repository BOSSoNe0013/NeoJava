package com.b1project.udooneo.sensors.reader;

import com.b1project.udooneo.sensors.GyroscopeSensor;
import com.b1project.udooneo.sensors.callback.GyroscopeReaderCallBack;

public class GyroscopeReader implements Runnable{
    GyroscopeReaderCallBack callBack;

    public GyroscopeReader(GyroscopeReaderCallBack callBack){
        this.callBack = callBack;
    }

    @Override
    public void run() {
        try {
            if(!GyroscopeSensor.isEnabled()){
                GyroscopeSensor.enableSensor(true);
            }
            String data = GyroscopeSensor.getData();
            if(callBack != null){
                callBack.onRequestComplete(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}