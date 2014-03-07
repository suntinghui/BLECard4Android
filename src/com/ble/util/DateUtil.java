package com.ble.util;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class DateUtil {

	public static int getCurrentYear() {
		return Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date()));
	}

	public static int getCurrentMonth() {
		return Integer.parseInt(new SimpleDateFormat("MM").format(new Date()));
	}

	public static int getCurrentDay() {
		return Integer.parseInt(new SimpleDateFormat("dd").format(new Date()));
	}

	public static int getCurrentHour() {
		return Integer.parseInt(new SimpleDateFormat("HH").format(new Date()));
	}

	public static int getCurrentMin() {
		return Integer.parseInt(new SimpleDateFormat("mm").format(new Date()));
	}

	public static int getCurrentSec() {
		return Integer.parseInt(new SimpleDateFormat("ss").format(new Date()));
	}

}
