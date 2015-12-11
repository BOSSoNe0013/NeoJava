package com.b1project.udooneo.gpio;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Objects;

@SuppressWarnings("unused")
public class Gpio{
    int id;
    String uri;
    
    public enum PinState{
        LOW,
        HIGH
    }
    
    public enum PinMode{
        OUTPUT,
        INPUT
    }
    
    public static Gpio getInstance(int pinId) throws Exception{
        Gpio gpio = new Gpio(pinId);
        File gpioDir = new File(gpio.uri);
        if (!gpioDir.exists()) {
            gpio.export();
        }
        return gpio;
    }
        
    public Gpio(int pinId){
        super();
        id = pinId;
        uri = "/sys/class/gpio/gpio" + id;
     }
    
    public void export() throws Exception{
        File file = new File("/sys/class/gpio/export");
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(id + "");
        bw.close();
    }
    
    public void unexport() throws Exception{
        File file = new File("/sys/class/gpio/unexport");
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(id + "");
        bw.close();
    }
    
    public void setMode(PinMode mode) throws Exception{
        File file = new File(this.uri + "/direction");
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(mode == PinMode.INPUT?"in":"out");
        bw.close();
    }
    
    public void write(PinState state) throws Exception{
        File file = new File(this.uri + "/value");
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(state == PinState.HIGH?"1":"0");
        bw.close();
    }
    
    public PinState read() throws Exception{
        File file = new File(this.uri + "/value");
        FileReader fr = new FileReader(file.getAbsoluteFile());
        BufferedReader br = new BufferedReader(fr);
        PinState state = (Objects.equals(br.readLine(), "1"))?PinState.HIGH:PinState.LOW;
        br.close();
        return state;
    }
    
    public void high() throws Exception{
        //System.out.println("Set 1 on GPIO" + this.id);
        this.write(PinState.HIGH);
    }
    
    public void low() throws Exception{
        //System.out.println("Set 0 on GPIO" + this.id);
        this.write(PinState.LOW);
    }
    
}
    
