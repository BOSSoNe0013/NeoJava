package com.b1project.udooneo.messages;

import com.b1project.udooneo.gpio.Gpio.PinMode;
import com.b1project.udooneo.gpio.Gpio.PinState;

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

public class RequestMessage extends Message{

	public int pinId;
	public PinState state;
	public PinMode mode;
	public String textToDisplay;
	
	public RequestMessage(String method) {
		this(method,0,null,null);
	}
	public RequestMessage(String method, int pinId) {
		this(method, pinId, null, null);
	}
	public RequestMessage(String method, int pinId, PinState state) {
		this(method, pinId, state, null);
	}
	public RequestMessage(String method, int pinId, PinMode mode) {
		this(method, pinId, null, mode);
	}
	public RequestMessage(String method, int pinId, PinState state, PinMode mode) {
		super(method);
		this.pinId = pinId;
		this.state = state;
		this.mode = mode;
	}
	public RequestMessage(String method, String textToDisplay) {
		this(method);
		this.textToDisplay = textToDisplay;
	}

}
