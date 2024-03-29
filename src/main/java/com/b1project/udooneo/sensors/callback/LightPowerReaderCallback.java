package com.b1project.udooneo.sensors.callback;

/**
 * Copyright (C) 2017 Cyril BOSSELUT <bossone0013@gmail.com>
 * <p>
 * This file is part of NeoJava
 * <p>
 * NeoJava is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <<a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>>.
 */
public interface LightPowerReaderCallback {
    void onRequestComplete(Float power);
}
