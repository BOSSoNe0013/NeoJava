package com.b1project.udooneo.sensors.reader;

import com.b1project.udooneo.sensors.AccelerometerSensor;
import com.b1project.udooneo.sensors.callback.AccelerometerReaderCallBack;

/**
 *  Copyright (C) 2015 Cyril BOSSELUT <bossone0013@gmail.com>
 *
 *  This file is part of NeoJava Tools for UDOO Neo
 *
 *  NeoJava Tools for UDOO Neo is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This libraries are distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <<a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>>.
 *
 */

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