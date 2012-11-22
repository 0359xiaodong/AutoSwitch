package com.powervv.autoswitch;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;
//import android.widget.TimePicker;
import android.app.AlarmManager;
import android.app.PendingIntent;
//import android.app.TimePickerDialog;
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
	
	//6����ѡ��
	CheckBox	m_CheckBox1;
	CheckBox	m_CheckBox2;
	CheckBox	m_CheckBox3;
	CheckBox	m_CheckBox4;
	CheckBox	m_CheckBox5;
	CheckBox	m_CheckBox6;	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_switch);
        
		// ȡ��ÿ��CheckBox���� 
		m_CheckBox1 = (CheckBox) findViewById(R.id.checkBox1);
		m_CheckBox2 = (CheckBox) findViewById(R.id.checkBox2);
		m_CheckBox3 = (CheckBox) findViewById(R.id.checkBox3);
		m_CheckBox4 = (CheckBox) findViewById(R.id.checkBox4);
		m_CheckBox5 = (CheckBox) findViewById(R.id.checkBox5);
		m_CheckBox6 = (CheckBox) findViewById(R.id.checkBox6);	
		// ��ʼ��wifi��mobile״̬����
		aWifiState 		= new boolean[3];
		aMobileState 	= new boolean[3];
		aWifiState[0] 	= m_CheckBox1.isChecked();
		aMobileState[0] = m_CheckBox2.isChecked();
		aWifiState[1] 	= m_CheckBox3.isChecked();
		aMobileState[1] = m_CheckBox4.isChecked();
		aWifiState[2] 	= m_CheckBox5.isChecked();
		aMobileState[2] = m_CheckBox6.isChecked();
		
		//��ÿ��ѡ�������¼�����
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
			
		// ��������ʵ��
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
    
    // �����л�����������Ӧ��alarm
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
            /* ��ȡ���ӹ�����ʵ�� */
            am = (AlarmManager)getSystemService(ALARM_SERVICE);
            /* �������� */
            // am.set(AlarmManager.RTC_WAKEUP, aCalendar[i].getTimeInMillis(), pendingIntent);
            /* ���������� */
            am.setRepeating(AlarmManager.RTC_WAKEUP, aCalendar[i].getTimeInMillis(), (24*60*60*1000), pendingIntent);            	
        }
    }
}