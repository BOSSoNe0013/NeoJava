package com.b1project.udooneo.sensors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.b1project.udooneo.utils.FileUtils;

/**
 * Copyright (C) 2015 Cyril BOSSELUT <bossone0013@gmail.com>
 * <p/>
 * This file is part of NeoJava tools for UDOO
 * <p/>
 * NeoJava tools for UDOO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This libraries are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@SuppressWarnings({"SameParameterValue", "unused"})
public class AccelerometerSensor extends Sensor {

    public static void enableSensor(boolean enable) throws Exception{
        File file = new File(FileUtils.ACCELEROMETER_ACTIVATION_URI);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(enable?"1":"0");
        bw.close();
    }

    public static boolean isEnabled(){
        try {
            return Integer.parseInt(read(FileUtils.ACCELEROMETER_ACTIVATION_URI)) == 1;
        } catch (Exception e) {
            System.err.println("\rError: " + e.getMessage());
            System.out.print("#:");
        }
        return false;
    }

    public static String getData(){
        try {
            return read(FileUtils.ACCELEROMETER_DATA_URI);
        } catch (Exception e) {
            System.err.println("\rError: " + e.getMessage());
            System.out.print("#:");
        }
        return "0,0,0";
    }
}
