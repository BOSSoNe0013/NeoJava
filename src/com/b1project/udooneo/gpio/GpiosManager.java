package com.b1project.udooneo.gpio;

import com.b1project.udooneo.listeners.GpioListener;
import com.b1project.udooneo.listeners.GpiosManagerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
 * <p/>
 * This file is part of NeoJava Tools for UDOO Neo
 * <p/>
 * NeoJava Tools for UDOO Neo is free software: you can redistribute it and/or modify
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

public class GpiosManager implements GpioListener {

    public final static int GPIO_106 = 106;
    public final static int GPIO_107 = 107;
    public final static int GPIO_180 = 180;
    public final static int GPIO_181 = 181;
    public final static int GPIO_172 = 172;
    public final static int GPIO_173 = 173;
    public final static int GPIO_182 = 182;
    public final static int GPIO_24 = 24;
    public final static int GPIO_25 = 25;
    public final static int GPIO_22 = 22;
    public final static int GPIO_14 = 14;
    public final static int GPIO_15 = 15;
    public final static int GPIO_16 = 16;
    public final static int GPIO_17 = 17;
    public final static int GPIO_18 = 18;
    public final static int GPIO_19 = 19;
    public final static int GPIO_20 = 20;
    public final static int GPIO_21 = 21;
    public final static int GPIO_203 = 203;
    public final static int GPIO_202 = 202;
    public final static int GPIO_177 = 177;
    public final static int GPIO_176 = 176;
    public final static int GPIO_175 = 175;
    public final static int GPIO_174 = 174;
    public final static int GPIO_119 = 119;
    public final static int GPIO_124 = 124;
    public final static int GPIO_127 = 127;
    public final static int GPIO_116 = 116;
    public final static int GPIO_7 = 7;
    public final static int GPIO_6 = 6;
    public final static int GPIO_5 = 5;
    public final static int GPIO_4 = 4;

    static final int[] GPIOS = {GPIO_106, GPIO_107, GPIO_180, GPIO_181, GPIO_172, GPIO_173, GPIO_182, GPIO_24, GPIO_25,
            GPIO_22, GPIO_14, GPIO_15, GPIO_16, GPIO_17, GPIO_18, GPIO_19, GPIO_20, GPIO_21, GPIO_203, GPIO_202,
            GPIO_177, GPIO_176, GPIO_175, GPIO_174, GPIO_119, GPIO_124, GPIO_127, GPIO_116, GPIO_7, GPIO_6, GPIO_5, GPIO_4};

    private static List<Integer> mExportedGpios = new ArrayList<>();
    private static List<GpiosManagerListener> mListeners = new ArrayList<>();

    public GpiosManager(){

    }

    public static GpiosManager getInstance(){
        GpiosManager manager = new GpiosManager();
        Gpio.addListener(manager);
        manager.checkGpiosExportStatus();
        return manager;
    }

    public void addListener(GpiosManagerListener listener){
        if(!mListeners.contains(listener)){
            mListeners.add(listener);
        }
    }

    public void removeListener(GpiosManagerListener listener){
        if (mListeners.contains(listener)){
            mListeners.remove(listener);
        }
    }

    private void checkGpiosExportStatus(){
        for(int pinId: GPIOS){
            if(Gpio.isExported(pinId)){
                if(!mExportedGpios.contains(pinId)) {
                    mExportedGpios.add(pinId);
                }
            }
            else{
                if(mExportedGpios.contains(pinId)) {
                    mExportedGpios.remove(mExportedGpios.indexOf(pinId));
                }
            }
        }
    }

    @Override
    public void onStateChanged(int pinId, Gpio.PinState state) {
        for(GpiosManagerListener listener: mListeners){
            listener.onStateChanged(pinId, state);
        }
    }

    @Override
    public void onModeChanged(int pinId, Gpio.PinMode mode) {
        for(GpiosManagerListener listener: mListeners){
            listener.onModeChanged(pinId, mode);
        }
    }

    @Override
    public void onExport(int pinId) {
        mExportedGpios.add(pinId);
        for(GpiosManagerListener listener: mListeners){
            listener.onExport(pinId);
        }
    }

    @Override
    public void onRelease(int pinId) {
        mExportedGpios.remove(mExportedGpios.indexOf(pinId));
        for(GpiosManagerListener listener: mListeners){
            listener.onRelease(pinId);
        }
    }

    public List<Pin> getExportedGpios(){
        List<Pin> gpios = new ArrayList<>();
        for(Integer pinId: mExportedGpios){
            Pin pin = new Pin(pinId);
            try {
                pin.setMode(Gpio.getInstance(pinId).getMode());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                pin.setState(Gpio.getInstance(pinId).read());
            } catch (Exception e) {
                e.printStackTrace();
            }
            gpios.add(pin);
        }
        return gpios;
    }
}
