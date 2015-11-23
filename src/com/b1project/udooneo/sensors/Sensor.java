package com.b1project.udooneo.sensors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Sensor {

    protected static String read(String uri) throws Exception{
        File file = new File(uri);
        FileReader fr = new FileReader(file.getAbsoluteFile());
        BufferedReader br = new BufferedReader(fr);
        String value = br.readLine();
        br.close();
        return value;
    }

}
