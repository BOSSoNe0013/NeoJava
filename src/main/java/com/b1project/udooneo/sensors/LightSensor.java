package com.b1project.udooneo.sensors;

import com.b1project.udooneo.utils.FileUtils;

/**
 * Copyright (C) 2017 Cyril Bosselut <bossone0013@gmail.com>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class LightSensor extends Sensor {

    public static Float getLightPower(){
        try {
            Float raw_light_power = Float.parseFloat(read(FileUtils.LIGHT_RAW_URI));
            Float light_power_scale = Float.parseFloat(read(FileUtils.LIGHT_SCALE_URI));
            return raw_light_power / light_power_scale;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0f;
    }
}
