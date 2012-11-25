package com.powervv.autoswitch;

import android.os.Bundle;
import android.app.Activity;
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
import android.content.Intent;
import java.util.Calendar;
import android.util.Log;

public class AutoSwitchActivity extends Activity {
	private static final String	TAG	= "AutoSwitch";
	Calendar aCalendar[];
	int aHour[] 	= 	{7, 19, 23}; // {22, 22, 22}; // 
	int aMinute[] 	= 	{0, 0, 0}; // {52, 53, 54}; // 
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
	
    static public String padLeft(String oriStr,int len,char alexin){
    	String str = new String();  
    	int strlen = oriStr.length();
    	  if(strlen < len){
    	   for(int i=0;i<len-strlen;i++){
    	    str = str+alexin;
    	   }
    	  }
    	  str += oriStr;
    	  return str;
    }
    
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
		aWifiState[0] 	= m_CheckBox1.isChecked();
		aMobileState[0] = m_CheckBox2.isChecked();
		aWifiState[1] 	= m_CheckBox3.isChecked();
		aMobileState[1] = m_CheckBox4.isChecked();
		aWifiState[2] 	= m_CheckBox5.isChecked();
		aMobileState[2] = m_CheckBox6.isChecked();
		
		//对每个选项设置事件监听
		m_CheckBox1.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aWifiState[0] 	= m_CheckBox1.isChecked();
		        setSwitchRule();
			}
		});
		m_CheckBox2.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aMobileState[0]	= m_CheckBox2.isChecked();
		        setSwitchRule();
			}
		});
		m_CheckBox3.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aWifiState[1] 	= m_CheckBox3.isChecked();
		        setSwitchRule();
			}
		});
		m_CheckBox4.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aMobileState[1]	= m_CheckBox4.isChecked();
		        setSwitchRule();
			}
		});
		m_CheckBox5.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aWifiState[2] 	= m_CheckBox5.isChecked();
		        setSwitchRule();
			}
		});
		m_CheckBox6.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				aMobileState[2]	= m_CheckBox6.isChecked();
		        setSwitchRule();
			}
		});
		
		m_TableRow2.setOnClickListener(new View.OnClickListener()
	    {
	      public void onClick(View v)
	      {
	    	Calendar calendar = Calendar.getInstance();
	    	calendar.setTimeInMillis(System.currentTimeMillis());
	        int mHour=calendar.get(Calendar.HOUR_OF_DAY);
	        int mMinute=calendar.get(Calendar.MINUTE);
	        new TimePickerDialog(AutoSwitchActivity.this,
	          new TimePickerDialog.OnTimeSetListener()
	          {                
	            public void onTimeSet(TimePicker view,int hourOfDay,int minute)
	            {
	            	aHour[0] 	= hourOfDay;
	            	aMinute[0] 	= minute;
	            	m_TextView3.setText(AutoSwitchActivity.padLeft(String.valueOf(aHour[0]), 2, '0') + ":" + AutoSwitchActivity.padLeft(String.valueOf(aMinute[0]), 2, '0'));
	            	
	    	        setSwitchRule();
	            }          
	          },mHour,mMinute,true).show();
	        
	      }
	    });		
		
		m_TableRow3.setOnClickListener(new View.OnClickListener()
	    {
	      public void onClick(View v)
	      {
	    	Calendar calendar = Calendar.getInstance();
	    	calendar.setTimeInMillis(System.currentTimeMillis());
	        int mHour=calendar.get(Calendar.HOUR_OF_DAY);
	        int mMinute=calendar.get(Calendar.MINUTE);
	        new TimePickerDialog(AutoSwitchActivity.this,
	          new TimePickerDialog.OnTimeSetListener()
	          {                
	            public void onTimeSet(TimePicker view,int hourOfDay,int minute)
	            {
	            	aHour[1] 	= hourOfDay;
	            	aMinute[1] 	= minute;
	            	m_TextView4.setText(AutoSwitchActivity.padLeft(String.valueOf(aHour[1]), 2, '0') + ":" + AutoSwitchActivity.padLeft(String.valueOf(aMinute[1]), 2, '0'));
	            	setSwitchRule();
	            }          
	          },mHour,mMinute,true).show();
	        
	      }
	    });	
		
		m_TableRow4.setOnClickListener(new View.OnClickListener()
	    {
	      public void onClick(View v)
	      {
	    	Calendar calendar = Calendar.getInstance();
	    	calendar.setTimeInMillis(System.currentTimeMillis());
	        int mHour=calendar.get(Calendar.HOUR_OF_DAY);
	        int mMinute=calendar.get(Calendar.MINUTE);
	        new TimePickerDialog(AutoSwitchActivity.this,
	          new TimePickerDialog.OnTimeSetListener()
	          {                
	            public void onTimeSet(TimePicker view,int hourOfDay,int minute)
	            {
	            	aHour[2] 	= hourOfDay;
	            	aMinute[2] 	= minute;
	            	m_TextView5.setText(AutoSwitchActivity.padLeft(String.valueOf(aHour[2]), 2, '0') + ":" + AutoSwitchActivity.padLeft(String.valueOf(aMinute[2]), 2, '0'));
	    	        setSwitchRule();
	            }          
	          },mHour,mMinute,true).show();
	        
	      }
	    });	
		
		// 创建日历实例
        aCalendar = new Calendar[3];
        int len = aCalendar.length;
        for (int i = 0; i < len; ++i)
        {
        	aCalendar[i]=Calendar.getInstance();
        }
        setSwitchRule();
         	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_auto_switch, menu);
        return true;
    }
    
    // 更新切换规则，生成相应的alarm
    private void setSwitchRule() {
        int len = aCalendar.length;
        for (int i = 0; i < len; ++i)
        {
        	//aCalendar[i]=Calendar.getInstance();
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

            PendingIntent pendingIntent=PendingIntent.getBroadcast(AutoSwitchActivity.this, i, intent,  PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am;
            /* 获取闹钟管理的实例 */
            am = (AlarmManager)getSystemService(ALARM_SERVICE);
            /* 设置闹钟 */
            // am.set(AlarmManager.RTC_WAKEUP, aCalendar[i].getTimeInMillis(), pendingIntent);
            /* 设置周期闹 */
            am.setRepeating(AlarmManager.RTC_WAKEUP, aCalendar[i].getTimeInMillis(), (24*60*60*1000), pendingIntent);            	
        }
    }
    

}
