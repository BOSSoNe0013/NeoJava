package com.b1project.udooneo.sensors.reader;

import com.b1project.udooneo.sensors.BarometerSensor;
import com.b1project.udooneo.sensors.TemperatureSensor;
import com.b1project.udooneo.sensors.callback.TemperatureReaderCallBack;

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

public class TemperatureReader implements Runnable{
    private final TemperatureReaderCallBack callBack;

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
