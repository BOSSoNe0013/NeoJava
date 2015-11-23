package com.b1project.udooneo.lcd;
/**
 *  Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
 *
 *  This file is part of NeoJava examples for UDOO
 *
 *  NeoJava examples for UDOO is free software: you can redistribute it and/or modify
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

import com.b1project.udooneo.gpio.Gpio;

@SuppressWarnings({"unused", "FieldCanBeLocal", "PointlessBitwiseExpression"})
public class Lcd{
    //# commands
    final static char LCD_CLEARDISPLAY        = 0x01;
    final static char LCD_RETURNHOME          = 0x02;
    final static char LCD_ENTRYMODESET        = 0x04;
    final static char LCD_DISPLAYCONTROL      = 0x08;
    final static char LCD_CURSORSHIFT         = 0x10;
    final static char LCD_FUNCTIONSET         = 0x20;
    final static char LCD_SETCGRAMADDR        = 0x40;
    final static char LCD_SETDDRAMADDR        = 0x80;

    //# flags for display entry mode
    final static char LCD_ENTRYRIGHT          = 0x00;
    final static char LCD_ENTRYLEFT           = 0x02;
    final static char LCD_ENTRYSHIFTINCREMENT = 0x01;
    final static char LCD_ENTRYSHIFTDECREMENT = 0x00;

    //# flags for display on/off control
    final static char LCD_DISPLAYON           = 0x04;
    final static char LCD_DISPLAYOFF          = 0x00;
    final static char LCD_CURSORON            = 0x02;
    final static char LCD_CURSOROFF           = 0x00;
    final static char LCD_BLINKON             = 0x01;
    final static char LCD_BLINKOFF            = 0x00;

    //# flags for display/cursor shift
    final static char LCD_DISPLAYMOVE         = 0x08;
    final static char LCD_CURSORMOVE          = 0x00;
    final static char LCD_MOVERIGHT           = 0x04;
    final static char LCD_MOVELEFT            = 0x00;

    //# flags for function set
    final static char LCD_8BITMODE            = 0x10;
    final static char LCD_4BITMODE            = 0x00;
    final static char LCD_2LINE               = 0x08;
    final static char LCD_1LINE               = 0x00;
    final static char LCD_5x10DOTS            = 0x04;
    final static char LCD_5x8DOTS             = 0x00;


    /*#  D0: gpio16
      #  D7: gpio15
      #  D6: gpio14
      #  D5: gpio22
      #  D4: gpio25
      #  EN: gpio20
      #  RS: gpio21*/
      
    private Gpio lcd_en;
    private Gpio lcd_rs;
    
    private Gpio lcd_bl;

    private Gpio lcd_d4;
    private Gpio lcd_d5;
    private Gpio lcd_d6;
    private Gpio lcd_d7;
    
    private char lcd_mode = LCD_4BITMODE;

    public Lcd(int en, int rs, int d4, int d5, int d6, int d7) throws Exception{
        super();
        lcd_mode = LCD_4BITMODE;
        lcd_en = Gpio.getInstance(en);
        lcd_rs = Gpio.getInstance(rs);
        
        lcd_bl = Gpio.getInstance(16);

        lcd_d4 = Gpio.getInstance(d4);
        lcd_d5 = Gpio.getInstance(d5);
        lcd_d6 = Gpio.getInstance(d6);
        lcd_d7 = Gpio.getInstance(d7);
        
        lcd_rs.low();
        //lcd_rw.low();
        lcd_d7.low();
        lcd_d6.low();
        lcd_d5.low();
        lcd_d4.low();
         
        this.writeNibbles((char)0x33, 0);
        Thread.sleep(2);
        this.writeNibbles((char)0x32, 0);
        Thread.sleep(2);
        
        this.writeNibbles((char)(LCD_FUNCTIONSET | lcd_mode | LCD_2LINE | LCD_5x8DOTS), 0);
        Thread.sleep(2);
        setBacklightState(true);
        setLcdDisplayState(true);
        this.writeNibbles((char)(LCD_ENTRYMODESET | LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT), 0);
        Thread.sleep(2);
        System.out.println("Clear display");
        this.clear();
        Thread.sleep(2000);
        System.out.println("LCD init complete");
        
    }
    
    public void pulseEn() throws Exception{
        //System.out.println("Pulse En");
        Thread.sleep(2);
        lcd_en.high();
        Thread.sleep(2);
        lcd_en.low();
   }
   
   public void clear() throws Exception{
        this.writeNibbles(LCD_CLEARDISPLAY, 0);
   }
    
    public void writeNibble(int nib) throws Exception{
        lcd_rs.low();
        /*System.out.print(nib & 0x8);
        System.out.print(nib & 0x4);
        System.out.print(nib & 0x2);
        System.out.println(nib & 0x1);*/
        if((nib & 0x8) != 0){lcd_d7.high();}else{lcd_d7.low();}
        if((nib & 0x4) != 0){lcd_d6.high();}else{lcd_d6.low();}
        if((nib & 0x2) != 0){lcd_d5.high();}else{lcd_d5.low();}
        if((nib & 0x1) != 0){lcd_d4.high();}else{lcd_d4.low();}
        this.pulseEn();
        Thread.sleep(2);
    }
    
    public void writeNibbles(char value, int mode) throws Exception{
        Gpio gpio = Gpio.getInstance(106);
        if(mode == 1){
            lcd_rs.high();
        }
        else{
            lcd_rs.low();
        }
        gpio.high();
        int nib = ((int)value >> 4);
        /*System.out.printf("%s %04X : ", value, (int)value);
        System.out.print(nib & 0x8);
        System.out.print(nib & 0x4);
        System.out.print(nib & 0x2);
        System.out.print(nib & 0x1);*/
        
        if((nib & 0x8) != 0){lcd_d7.high();}else{lcd_d7.low();}
        if((nib & 0x4) != 0){lcd_d6.high();}else{lcd_d6.low();}
        if((nib & 0x2) != 0){lcd_d5.high();}else{lcd_d5.low();}
        if((nib & 0x1) != 0){lcd_d4.high();}else{lcd_d4.low();}
        this.pulseEn();
        gpio.low();
        Thread.sleep(1);
        gpio.high();
        nib = ((int)value & 0x0f);
        /*System.out.print(nib & 0x8);
        System.out.print(nib & 0x4);
        System.out.print(nib & 0x2);
        System.out.println(nib & 0x1);*/
        
        if((nib & 0x8) != 0){lcd_d7.high();}else{lcd_d7.low();}
        if((nib & 0x4) != 0){lcd_d6.high();}else{lcd_d6.low();}
        if((nib & 0x2) != 0){lcd_d5.high();}else{lcd_d5.low();}
        if((nib & 0x1) != 0){lcd_d4.high();}else{lcd_d4.low();}
        this.pulseEn();
        gpio.low();
        Thread.sleep(1);
    }
    
    public void print(String message) throws Exception{
        this.print(message.toCharArray());
    }
    
    public void print(char[] message) throws Exception{
        for (char aMessage : message) {
            if (aMessage == '\n' || aMessage == '\r') {
                this.writeNibbles((char) 0xC0, 0);
            } else {
                writeNibbles(aMessage, 1);
            }
        }
    }

    public void setLcdDisplayState(boolean state){
        try {
            if (state) {
                this.writeNibbles((char)(LCD_DISPLAYCONTROL | LCD_DISPLAYON | LCD_CURSOROFF | LCD_BLINKOFF), 0);
            }
            else{
                this.writeNibbles((char)(LCD_DISPLAYCONTROL | LCD_DISPLAYOFF | LCD_CURSOROFF | LCD_BLINKOFF), 0);
            }
            Thread.sleep(2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setBacklightState(boolean state){
        try {
            if (state) {
                lcd_bl.high();
            } else {
                lcd_bl.low();
            }
            Thread.sleep(2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
