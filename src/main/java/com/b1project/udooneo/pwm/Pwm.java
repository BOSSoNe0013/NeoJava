package com.b1project.udooneo.pwm;

import com.b1project.udooneo.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Objects;

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
@SuppressWarnings({"unused"})
public class Pwm {
    private int id;
    private String uri;
    private static final String PWM_PERIOD_PATH = "/period";
    private static final String PWM_DUTY_CyCLE_PATH = "/duty_cycle";
    private static final String PWM_ENABLE_PATH = "/enable";
    private static HashMap<Integer, PwmState> currentPwmStates = new HashMap<>();
    private PwmState currentPwmState = PwmState.DISABLE;

    public enum PwmState{
        DISABLE,
        ENABLE
    }

    public static Pwm getInstance(int pwmId) throws Exception{
        Pwm pwm = new Pwm(pwmId);
        if (!isExported(pwmId)) {
            pwm.export();
        }
        return pwm;
    }

    static String mkPwmUri(int pwmId){
        return FileUtils.COMMON_PWM_URI + "/pwm" + pwmId;
    }

    static boolean isExported(int pinId){
        File pwmDir = new File(mkPwmUri(pinId));
        return pwmDir.exists();
    }

    public Pwm(int pwmId){
        super();
        id = pwmId;
        uri = mkPwmUri(pwmId);
     }

    private void export() throws Exception{
        File file = new File(FileUtils.EXPORT_PWM_URI);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(id + "");
        bw.close();
    }

    private void release() throws Exception{
        File file = new File(FileUtils.RELEASE_PWM_URI);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(id + "");
        bw.close();
    }

    public void setPeriod(long period) throws Exception{
        File file = new File(this.uri + PWM_PERIOD_PATH);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(period + "");
        bw.close();
    }

    public long getPeriod() throws Exception{
        String periodString = FileUtils.readFile(this.uri + PWM_PERIOD_PATH);
        return Long.parseLong(periodString);
    }

    public void setDutyCycle(long dutyCycle) throws Exception{
        File file = new File(this.uri + PWM_DUTY_CyCLE_PATH);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(dutyCycle + "");
        bw.close();
    }

    public long getDutyCycle() throws Exception{
        String dutyCycleString = FileUtils.readFile(this.uri + PWM_DUTY_CyCLE_PATH);
        return Long.parseLong(dutyCycleString);
    }

    public void set8BitValue(int value) throws Exception{
        long period = this.getPeriod();
        long duty_cycle = (long)(period / 255) * value;
        this.setDutyCycle(duty_cycle);
    }

    public void setState(PwmState state) throws Exception{
        File file = new File(this.uri + PWM_ENABLE_PATH);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(state.ordinal() + "");
        bw.close();
        currentPwmState = state;
        currentPwmStates.put(this.id, currentPwmState);
    }

    public void enable() throws Exception{
        this.setState(PwmState.ENABLE);
    }

    public void disable() throws Exception{
        this.setState(PwmState.DISABLE);
    }

    public PwmState getState() throws Exception{
        PwmState state = (Objects.equals(FileUtils.readFile(this.uri + PWM_ENABLE_PATH), "1"))?PwmState.ENABLE:PwmState.DISABLE;
        currentPwmState = state;
        currentPwmStates.put(this.id, currentPwmState);
        return state;
    }

    public void configure(long period, long dutyCycle) throws Exception{
        this.setPeriod(period);
        this.setDutyCycle(dutyCycle);
        this.enable();
    }
}
