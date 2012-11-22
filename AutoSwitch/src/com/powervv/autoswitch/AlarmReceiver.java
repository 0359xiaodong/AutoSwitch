package com.powervv.autoswitch;

import java.lang.reflect.Method;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver
{
	private static final String	TAG	= "AlarmReceiver";	
	private ConnectivityManager mConnectivityManager;
	public void onReceive(Context context, Intent intent)
	{		
		Bundle bundle = intent.getExtras();
		boolean bWifiEnable = bundle.getBoolean("wifi");
		boolean bMobileEnable = bundle.getBoolean("mobile");
		
		Log.e(TAG, "alarm received! bWifi=" + bWifiEnable + " bMobile=" + bMobileEnable);
		Toast.makeText(context, "你设置的闹钟时间到了", Toast.LENGTH_LONG).show();
		
		WifiManager wifiManager= (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
		//if (wifiManager.WIFI_STATE_DISABLED != wifiManager.getWifiState())
		{
			wifiManager.setWifiEnabled(bWifiEnable);			
		}

		mConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Boolean bMobileDataEnable = false;
		try
		{
			Object[] arg = null;
			bMobileDataEnable = (Boolean) invokeMethod("getMobileDataEnabled", arg);
		} catch (Exception e)
		{
			e.printStackTrace();
		}		
		
		//if (bMobileDataEnable)
		{
			try
			{
				invokeBooleanArgMethod("setMobileDataEnabled", bMobileEnable);
			} catch (Exception e)
			{
				e.printStackTrace();
			}		
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	public Object invokeBooleanArgMethod(String methodName, boolean value) throws Exception
	{
		Class ownerClass = mConnectivityManager.getClass();
		Class[] argsClass = new Class[1];
		argsClass[0] = boolean.class;
		Method method = ownerClass.getMethod(methodName, argsClass);
		return method.invoke(mConnectivityManager, value);
	}
	
	@SuppressWarnings("rawtypes")
	public Object invokeMethod(String methodName, Object[] arg) throws Exception
	{
	Class ownerClass = mConnectivityManager.getClass();
	Class[] argsClass = null;
	if (arg != null)
	{
	argsClass = new Class[1];
	argsClass[0] = arg.getClass();
	}
	Method method = ownerClass.getMethod(methodName, argsClass);
	return method.invoke(mConnectivityManager, arg);
	}

}
