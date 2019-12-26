package com.b1project.udooneo.gpio;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.b1project.udooneo.listeners.GpioListener;
import com.b1project.udooneo.utils.FileUtils;

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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class Gpio {
    private int id;
    private String uri;
    private static final String GPIO_DIRECTION_PATH = "/direction";
    private static final String GPIO_VALUE_PATH = "/value";
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

    static void addListener(GpioListener listener){
        if(!mListeners.contains(listener)){
            mListeners.add(listener);
        }
    }

    public static void removeListener(GpioListener listener){
        mListeners.remove(listener);
    }

    static String mkGpioUri(int pinId){
        return FileUtils.COMMON_GPIO_URI + pinId;
    }

    static boolean isExported(int pinId){
        File gpioDir = new File(mkGpioUri(pinId));
        return gpioDir.exists();
    }
        
    public Gpio(int pinId){
        super();
        id = pinId;
        uri = mkGpioUri(pinId);
     }
    
    private void export() throws Exception{
        File file = new File(FileUtils.EXPORT_GPIO_URI);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(id + "");
        bw.close();
        for(GpioListener listener: mListeners){
            listener.onExport(id);
        }
    }
    
    public void release() throws Exception{
        if(isExported(id)) {
            File file = new File(FileUtils.RELEASE_GPIO_URI);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(id + "");
            bw.close();
        }
        for(GpioListener listener: mListeners){
            listener.onRelease(id);
        }
    }
    
    public void setMode(PinMode mode) throws Exception{
        File file = new File(this.uri + GPIO_DIRECTION_PATH);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(mode == PinMode.INPUT ? "in" : "out");
        bw.close();
        currentPinMode = mode;
        for(GpioListener listener: mListeners){
            listener.onModeChanged(id, mode);
        }
    }

    public PinMode getMode() throws Exception{
        PinMode mode = (Objects.equals(FileUtils.readFile(this.uri + GPIO_DIRECTION_PATH), "in"))? PinMode.INPUT:PinMode.OUTPUT;
        currentPinMode = mode;
        return mode;
    }

    public void write(PinState state) throws Exception{
        File file = new File(this.uri + GPIO_VALUE_PATH);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(state == PinState.HIGH ? "1" : "0");
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
        PinState state = (Objects.equals(FileUtils.readFile(this.uri + GPIO_VALUE_PATH), "1"))?PinState.HIGH:PinState.LOW;
        currentPinState = state;
        currentPinStates.put(this.id, currentPinState);
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
    
