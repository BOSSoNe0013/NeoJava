package com.b1project.udooneo.sensors;

import com.b1project.udooneo.utils.FileUtils;

public class Sensor {

    static String read(String uri) throws Exception{
        return FileUtils.readFile(uri);
    }

}
