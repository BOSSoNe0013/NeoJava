package com.b1project.udooneo.listeners;

import com.b1project.udooneo.gpio.Pin;

import java.net.Socket;
import java.util.List;

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

public interface NeoJavaProtocolListener {
    String getVersionString();
    void onQuitRequest(Socket clientSocket);
    void onClearLCDRequest();
    void onLCDPrintRequest(String message);
    void onTemperatureRequest();
    List<Pin> onExportedGpiosRequest();
}
