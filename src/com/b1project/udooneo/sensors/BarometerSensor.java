package com.b1project.udooneo.sensors;
/**
 *  Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class BarometerSensor extends Sensor {
    private  static String TEMP_RAW_URI = "/sys/class/i2c-dev/i2c-1/device/1-0060/iio:device0/in_temp_raw";
    private  static String TEMP_SCALE_URI ="/sys/class/i2c-dev/i2c-1/device/1-0060/iio:device0/in_temp_scale";
    private  static String PRESS_RAW_URI ="/sys/class/i2c-dev/i2c-1/device/1-0060/iio:device0/in_pressure_raw";
    private  static String PRESS_SCALE_URI ="/sys/class/i2c-dev/i2c-1/device/1-0060/iio:device0/in_pressure_scale";

    public static Float getPressure(){
        try {
            Float raw_pressure = Float.parseFloat(read(PRESS_RAW_URI));
            Float scale_pressure = Float.parseFloat(read(PRESS_SCALE_URI));
            return raw_pressure * scale_pressure;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0f;
    }

    public static Float getTemperature(){
        try {
            Float raw_pressure = Float.parseFloat(read(TEMP_RAW_URI));
            Float scale_pressure = Float.parseFloat(read(TEMP_SCALE_URI));
            return raw_pressure * scale_pressure;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0f;
    }
}
