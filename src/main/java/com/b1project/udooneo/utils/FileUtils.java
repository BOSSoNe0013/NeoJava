package com.b1project.udooneo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Copyright (C) 2015 Cyril BOSSELUT <bossone0013@gmail.com>
 * <p>
 * This file is part of NeoJava examples for UDOO
 * <p>
 * NeoJava examples for UDOO is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * <p>
 * This libraries are distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FileUtils {
	public static final String SEPARATOR = "/";

	public static final String DEVICE_NAME_ENDPOINT = "name";
	public static final String DEVICE_EXPORT_ENDPOINT = "export";
	public static final String DEVICE_UNEXPORT_ENDPOINT = "unexport";
	public static final String DEVICE_ENABLE_ENDPOINT = "enable";
	public static final String DEVICE_DATA_ENDPOINT = "data";

	//gpios
	public static final String BASE_GPIO_URI = "/sys/class/gpio";
	public static final String COMMON_GPIO_URI = BASE_GPIO_URI + SEPARATOR + "gpio";
	public static final String EXPORT_GPIO_URI = BASE_GPIO_URI + SEPARATOR + DEVICE_EXPORT_ENDPOINT;
	public static final String RELEASE_GPIO_URI = BASE_GPIO_URI + SEPARATOR + DEVICE_UNEXPORT_ENDPOINT;

	//pwm
	public static final String BASE_PWM_URI = "/sys/class/pwm";
	public static final String COMMON_PWM_URI = BASE_PWM_URI + "/pwmchip";
	public static final String EXPORT_PWM_URI = COMMON_PWM_URI + SEPARATOR + DEVICE_EXPORT_ENDPOINT;
	public static final String RELEASE_PWM_URI = COMMON_PWM_URI + SEPARATOR + DEVICE_UNEXPORT_ENDPOINT;

	//board info
	public static final String BOARD_NAME_URI = "/etc/hostname";
	public static final String BOARD_CFG0_URI = "/sys/fsl_otp/HW_OCOTP_CFG0";
	public static final String BOARD_CFG1_URI = "/sys/fsl_otp/HW_OCOTP_CFG1";
	public static final String BOARD_MODEL_URI = "/proc/device-tree/model";

	// temperature
	public static final String TEMPERATURE_BASE_URI = "/sys/class/i2c-dev/i2c-1/device/1-0048";
	public static final String TEMPERATURE_DEVICE_NAME_URI = TEMPERATURE_BASE_URI + SEPARATOR + DEVICE_NAME_ENDPOINT;
	public static final String TEMP_URI = TEMPERATURE_BASE_URI + SEPARATOR + "temp1_input";

	// barometer
	public static final String BAROMETER_BASE_URI = "/sys/class/i2c-dev/i2c-1/device/1-0060/iio:device3";
	public static final String BAROMETER_DEVICE_NAME_URI = BAROMETER_BASE_URI + SEPARATOR + DEVICE_NAME_ENDPOINT;
	public static final String TEMP_RAW_URI = BAROMETER_BASE_URI + SEPARATOR + "in_temp_raw";
	public static final String TEMP_SCALE_URI = BAROMETER_BASE_URI + SEPARATOR + "in_temp_scale";
	public static final String PRESS_RAW_URI = BAROMETER_BASE_URI + SEPARATOR + "in_pressure_raw";
	public static final String PRESS_SCALE_URI = BAROMETER_BASE_URI + SEPARATOR + "in_pressure_scale";

	// light sensor
	public static final String LIGHT_BASE_URI = "/sys/class/i2c-dev/i2c-1/device/1-0029/iio:device2";
	public static final String LIGHT_DEVICE_NAME_URI = LIGHT_BASE_URI  + SEPARATOR+ DEVICE_NAME_ENDPOINT;
	public static final String LIGHT_RAW_URI = LIGHT_BASE_URI  + SEPARATOR+ "in_intensity_ir_raw";
	public static final String LIGHT_SCALE_URI = LIGHT_BASE_URI + SEPARATOR + "in_intensity_ir_calibscale";

	// magnetometer
	public static final String MAGNETOMETER_BASE_URI = "/sys/class/misc/FreescaleMagnetometer";
	public static final String MAGNETOMETER_ACTIVATION_URI = MAGNETOMETER_BASE_URI + SEPARATOR + DEVICE_ENABLE_ENDPOINT;
	public static final String MAGNETOMETER_DATA_URI = MAGNETOMETER_BASE_URI + SEPARATOR + DEVICE_DATA_ENDPOINT;

	// gyroscope
	public static final String GYROSCOPE_BASE_URI = "/sys/class/misc/FreescaleGyroscope";
	public static final String GYROSCOPE_ACTIVATION_URI = GYROSCOPE_BASE_URI + SEPARATOR + DEVICE_ENABLE_ENDPOINT;
	public static final String GYROSCOPE_DATA_URI = GYROSCOPE_BASE_URI + SEPARATOR + DEVICE_DATA_ENDPOINT;

	// accelerometer
	public static final String ACCELEROMETER_BASE_URI = "/sys/class/misc/FreescaleAccelerometer";
	public static final String ACCELEROMETER_ACTIVATION_URI = ACCELEROMETER_BASE_URI + SEPARATOR + DEVICE_ENABLE_ENDPOINT;
	public static final String ACCELEROMETER_DATA_URI = ACCELEROMETER_BASE_URI + SEPARATOR + DEVICE_DATA_ENDPOINT;

	public static String readFile(String uri) throws Exception {
		File file = new File(uri);
		FileReader fr = new FileReader(file.getAbsoluteFile());
		BufferedReader br = new BufferedReader(fr);
		String value = br.readLine();
		br.close();
		return value;
	}
}
