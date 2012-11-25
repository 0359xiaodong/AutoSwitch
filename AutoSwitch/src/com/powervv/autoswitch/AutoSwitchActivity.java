package com.powervv.autoswitch;

import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.app.TimePickerDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Calendar;
import android.util.Log;

public class AutoSwitchActivity extends Activity {
	private static final String	TAG	= "AutoSwitch";
	Calendar aCalendar[];
	int aHour[] 	= 	{7, 19, 23}; // {22, 22, 22}; // 
	int aMinute[] 	= 	{0, 0, 10}; // {52, 53, 54}; // 
	boolean aWifiState[]; // = {false, true, false};
	boolean aMobileState[]; // = {true, true, false};
	
	//6个多选项
	CheckBox	m_CheckBox1;
	CheckBox	m_CheckBox2;
	CheckBox	m_CheckBox3;
	CheckBox	m_CheckBox4;
	CheckBox	m_CheckBox5;
	CheckBox	m_CheckBox6;
	
	TableRow	m_TableRow2;
	TableRow	m_TableRow3;
	TableRow	m_TableRow4;	
	TextView 	m_TextView3;
	TextView 	m_TextView4;
	TextView 	m_TextView5;	
	   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_switch);
        
		// 取得每个CheckBox对象 
		m_CheckBox1 = (CheckBox) findViewById(R.id.checkBox1);
		m_CheckBox2 = (CheckBox) findViewById(R.id.checkBox2);
		m_CheckBox3 = (CheckBox) findViewById(R.id.checkBox3);
		m_CheckBox4 = (CheckBox) findViewById(R.id.checkBox4);
		m_CheckBox5 = (CheckBox) findViewById(R.id.checkBox5);
		m_CheckBox6 = (CheckBox) findViewById(R.id.checkBox6);	
		
		m_TableRow2 = (TableRow) findViewById(R.id.tableRow2);
		m_TableRow3 = (TableRow) findViewById(R.id.tableRow3);
		m_TableRow4 = (TableRow) findViewById(R.id.tableRow4);		
		m_TextView3 = (TextView) findViewById(R.id.textView3);
		m_TextView4 = (TextView) findViewById(R.id.textView4);
		m_TextView5 = (TextView) findViewById(R.id.textView5);
		
		// 初始化wifi、mobile状态数组
		aWifiState 		= new boolean[3];
		aMobileState 	= new boolean[3];
		
		// 读取配置文件，初始化。
		load();
		
		m_CheckBox1.setChecked(aWifiState[0]);
		m_CheckBox2.setChecked(aMobileState[0]);
		m_CheckBox3.setChecked(aWifiState[1]);
		m_CheckBox4.setChecked(aMobileState[1]);
		m_CheckBox5.setChecked(aWifiState[2]);
		m_CheckBox6.setChecked(aMobileState[2]);
				
		//对每个选项设置事件监听
		m_CheckBox1.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aWifiState[0] 	= m_CheckBox1.isChecked();
		        setSwitchRule(0);
			}
		});
		m_CheckBox2.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aMobileState[0]	= m_CheckBox2.isChecked();
		        setSwitchRule(0);
			}
		});
		m_CheckBox3.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aWifiState[1] 	= m_CheckBox3.isChecked();
		        setSwitchRule(1);
			}
		});
		m_CheckBox4.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aMobileState[1]	= m_CheckBox4.isChecked();
		        setSwitchRule(1);
			}
		});
		m_CheckBox5.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aWifiState[2] 	= m_CheckBox5.isChecked();
		        setSwitchRule(2);
			}
		});
		m_CheckBox6.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aMobileState[2]	= m_CheckBox6.isChecked();
		        setSwitchRule(2);
			}
		});
		
		m_TableRow2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Calendar calendar = aCalendar[0];
				int mHour = calendar.get(Calendar.HOUR_OF_DAY);
				int mMinute = calendar.get(Calendar.MINUTE);
				new TimePickerDialog(AutoSwitchActivity.this,
						new TimePickerDialog.OnTimeSetListener() {
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								aHour[0] = hourOfDay;
								aMinute[0] = minute;
								m_TextView3.setText(String.format("%02d",
										aHour[0])
										+ ":"
										+ String.format("%02d", aMinute[0]));
								setSwitchRule(0);
							}
						}, mHour, mMinute, true).show();

			}
		});
		
		m_TableRow3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Calendar calendar = aCalendar[1];
				int mHour = calendar.get(Calendar.HOUR_OF_DAY);
				int mMinute = calendar.get(Calendar.MINUTE);
				new TimePickerDialog(AutoSwitchActivity.this,
						new TimePickerDialog.OnTimeSetListener() {
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								aHour[1] = hourOfDay;
								aMinute[1] = minute;
								m_TextView4.setText(String.format("%02d",
										aHour[1])
										+ ":"
										+ String.format("%02d", aMinute[1]));
								setSwitchRule(1);
							}
						}, mHour, mMinute, true).show();

			}
		});
		
		m_TableRow4.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Calendar calendar = aCalendar[2];
				int mHour = calendar.get(Calendar.HOUR_OF_DAY);
				int mMinute = calendar.get(Calendar.MINUTE);
				new TimePickerDialog(AutoSwitchActivity.this,
						new TimePickerDialog.OnTimeSetListener() {
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								aHour[2] = hourOfDay;
								aMinute[2] = minute;
								m_TextView5.setText(String.format("%02d",
										aHour[2])
										+ ":"
										+ String.format("%02d", aMinute[2]));
								setSwitchRule(2);
							}
						}, mHour, mMinute, true).show();

			}
		});
		
		// 更新 TextView显示
    	m_TextView3.setText(String.format("%02d", aHour[0]) + ":" + String.format("%02d", aMinute[0]));	
    	m_TextView4.setText(String.format("%02d", aHour[1]) + ":" + String.format("%02d", aMinute[1]));	
    	m_TextView5.setText(String.format("%02d", aHour[2]) + ":" + String.format("%02d", aMinute[2]));	
		
		// 创建日历实例
        aCalendar = new Calendar[3];
        int len = aCalendar.length;
        for (int i = 0; i < len; ++i)
        {
        	aCalendar[i]=Calendar.getInstance();
        }
        setSwitchRules();
         	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_auto_switch, menu);
        return true;
    }
    
    // 更新切换规则，生成相应的alarm
    private void setSwitchRules() {
        int len = aCalendar.length;
        for (int i = 0; i < len; ++i)
        {
           	setSwitchRule(i);
        }
    }
    
    private void setSwitchRule(int i) {
        aCalendar[i].setTimeInMillis(System.currentTimeMillis());
        String time = aCalendar[i].get(Calendar.HOUR_OF_DAY) + ":" + aCalendar[i].get(Calendar.MINUTE);
    	Log.e(TAG, time);
        aCalendar[i].set(Calendar.HOUR_OF_DAY, aHour[i]);
        aCalendar[i].set(Calendar.MINUTE, aMinute[i]);
        aCalendar[i].set(Calendar.SECOND,0);
        aCalendar[i].set(Calendar.MILLISECOND,0);
  
        Intent intent = new Intent(AutoSwitchActivity.this, AlarmReceiver.class);
        intent.putExtra("wifi", aWifiState[i]);
        intent.putExtra("mobile", aMobileState[i]);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(AutoSwitchActivity.this, i, intent,  PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am;
        /* 获取闹钟管理的实例 */
        am = (AlarmManager)getSystemService(ALARM_SERVICE);
        /* 设置闹钟 周期闹 */
        am.setRepeating(AlarmManager.RTC_WAKEUP, aCalendar[i].getTimeInMillis(), (24*60*60*1000), pendingIntent);     	
    }
    
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			/* 这里我们在退出应用程序时保存数据 */
			save();

			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	// 装载、读取数据   
	void load()
	{
		/* 装载数据 */
		// 取得活动的preferences对象.
		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);

		// 取得值.
		aWifiState[0] = settings.getBoolean("bWifi0", false);
		aMobileState[0] = settings.getBoolean("bMobile0", true);		
		aWifiState[1] = settings.getBoolean("bWifi1", true);
		aMobileState[1] = settings.getBoolean("bMobile1", true);		
		aWifiState[2] = settings.getBoolean("bWifi2", false);
		aMobileState[2] = settings.getBoolean("bMobile2", false);		
	}
	
	// 保存数据 
	void save()
	{
		SharedPreferences uiState = getPreferences(0);

		// 取得编辑对象
		SharedPreferences.Editor editor = uiState.edit();

		// 添加值
		editor.putBoolean("bWifi0", aWifiState[0]);
		editor.putBoolean("bMobile0", aMobileState[0]);
		editor.putBoolean("bWifi1", aWifiState[1]);
		editor.putBoolean("bMobile1", aMobileState[1]);
		editor.putBoolean("bWifi2", aWifiState[2]);
		editor.putBoolean("bMobile2", aMobileState[2]);		
		
		// 提交保存
		editor.commit();
	}

}
