package com.b1project.udooneo.pwm;

import com.b1project.udooneo.NeoJava;
import com.b1project.udooneo.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Objects;

/**
 * Copyright (C) 2015 Cyril BOSSELUT <bossone0013@gmail.com>
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
@SuppressWarnings({"unused", "WeakerAccess"})
public class Pwm {
    private final int id;
    private final String uri;
    private static final long PWM_DEFAULT_PERIOD = 10000;
    private static final String PWM_PERIOD_PATH = "/period";
    private static final String PWM_DUTY_CYCLE_PATH = "/duty_cycle";
    private static final String PWM_ENABLE_PATH = "/enable";
    private static final HashMap<Integer, Pwm> PWM_MAP = new HashMap<>();
    private static Pwm pwm;
    private static final HashMap<Integer, PwmState> currentPwmStates = new HashMap<>();
    private PwmState currentPwmState = PwmState.DISABLE;

    public enum PwmState{
        DISABLE,
        ENABLE
    }

    /**
     * @param pwmId int pwm id
     * @return Pwm instance
     * @throws Exception if Pwm instance cannot be retrieved or created
     */
    public static Pwm getInstance(int pwmId) throws Exception{
        Pwm pwm;
        if(PWM_MAP.containsKey(pwmId)) {
            pwm = PWM_MAP.get(pwmId);
        }
        else {
            pwm = new Pwm(pwmId);
            PWM_MAP.put(pwmId, pwm);
        }
        if (!isExported(pwmId)) {
            pwm.export();
        }
        pwm.enable();
        return pwm;
    }

    /**
     * @param pwmId int pwm id
     * @return String pwm uri
     */
    public static String mkPwmUri(int pwmId){
        return FileUtils.COMMON_PWM_URI  + pwmId;
    }

    /**
     * @param pwmId int pwm id
     * @return boolean returns true if pwm is exported
     */
    public static boolean isExported(int pwmId){
        File pwmDir = new File(mkPwmUri(pwmId) + "/pwm0");
        return pwmDir.exists();
    }

    /**
     * Constructor
     * @param pwmId int pwm id
     */
    protected Pwm(int pwmId){
        super();
        id = pwmId;
        uri = mkPwmUri(pwmId);
     }

    private void export() throws Exception{
        File file = new File(uri + FileUtils.DEVICE_EXPORT_ENDPOINT);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("1");
        bw.close();
        fw.close();
    }

    private void release() throws Exception{
        File file = new File(uri + FileUtils.DEVICE_UNEXPORT_ENDPOINT);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(id + "");
        bw.close();
        fw.close();
    }

    /**
     * @param period in nanoseconds
     * @throws Exception if period cannot be set (for example if period is smaller than current duty_cycle)
     */
    public void setPeriod(long period) throws Exception{
        File file = new File(this.uri + PWM_PERIOD_PATH);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(period + "");
        bw.close();
        fw.close();
    }

    /**
     * @return long Current period length in nanoseconds
     * @throws Exception if period cannot be read
     */
    public long getPeriod() throws Exception{
        String periodString = FileUtils.readFile(this.uri+ "/pwm0" + PWM_PERIOD_PATH);
        return Long.parseLong(periodString);
    }

    /**
     * @param dutyCycle in nanoseconds
     * @throws Exception if duty_cycle cannot be set (for example if current period is smaller than duty_cycle)
     */
    public void setDutyCycle(long dutyCycle) throws Exception{
        File file = new File(this.uri+ "/pwm0" + PWM_DUTY_CYCLE_PATH);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(dutyCycle + "");
        bw.close();
        fw.close();
    }

    /**
     * @return long Current duty_cycle length in nanoseconds
     * @throws Exception if duty_cycle cannot be read
     */
    public long getDutyCycle() throws Exception{
        String dutyCycleString = FileUtils.readFile(this.uri+ "/pwm0" + PWM_DUTY_CYCLE_PATH);
        return Long.parseLong(dutyCycleString);
    }

    /**
     * @param value int {0...255}
     * @throws Exception if value cannot be set (for example if value is not between 0 and 255 or is NaN)
     */
    public void set8BitValue(int value) throws Exception {
        if(value < 0){
            value = 0;
        }
        if(value > 255){
            value = 255;
        }
        long period = this.getPeriod();
        if (period == 0) {
            period = PWM_DEFAULT_PERIOD;
            setPeriod(PWM_DEFAULT_PERIOD);
        }
        long duty_cycle = period / 255 * value;
        if (NeoJava.DEBUG) {
            NeoJava.logger.debug(String.format("ID:%d", id));
            NeoJava.logger.debug(String.format("period:%d", period));
            NeoJava.logger.debug(String.format("duty cycle:%d", duty_cycle));
        }
        this.setDutyCycle(duty_cycle);
    }

    public long get8BitValue() throws Exception{
        long period = this.getPeriod();
        long duty_cycle = this.getDutyCycle();
        return Math.min(255, Math.round((float) (duty_cycle * 255) / period));
    }

    /**
     * @param value int {0...100}
     * @throws Exception if value cannot be set (for example if value is not between 0 and 100 or is NaN)
     */
    public void setPercentValue(int value) throws Exception{
        if(value < 0){
            value = 0;
        }
        if(value > 100){
            value = 100;
        }
        long period = this.getPeriod();
        if (period == 0) {
            period = PWM_DEFAULT_PERIOD;
            setPeriod(PWM_DEFAULT_PERIOD);
        }
        long duty_cycle = period / 100 * value;
        this.setDutyCycle(duty_cycle);
    }

    /**
     * @param state PwmState set pwm output state (enable/disable)
     * @throws Exception is state cannot be set (for example if pwm is not exported
     */
    public void setState(PwmState state) throws Exception{
        File file = new File(this.uri + "/pwm0" + PWM_ENABLE_PATH);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(state.ordinal() + "");
        bw.close();
        fw.close();
        currentPwmState = state;
        currentPwmStates.put(this.id, currentPwmState);
    }

    public void enable() throws Exception{
        this.setState(PwmState.ENABLE);
    }

    public void disable() throws Exception{
        this.setState(PwmState.DISABLE);
    }

    /**
     * @return PwmState Current pwm state
     * @throws Exception if period cannot be read
     */
    public PwmState getState() throws Exception{
        PwmState state = (Objects.equals(FileUtils.readFile(this.uri + "/pwm0" + PWM_ENABLE_PATH), "1"))?PwmState.ENABLE:PwmState.DISABLE;
        currentPwmState = state;
        currentPwmStates.put(this.id, currentPwmState);
        return state;
    }

    public HashMap<Integer, PwmState> getStates() {
        return currentPwmStates;
    }

    /**
     * Pwm configuration wrapper: set period and duty_cycle and enable pwm output
     * @param period in nanoseconds
     * @param dutyCycle in nanoseconds
     * @throws Exception if something went wrong in configuration process
     */
    public void configure(long period, long dutyCycle) throws Exception{
        this.setPeriod(period);
        this.setDutyCycle(dutyCycle);
        this.enable();
    }
}
