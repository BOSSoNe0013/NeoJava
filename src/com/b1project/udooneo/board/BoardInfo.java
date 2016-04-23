package com.b1project.udooneo.board;

import com.b1project.udooneo.utils.FileUtils;

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

public class BoardInfo {

    public static String getBoardID(){
        try{
            String BOARD_CFG0_URI = "/sys/fsl_otp/HW_OCOTP_CFG0";
            String cfg0 = FileUtils.readFile(BOARD_CFG0_URI).replace("0x", "");
            String BOARD_CFG1_URI = "/sys/fsl_otp/HW_OCOTP_CFG1";
            String cfg1 = FileUtils.readFile(BOARD_CFG1_URI).replace("0x", "");
            return cfg0 + cfg1;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getBoardModel(){
        try{
            String BOARD_MODEL_URI = "/proc/device-tree/model";
            return FileUtils.readFile(BOARD_MODEL_URI);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}