package com.b1project.udooneo.net;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (C) 2016 Cyril Bosselut <bossone0013@gmail.com>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class InetDevice {

    private NetworkInterface mNetworkInterface;

    @SuppressWarnings("unused")
    public enum Dev {
        LOOP("lo"),
        ETH("eth0"),
        WLAN("wlan0"),
        USB("usb0");

        private String name;
        Dev(String name){
            this.name = name;
        }
        public String toString(){
            return name;
        }
    }

    public enum State {
        DOWN,
        UP
    }

    public InetDevice(Dev dev){
        mNetworkInterface = getInterface(dev);
    }

    public static List<NetworkInterface> listAll(){
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException e) {
            System.err.println(e.getLocalizedMessage());
            return null;
        }
    }

    private static NetworkInterface getInterface(Dev dev){
        try {
            return NetworkInterface.getByName(dev.toString());
        } catch (SocketException e) {
            System.err.println(e.getLocalizedMessage());
            return null;
        }
    }

    public State getState(){
        try {
            if (mNetworkInterface != null && mNetworkInterface.isUp()){
                return State.UP;
            }
        } catch (SocketException e) {
            return State.DOWN;
        }
        return State.DOWN;
    }

}
