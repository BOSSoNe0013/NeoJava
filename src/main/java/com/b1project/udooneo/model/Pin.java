package com.b1project.udooneo.model;

import com.b1project.udooneo.gpio.Gpio;

/**
 * Copyright (C) 2015 Cyril BOSSELUT <bossone0013@gmail.com>
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
@SuppressWarnings("unused")
public class Pin {
    private final Integer id;
    private Gpio.PinState state;
    private Gpio.PinMode mode;

    public Pin(Integer id) {
        this(id, null, null);
    }
    public Pin(Integer id, Gpio.PinMode mode) {
        this(id, null, mode);
    }
    public Pin(Integer id, Gpio.PinState state) {
        this(id, state, null);
    }
    public Pin(Integer id, Gpio.PinState state, Gpio.PinMode mode) {
        this.id = id;
        this.state = state;
        this.mode = mode;
    }

    public int getId(){
        return id;
    }

    public Gpio.PinState getState(){
        return state;
    }

    public void setState(Gpio.PinState state){
        this.state = state;
    }

    public Gpio.PinMode getMode(){
        return this.mode;
    }

    public void setMode(Gpio.PinMode mode) {
        this.mode = mode;
    }

    public String toString(){
        return String.format("{\\\"id\\\":\\\"%d\\\",\\\"state\\\":\\\"%d\\\",\\\"mode\\\":\\\"%s\\\"}", id, state.ordinal(), (mode == Gpio.PinMode.INPUT)?"in":"out");
    }

}
