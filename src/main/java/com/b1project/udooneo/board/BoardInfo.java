package com.b1project.udooneo.board;

import com.b1project.udooneo.utils.FileUtils;

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
 *  along with this program.  If not, see <<a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>>.
 *
 */

public class BoardInfo {

    public static String getBoardID(){
        try{
            
            String cfg0 = FileUtils.readFile(FileUtils.BOARD_CFG0_URI).replace("0x", "");
            String cfg1 = FileUtils.readFile(FileUtils.BOARD_CFG1_URI).replace("0x", "");
            return cfg0 + cfg1;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getBoardModel(){
        try{
            return FileUtils.readFile(FileUtils.BOARD_MODEL_URI).replace("\u0000", "");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getBoardName(){
        try{
            return FileUtils.readFile(FileUtils.BOARD_NAME_URI);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String reboot(){
        try{
            Runtime.getRuntime().exec("reboot");
            return "OK";
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}