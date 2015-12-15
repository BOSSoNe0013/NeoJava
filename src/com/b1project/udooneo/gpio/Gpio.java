package com.b1project.udooneo.gpio;
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

import com.b1project.udooneo.listeners.GpioListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class Gpio {
    int id;
    String uri;
    static final String COMMON_GPIO_URI = "/sys/class/gpio/gpio";
    static final String EXPORT_GPIO_URI = "/sys/class/gpio/export";
    static final String RELEASE_GPIO_URI = "/sys/class/gpio/unexport";
    static final String GPIO_DIRECTION_PATH = "/direction";
    static final String GPIO_VALUE_PATH = "/value";
    private static List<GpioListener> mListeners = new ArrayList<>();
    private static HashMap<Integer, PinState> currentPinStates = new HashMap<>();
    private PinState currentPinState = PinState.LOW;
    private PinMode currentPinMode = PinMode.OUTPUT;

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
        if (!isExported(pinId)) {
            gpio.export();
        }
        gpio.getMode();
        return gpio;
    }

    public static void addListener(GpioListener listener){
        if(!mListeners.contains(listener)){
            mListeners.add(listener);
        }
    }

    public static void removeListener(GpioListener listener){
        if (mListeners.contains(listener)){
            mListeners.remove(listener);
        }
    }

    private static String mkGpioUri(int pinId){
        return COMMON_GPIO_URI + pinId;
    }

    public static boolean isExported(int pinId){
        File gpioDir = new File(mkGpioUri(pinId));
        return gpioDir.exists();
    }
        
    public Gpio(int pinId){
        super();
        id = pinId;
        uri = mkGpioUri(pinId);
     }
    
    public void export() throws Exception{
        File file = new File(EXPORT_GPIO_URI);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(id + "");
        bw.close();
        for(GpioListener listener: mListeners){
            listener.onExport(id);
        }
    }
    
    public void release() throws Exception{
        File file = new File(RELEASE_GPIO_URI);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(id + "");
        bw.close();
        for(GpioListener listener: mListeners){
            listener.onRelease(id);
        }
    }
    
    public void setMode(PinMode mode) throws Exception{
        File file = new File(this.uri + GPIO_DIRECTION_PATH);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(mode == PinMode.INPUT?"in":"out");
        bw.close();
        currentPinMode = mode;
        for(GpioListener listener: mListeners){
            listener.onModeChanged(id, mode);
        }
    }

    public PinMode getMode() throws Exception{
        File file = new File(this.uri + GPIO_DIRECTION_PATH);
        FileReader fr = new FileReader(file.getAbsoluteFile());
        BufferedReader br = new BufferedReader(fr);
        PinMode mode = (Objects.equals(br.readLine(), "in"))?PinMode.INPUT:PinMode.OUTPUT;
        br.close();
        currentPinMode = mode;
        return mode;
    }

    public void write(PinState state) throws Exception{
        File file = new File(this.uri + GPIO_VALUE_PATH);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(state == PinState.HIGH?"1":"0");
        bw.close();
        currentPinState = state;
        currentPinStates.put(this.id, currentPinState);
        for(GpioListener listener: mListeners){
            listener.onStateChanged(id, state);
        }
    }
    
    public PinState read() throws Exception{
        if (currentPinMode == PinMode.OUTPUT && currentPinStates.containsKey(this.id)){
            return currentPinStates.get(this.id);
        }
        File file = new File(this.uri + GPIO_VALUE_PATH);
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
    
