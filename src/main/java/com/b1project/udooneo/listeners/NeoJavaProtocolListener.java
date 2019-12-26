package com.b1project.udooneo.listeners;

import java.net.Socket;
import java.util.List;

import com.b1project.udooneo.model.Pin;

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

public interface NeoJavaProtocolListener {
    String getVersionString();
    void onQuitRequest(Socket clientSocket);
    void onClearLCDRequest();
    void onLCDPrintRequest(String message);
    void onTemperatureRequest();
    void onLightPowerRequest();
    void onAccelerometerRequest();
    void onMagnetometerRequest();
    void onGyroscopeRequest();
    List<Pin> onExportedGpiosRequest();
    void onSerialPortWriteRequest(byte[] bytes);
    void onSerialPortWriteRequest(int b);
    void onSerialPortWriteRequest(String message);
}
