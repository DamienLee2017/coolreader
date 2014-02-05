package com.dotcool.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 计算时间的一些工具，如计算当前时间...
 * @author 002666(lee)
 * date 2012/4/25
 */

public class TimeUtil
{
	/**
	 * 计算当前时间，并按指定格式输出
	 * @return 返回当前时间
	 */
	public static String getCurrentTime()
	{
		String currentTime = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		currentTime = sdf.format(new Date());
		return currentTime;
	}
}
