package com.b1project.udooneo.ir;

import com.b1project.udooneo.gpio.Gpio;
import com.b1project.udooneo.gpio.GpiosManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
 * <p>
 * This file is part of NeoJava examples for UDOO
 * <p>
 * NeoJava examples for UDOO is free software: you can redistribute it and/or modify
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
public class GpioIrReceiver {
    private Gpio mDataPin;
    private final static int MAX_PULSE = 1000;
    private final static int MAX_FRAMES = 64;
    private final static int DELAY = 4;
    private final static int FUZZY = 20;
    private final static float MAX_FAIL = 0.1f;
    private int[][] pulses = new int[MAX_FRAMES][2];
    private int[][] filteredPulses = new int[MAX_FRAMES][2];

    public GpioIrReceiver() throws Exception{
        super();
        mDataPin = Gpio.getInstance(GpiosManager.GPIO_177);
        mDataPin.setMode(Gpio.PinMode.INPUT);
        (new Thread(new GpioReader())).start();
    }

    private class GpioReader implements Runnable{

        @Override
        public void run() {
            try {
                while (true) {
                    int captured = captureFrames();
                    //printFrame(captured, pulses);
                    /*int[][] filtered = filterFrame(captured, IRCodes.POWER_BUTTON);
                    printFrame(filtered.length, filtered);*/
                    if (captured > 10 && compareFrames(captured, IRCodes.POWER_BUTTON)) {
                        System.out.println("\rPower button detected");
                        System.out.print("#:");
                    }
                    //sleep between two captures
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int captureFrames(){
        int currentPulse = 0;
        Gpio.PinState currentState = Gpio.PinState.HIGH;
        try {
            while (true) {
                int highPulse = 0;
                int lowPulse = 0;
                if(currentPulse >= MAX_FRAMES){
                    return currentPulse;
                }
                while (currentState == Gpio.PinState.HIGH) {
                    //pin state remains HIGH
                    lowPulse++;
                    // wait 4µs
                    TimeUnit.MICROSECONDS.sleep(DELAY);
                    currentState = mDataPin.read();
                    if ((lowPulse >= MAX_PULSE && currentPulse > 0) || currentPulse >= MAX_FRAMES) {
                        // don't record as IR is in idle mode
                        return currentPulse;
                    }
                }
                pulses[currentPulse][1] = lowPulse;
                while (currentState == Gpio.PinState.LOW) {
                    //pin state remains LOW
                    highPulse++;
                    // wait 4µs
                    TimeUnit.MICROSECONDS.sleep(DELAY);
                    currentState = mDataPin.read();
                    if ((highPulse >= MAX_PULSE && currentPulse > 0) || currentPulse >= MAX_FRAMES) {
                        // there's certainly a mistake as sensor idle state is HIGH
                        return currentPulse;
                    }
                }
                pulses[currentPulse][0] = highPulse;
                /*System.out.println("\r" + currentPulse + ": " + highPulse + ", " + lowPulse);
                System.out.print("#:");*/
                currentPulse++;
            }
        }catch(Exception e){
            System.err.println("Error while recording pulse @" + currentPulse);
            e.printStackTrace();
            System.out.print("#:");
        }
        return currentPulse;
    }

    private boolean compareFrames(int max, int[] refFrame) {
        //System.out.println("\rGot " + max + " pulses to analyze");
        //System.out.print("#:");
        int fail = 0;
        for(int i = 1; i < max - 1; i++) {
            int highCode = pulses[i][0] * DELAY;
            int lowCode = pulses[i][1] * DELAY;

            if(i*2 + 1 >= refFrame.length){
                break;
            }

            //use a FUZZY matching
            if (Math.abs((highCode - refFrame[i * 2])/ 10) > (refFrame[i * 2] * FUZZY / 10)) {
                //System.out.println("\r[HIGH] need " + refFrame[i * 2] + ", got " + highCode);
                //System.out.print("#:");
                fail++;
            }
            if (Math.abs((lowCode - refFrame[i * 2 + 1])/ 10) > (refFrame[i * 2 + 1] * FUZZY / 10)) {
                //System.out.println("\r[LOW] need " + refFrame[i * 2 + 1] + ", got " + lowCode);
                //System.out.print("#:");
                fail++;
            }
        }
        return fail < max*MAX_FAIL;
    }

    private int[][] filterFrame(int max, int[] refFrame){
        System.out.println("\rGot " + max + " pulses to analyze");
        System.out.print("#:");
        List<int[]> filteredFrames = new ArrayList<>();
        for(int i = 0; i < max - 1; i++) {
            int highCode = pulses[i][0] * DELAY / 10;
            int lowCode = pulses[i][1] * DELAY / 10;

            if(i*2 >= refFrame.length){
                break;
            }

            //use a FUZZY matching
            if (Math.abs(highCode - refFrame[i]) > (refFrame[i*2] * FUZZY / 100)) {
                continue;
            }
            if (Math.abs(lowCode - refFrame[i + 1]) > (refFrame[i*2 + 1] * FUZZY / 100)) {
                continue;
            }
            filteredFrames.add(pulses[i]);
        }
        int[][] ff = new int[filteredFrames.size()][2];
        filteredFrames.toArray(ff);
        return ff;
    }

    private void printFrame(int max, int[][] frame){
        System.out.println("\r[HIGH], [LOW]");
        for(int i = 0; i < max - 1; i++){
            System.out.println(frame[i][0] + ", " + frame[i][1] + ", ");
        }
        System.out.print("#:");
    }
}
