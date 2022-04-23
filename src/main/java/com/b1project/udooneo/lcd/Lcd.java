package com.b1project.udooneo.lcd;

import com.b1project.udooneo.gpio.Gpio;

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
@SuppressWarnings({"unused", "FieldCanBeLocal", "WeakerAccess"})
public class Lcd{

    public final static int NO_RW = 0x00;

    //# commands
    private final static char LCD_CLEAR_DISPLAY = 0x01;
    private final static char LCD_RETURN_HOME = 0x02;
    private final static char LCD_ENTRY_MODESET = 0x04;
    private final static char LCD_DISPLAY_CONTROL = 0x08;
    private final static char LCD_CURSOR_SHIFT = 0x10;
    private final static char LCD_FUNCTION_SET = 0x20;
    private final static char LCD_SET_CG_RAM_ADDR = 0x40;
    private final static char LCD_SET_DD_RAM_ADDR = 0x80;

    //# flags for display entry mode
    private final static char LCD_ENTRY_RIGHT = 0x00;
    private final static char LCD_ENTRY_LEFT = 0x02;
    private final static char LCD_ENTRY_SHIFT_INCREMENT = 0x01;
    private final static char LCD_ENTRY_SHIFT_DECREMENT = 0x00;

    //# flags for display on/off control
    private final static char LCD_DISPLAY_ON = 0x04;
    private final static char LCD_DISPLAY_OFF = 0x00;
    private final static char LCD_CURSOR_ON = 0x02;
    private final static char LCD_CURSOR_OFF = 0x00;
    private final static char LCD_BLINK_ON = 0x01;
    private final static char LCD_BLINK_OFF = 0x00;

    //# flags for display/cursor shift
    private final static char LCD_DISPLAY_MOVE = 0x08;
    private final static char LCD_CURSOR_MOVE = 0x00;
    private final static char LCD_MOVE_RIGHT = 0x04;
    private final static char LCD_MOVE_LEFT = 0x00;

    //# flags for function set
    private final static char LCD_8BIT_MODE = 0x10;
    private final static char LCD_4BIT_MODE = 0x00;
    private final static char LCD_2LINE               = 0x08;
    private final static char LCD_1LINE               = 0x00;
    private final static char LCD_5x10DOTS            = 0x04;
    private final static char LCD_5x8DOTS             = 0x00;


    /*#  BL: gpio16
      #  D7: gpio15
      #  D6: gpio14
      #  D5: gpio22
      #  D4: gpio25
      #  EN: gpio20
      #  RS: gpio21*/
      
    private Gpio lcd_en;
    private Gpio lcd_rs;
    private Gpio lcd_rw;

    private Gpio lcd_bl;

    private Gpio lcd_d4;
    private Gpio lcd_d5;
    private Gpio lcd_d6;
    private Gpio lcd_d7;
    
    private final char lcd_mode;

    private static char DEFAULT_LCD_STATE = LCD_DISPLAY_ON | LCD_CURSOR_OFF | LCD_BLINK_OFF;

    public Lcd(int en, int rs, int d4, int d5, int d6, int d7, int bl, int rw) throws Exception{
        super();
        lcd_mode = LCD_4BIT_MODE;
        lcd_en = Gpio.getInstance(en);
        lcd_en.setMode(Gpio.PinMode.OUTPUT);
        lcd_rs = Gpio.getInstance(rs);
        lcd_rs.setMode(Gpio.PinMode.OUTPUT);
        if(rw != NO_RW){
            lcd_rw = Gpio.getInstance(rw);
        }

        lcd_bl = Gpio.getInstance(bl);
        lcd_bl.setMode(Gpio.PinMode.OUTPUT);

        lcd_d4 = Gpio.getInstance(d4);
        lcd_d4.setMode(Gpio.PinMode.OUTPUT);
        lcd_d5 = Gpio.getInstance(d5);
        lcd_d5.setMode(Gpio.PinMode.OUTPUT);
        lcd_d6 = Gpio.getInstance(d6);
        lcd_d6.setMode(Gpio.PinMode.OUTPUT);
        lcd_d7 = Gpio.getInstance(d7);
        lcd_d7.setMode(Gpio.PinMode.OUTPUT);

        lcd_rs.low();
        lcd_en.low();
        if(lcd_rw != null) {
            lcd_rw.low();
        }
        lcd_d7.low();
        lcd_d6.low();
        lcd_d5.low();
        lcd_d4.low();
        lcd_rs.high();
        lcd_rs.low();

        this.set((char)0x33);
	Thread.sleep(3);
        this.set((char)0x32);
	Thread.sleep(3);
        this.set((char)(LCD_SET_DD_RAM_ADDR | 0x40));
	Thread.sleep(3);
        this.set((char)(LCD_FUNCTION_SET | lcd_mode | LCD_2LINE | LCD_5x8DOTS));
	Thread.sleep(3);
        setBacklightState(true);
	Thread.sleep(3);
        setLcdDisplayState(true);
	Thread.sleep(3);
        this.set((char)(LCD_ENTRY_MODESET | LCD_ENTRY_LEFT | LCD_ENTRY_SHIFT_DECREMENT));
	Thread.sleep(3);
        System.out.println("\rClear display");
        System.out.print("#:");
        this.clear();
        Thread.sleep(2000);
        System.out.println("\rLCD init complete");
        System.out.print("#:");

    }

    @Override
    protected void finalize() throws Throwable{
        lcd_en.release();
        lcd_en = null;
        lcd_rs.release();
        lcd_rs = null;

        lcd_bl.release();
        lcd_bl = null;

        lcd_d4.release();
        lcd_d4 = null;
        lcd_d5.release();
        lcd_d5 = null;
        lcd_d6.release();
        lcd_d6 = null;
        lcd_d7.release();
        lcd_d7 = null;
        super.finalize();
    }

    /**
     * Clear lcd screen
     * @throws Exception if something went wrong
     */
    public void clear() throws Exception{
        this.set(LCD_CLEAR_DISPLAY);
    }

    /**
     * print String on screen
     * @param message String
     * @throws Exception if something went wrong
     */
    public void print(String message) throws Exception{
        if(message == null){
            throw new NullPointerException("Try to print a null String on LCD");
        }
        this.print(message.toCharArray());
    }

    /**
     * print char[] on screen
     * @param message char[]
     * @throws Exception if something went wrong
     */
    public void print(char[] message) throws Exception{
        for (char aMessage : message) {
            if (aMessage == '\n' || aMessage == '\r') {
                this.set((char) 0xC0);
            } else {
                write(aMessage);
            }
        }
    }

    /**
     * toggle display ON/OFF
     * @param state boolean
     * @throws Exception if something went wrong
     */
    public void setLcdDisplayState(boolean state) throws Exception{
        if (state) {
            DEFAULT_LCD_STATE &= LCD_DISPLAY_ON;
        }
        else{
            DEFAULT_LCD_STATE &= ~LCD_DISPLAY_ON;
        }
        this.set((char)(LCD_DISPLAY_CONTROL | DEFAULT_LCD_STATE));
    }

    /**
     * toggle cursor ON/OFF
     * @param state boolean
     * @throws Exception if something went wrong
     */
    public void setCursorState(boolean state) throws Exception{
        if(state){
            DEFAULT_LCD_STATE &= LCD_CURSOR_ON;
        }
        else{
            DEFAULT_LCD_STATE &= ~LCD_CURSOR_ON;
        }
        this.set((char)(LCD_DISPLAY_CONTROL | DEFAULT_LCD_STATE));
    }

    /**
     * Mode cursor to position
     * @param col int from 0 to n
     * @param row int from 0 to n
     * @throws Exception if something went wrong
     */
    public void setCursorPosition(int col, int row) throws Exception{
        int[] row_offsets = { 0x00, 0x40, 0x14, 0x54 };
        this.set((char)(LCD_SET_DD_RAM_ADDR | col + row_offsets[row]));
    }

    /**
     * toggle cursor blinking ON/OFF
     * @param state boolean
     * @throws Exception if something went wrong
     */
    public void setCursorBlinkingState(boolean state) throws Exception{
        if(state){
            DEFAULT_LCD_STATE &= LCD_BLINK_ON;
        }
        else{
            DEFAULT_LCD_STATE &= ~LCD_BLINK_ON;
        }
        this.set((char)(LCD_DISPLAY_CONTROL | DEFAULT_LCD_STATE));
    }

    /**
     * toggle backlight ON/OFF
     * @param state boolean
     * @throws Exception if something went wrong
     */
    public void setBacklightState(boolean state) throws Exception{
        if (state) {
            lcd_bl.high();
        } else {
            lcd_bl.low();
        }
        Thread.sleep(2);
    }

    /**
     * create custom char at location (from 0 to 7) with charmap
     * @param location int
     * @param charMap char[]
     * @throws Exception if something went wrong
     */
    public void createChar(int location, char[] charMap) throws Exception{
        location &= 0x7;
        this.set((char)(LCD_SET_CG_RAM_ADDR | (location << 3)));
        for(int i = 0; i < 8; i++){
            this.write(charMap[i]);
        }
    }

    /****** Low level part */

    public void pulseEn() throws Exception{
        //System.out.println("Pulse En");
        Thread.sleep(2);
        lcd_en.high();
        Thread.sleep(2);
        lcd_en.low();
   }
   
    public void set(char value) throws Exception{
        this.writeNibbles(value, 0);
        Thread.sleep(2);
    }

    public void write(char value) throws Exception{
        this.writeNibbles(value, 1);
        Thread.sleep(2);
    }
    
    public void writeNibbles(char value, int mode) throws Exception{
        if(mode == 1){
            lcd_rs.high();
        }
        else{
            lcd_rs.low();
        }
        if(lcd_rw != null) {
            lcd_rw.low();
        }
        int nib = ((int)value >> 4);

        if((nib & 0x8) != 0){lcd_d7.high();}else{lcd_d7.low();}
        if((nib & 0x4) != 0){lcd_d6.high();}else{lcd_d6.low();}
        if((nib & 0x2) != 0){lcd_d5.high();}else{lcd_d5.low();}
        if((nib & 0x1) != 0){lcd_d4.high();}else{lcd_d4.low();}
        this.pulseEn();
        Thread.sleep(1);
        nib = ((int)value & 0x0f);

        if((nib & 0x8) != 0){lcd_d7.high();}else{lcd_d7.low();}
        if((nib & 0x4) != 0){lcd_d6.high();}else{lcd_d6.low();}
        if((nib & 0x2) != 0){lcd_d5.high();}else{lcd_d5.low();}
        if((nib & 0x1) != 0){lcd_d4.high();}else{lcd_d4.low();}
        this.pulseEn();
        Thread.sleep(1);
    }
}
