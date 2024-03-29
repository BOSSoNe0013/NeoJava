package com.b1project.udooneo.sensors;

import com.b1project.udooneo.utils.FileUtils;

import static java.lang.Thread.sleep;

/**
 * Copyright (C) 2017 Cyril BOSSELUT <bossone0013@gmail.com>
 * <p>
 * This file is part of NeoJava
 * <p>
 * NeoJava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This libraries are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <<a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>>.
 */
public class LightSensor extends Sensor {

    private final static int A_TIME = 400;
    private final static int A_GAIN = 16;

    public static float getLightPower(){
        try {
            //Float light_power_scale = Float.parseFloat(read(FileUtils.LIGHT_SCALE_URI));
            float raw_light_power_0 = Float.parseFloat(read(FileUtils.LIGHT_RAW_URI));
            sleep(A_TIME);
            float raw_light_power_1 = Float.parseFloat(read(FileUtils.LIGHT_RAW_URI));
            return calculateLightPower(raw_light_power_0, raw_light_power_1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0f;
    }

    private static float calculateLightPower(float ch0, float ch1) {
        float cpl = (float) (A_TIME * A_GAIN / 200);
        float lux1 = (float) ((ch0 - 1.5 * ch1) / cpl);
        float lux2 = (float) ((0.4 * ch0 - 0.48 * ch1) / cpl);
        return Math.max(lux1, lux2);
    }
}
