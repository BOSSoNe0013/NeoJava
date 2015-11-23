package com.b1project.udooneo.sensors;
/**
 *  Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
 *
 *  This file is part of NeoJava examples for UDOO
 *
 *  NeoJava examples for UDOO is free software: you can redistribute it and/or modify
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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class TemperatureSensor extends Sensor {
    private  static String TEMP_URI = "/sys/class/i2c-dev/i2c-1/device/1-0048/temp1_input";

    public static Float getTemperature(){
        try {
            Float raw_pressure = Float.parseFloat(read(TEMP_URI));
            return raw_pressure / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0f;
    }
}
