package com.b1project.udooneo.board;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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
    private static String BOARD_CFG0_URI = "/sys/fsl_otp/HW_OCOTP_CFG0";
    private static String BOARD_CFG1_URI ="/sys/fsl_otp/HW_OCOTP_CFG1";

    public static String getBoardID(){
        try{
            String cfg0 = read(BOARD_CFG0_URI).replace("0x", "");
            String cfg1 = read(BOARD_CFG1_URI).replace("0x", "");
            return cfg0 + cfg1;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    protected static String read(String uri) throws Exception{
        File file = new File(uri);
        FileReader fr = new FileReader(file.getAbsoluteFile());
        BufferedReader br = new BufferedReader(fr);
        String value = br.readLine();
        br.close();
        return value;
    }
}