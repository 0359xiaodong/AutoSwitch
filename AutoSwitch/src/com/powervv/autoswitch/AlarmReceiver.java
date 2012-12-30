/**
 * 文件名：AlarmReceiver.java
 *
 * 版本信息：
 * 日期：2012-11-22
 * Copyright powervv.com 2012
 * 版权所有
 *
 */
package com.powervv.autoswitch;

import java.lang.reflect.Method;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.os.Bundle;

/**
 * 
 * 项目名称：com.powervv.autoswitch.AutoSwitchActivity 类名称：AlarmReceiver 类描述：
 * 创建人：paul 创建时间：2012-12-1 下午7:27:19 修改人：paul 修改时间：2012-12-1 下午7:27:19 修改备注：
 * 
 * @version
 * 
 */
public class AlarmReceiver extends BroadcastReceiver {
	final static String statesInfo[] = { "开启", "关闭" };	
	private final static String TAG = "AlarmReceiver";
	private ConnectivityManager mConnectivityManager;

	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		int weekDay = bundle.getInt("weekday"); 
		Calendar calendar = Calendar.getInstance();
		long curTime = System.currentTimeMillis();
		calendar.setTimeInMillis(curTime);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		Log.e(TAG, "dayOfWeek = " + dayOfWeek);
		switch (dayOfWeek) {
		case Calendar.MONDAY:
		case Calendar.TUESDAY:
		case Calendar.WEDNESDAY:
		case Calendar.THURSDAY:
		case Calendar.FRIDAY:
			if (weekDay == AutoSwitchActivity.WORK_DAY ||
					weekDay == AutoSwitchActivity.EVERY_DAY) {
				break;
			}
			else {
				return;
			}
		case Calendar.SATURDAY:
		case Calendar.SUNDAY:
			if (weekDay == AutoSwitchActivity.FREE_DAY ||
					weekDay == AutoSwitchActivity.EVERY_DAY) {
				break;
			}
			else {
				return;
			}
		default:
			return;
		}
				
		boolean bWifiEnable = bundle.getBoolean("wifi");
		boolean bMobileEnable = bundle.getBoolean("mobile");
		int wifiInfoIdx = bWifiEnable ? 0 : 1;
		int mobileInfoIdx = bMobileEnable ? 0 : 1;
		Log.e(TAG, "alarm received! bWifi=" + bWifiEnable + " bMobile="
				+ bMobileEnable);
		Toast.makeText(
				context,
				"Wifi状态:" + statesInfo[wifiInfoIdx] + ", 数据连接状态:"
						+ statesInfo[mobileInfoIdx], Toast.LENGTH_LONG).show();

		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		int wifiState = wifiManager.getWifiState();
		switch (wifiState) {
		case WifiManager.WIFI_STATE_DISABLED:
		case WifiManager.WIFI_STATE_DISABLING:
			if (bWifiEnable)
				wifiManager.setWifiEnabled(bWifiEnable);
			break;
		case WifiManager.WIFI_STATE_ENABLED:
		case WifiManager.WIFI_STATE_ENABLING:
			if (!bWifiEnable)
				wifiManager.setWifiEnabled(bWifiEnable);
			break;
		default:
			wifiManager.setWifiEnabled(bWifiEnable);
			break;
		}

		mConnectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		Boolean bMobileState = false;
		try {
			Object[] arg = null;
			bMobileState = (Boolean) invokeMethod("getMobileDataEnabled", arg);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 如果当前状态与要设置状态不一致
		if ((bMobileState && !bMobileEnable)
				|| (!bMobileState && bMobileEnable)) {
			try {
				invokeBooleanArgMethod("setMobileDataEnabled", bMobileEnable);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@SuppressWarnings("rawtypes")
	public Object invokeBooleanArgMethod(String methodName, boolean value)
			throws Exception {
		Class ownerClass = mConnectivityManager.getClass();
		Class[] argsClass = new Class[1];
		argsClass[0] = boolean.class;
		Method method = ownerClass.getMethod(methodName, argsClass);
		return method.invoke(mConnectivityManager, value);
	}

	@SuppressWarnings("rawtypes")
	public Object invokeMethod(String methodName, Object[] arg)
			throws Exception {
		Class ownerClass = mConnectivityManager.getClass();
		Class[] argsClass = null;
		if (arg != null) {
			argsClass = new Class[1];
			argsClass[0] = arg.getClass();
		}
		Method method = ownerClass.getMethod(methodName, argsClass);
		return method.invoke(mConnectivityManager, arg);
	}

}
