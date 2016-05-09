package com.b1project.udooneo.ir;

/**
 * Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
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
class IRCodes {
    private static IRCodes ourInstance = new IRCodes();

    static final int[] POWER_BUTTON = new int[]{
            1, 3,
            1, 1,
            1, 1,
            1, 3,
            1, 3,
            2, 5,
            1, 1,
            1, 1,
            1, 1,
            6, 28,
            6, 58,
            6, 57,
            6, 59,
            6, 59,
            1, 1,
            6, 55,
            6, 59,
            4, 59,
            6, 59,
            1, 1,
            6, 57,
            1, 1,
            6, 55,
            1, 1,
            6, 57,
            1, 1,
            6, 56,
            6, 57,
            1, 1,
            5, 58,
            1, 1,
            5, 56,
            1, 1,
            5, 58,
            1, 1,
            5, 58,
            1, 1,
            5, 56,
            1, 1,
            5, 58
    };

    public static IRCodes getInstance() {
        return ourInstance;
    }

    private IRCodes() {
    }
}
