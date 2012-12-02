/**
* 文件名：AutoSwitchActivity.java
*
* 版本信息：
* 日期：2012-11-22
* Copyright powervv.com 2012
* 版权所有
*
*/
package com.powervv.autoswitch;

import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TableLayout;
//import android.widget.CompoundButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.app.TimePickerDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;


/**
*
* 项目名称：com.powervv.autoswitch.AutoSwitchActivity
* 类名称：AutoSwitchActivity
* 类描述：
* 创建人：paul
* 创建时间：2012-12-1 下午6:50:22
* 修改人：paul
* 修改时间：2012-12-1 下午6:50:22
* 修改备注：
* @version
*
*/
public class AutoSwitchActivity extends Activity implements OnClickListener {
	private static final String	TAG	= "AutoSwitch";
	
	/* 数据库名 */
	private final static String	DATABASE_NAME	= "AutoSwitch.db";
	
	/* 表名 */
	private final static String	TABLE_NAME		= "EventTable";
	
	/* 版本 */
	private final static int 	DATABASE_VERSION = 1;
	
	/* 表中的字段 */
	private final static String	TABLE_ID		= "_id";
	private final static String	TABLE_HOUR		= "hour";
	private final static String	TABLE_MINUTE	= "minute";
	private final static String TABLE_WIFI		= "wifi";
	private final static String TABLE_MOBILE	= "mobile";
	private final static String TABLE_ACTIVE	= "active";
	
	/* 创建表的SQL语句 */
	private final static String	CREATE_TABLE	= "CREATE TABLE " + TABLE_NAME + 
		" (" + TABLE_ID + " INTEGER PRIMARY KEY," + TABLE_HOUR + " INTERGER,"+ TABLE_MINUTE + " INTEGER," +
			TABLE_WIFI + " INTEGER," + TABLE_MOBILE + " INTEGER," + TABLE_ACTIVE + " INTEGER)";
	
	/* 数据库对象 */
	private SQLiteDatabase		mSQLiteDatabase	= null;
		
	private TableLayout mTable = null;
	private View mView = null;
	private Record mTmpRecord;

	private ArrayList<Record> mRecords = null;
	
	private class Record {
		private int mId;
		private int mHour;
		private int mMinute;
		private boolean mWifiState;
		private boolean mMobileState;
		private boolean mActive;
		private Calendar mCalendar;
		
		Record(){
			mId = 0;
			mHour = 0;
			mMinute = 0;
			mWifiState = false;
			mMobileState = false;
			mActive = false;
			mCalendar = null;
		}
		Record(int id, int hour, int minute, boolean wifiState, boolean mobileState, boolean active, Calendar calendar){
			mId = id;
			mHour = hour;
			mMinute = minute;
			mWifiState = wifiState;
			mMobileState = mobileState;
			mActive = active;
			mCalendar = calendar;
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tableview);
		
        mRecords = new ArrayList<Record>();
        
        // 读取配置文件，初始化。
		load();
        
		// 更新界面显示
		mTable = (TableLayout) findViewById(R.id.table);
		int len = mRecords.size();
		int idx = 0;
		for (Record record : mRecords) {
			int i = record.mId;
			TableRow row = new TableRow(this);
			
			TextView timeView = new TextView(this);
			timeView.setId(i*3);
			timeView.setText(String.format("%02d", record.mHour) + ":" + String.format("%02d", record.mMinute));	
			timeView.setOnClickListener(this);

			CheckBox wifiBox = new CheckBox(this);
			wifiBox.setId(i*3 + 1);			
			wifiBox.setChecked(record.mWifiState);
			wifiBox.setOnClickListener(this); 			
			
			CheckBox mobileBox = new CheckBox(this);
			mobileBox.setId(i*3 + 2);
			mobileBox.setChecked(record.mMobileState);
			mobileBox.setOnClickListener(this); 			
			
			row.addView(timeView);
			row.addView(wifiBox);
			row.addView(mobileBox);
			mTable.addView(row);
			
			View cutLine = new View(this);
			if (idx < len-1) cutLine.setBackgroundColor(Color.parseColor("#FFE6E6E6"));
			else cutLine.setBackgroundColor(Color.parseColor("#FF909090"));
			cutLine.setMinimumHeight(2);
			//cutLine.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));  
			mTable.addView(cutLine);
			++idx;
		}
		
        setSwitchRules();
         	
    }

    @Override
	public void onSaveInstanceState(Bundle outState) {
		/* 这里我们在退出应用程序时保存数据 */
		save();
		super.onSaveInstanceState(outState);
	}
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			/* 这里我们在退出应用程序时保存数据 */
			save();
			mSQLiteDatabase.close();
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_auto_switch, menu);
        return true;
    }
    
    @Override
    public void onClick(View v) {  
    	int id = v.getId();
    	int row = id / 3;
    	int colum = id % 3;
    	CheckBox box = null;
		mView = v;    	
		mTmpRecord = getRecordbyId(row);    	
    	switch (colum)
    	{
    	case 0: // textview

			Calendar calendar = mTmpRecord.mCalendar; //mCalendars[row];
			int mHour = calendar.get(Calendar.HOUR_OF_DAY);
			int mMinute = calendar.get(Calendar.MINUTE);

			new TimePickerDialog(AutoSwitchActivity.this,
					new TimePickerDialog.OnTimeSetListener() {
						public void onTimeSet(TimePicker view,
								int hourOfDay, int minute) {
					    	int row = mView.getId() / 3;
					    	TextView textView =  (TextView)mView;
					    	mTmpRecord.mHour = hourOfDay;
					    	mTmpRecord.mMinute = minute;
							textView.setText(String.format("%02d",
									mTmpRecord.mHour)
									+ ":"
									+ String.format("%02d", mTmpRecord.mMinute));
							setSwitchRule(row);
						}
					}, mHour, mMinute, true).show(); 

    		break;
    	case 1: // wifi checkbox
    		box = (CheckBox)v;
    		mTmpRecord.mWifiState 	= box.isChecked();
    		break;
    	case 2: // mobile checkbox
    		box = (CheckBox)v;
    		mTmpRecord.mMobileState = box.isChecked();	
    		break;
    	default:
    		break;
    	}
    	
		mView = null;
		mTmpRecord = null;
    }  
    
    private Record getRecordbyId(int id) {
    	for (Record record : mRecords){
    		if (record.mId == id)
    			return record;
    	}   	
    	return null;
    }
    
    // 更新切换规则，生成相应的alarm
    private void setSwitchRules() {
        int len = mRecords.size();
        for (int i = 0; i < len; ++i)
        {
           	setSwitchRule(i);
        }
    }
    
    private void setSwitchRule(int i) {
    	Record record = mRecords.get(i);
    	Calendar calendar = record.mCalendar;
    	long curTime = System.currentTimeMillis();
    	calendar.setTimeInMillis(curTime);
        //String time = aCalendar[i].get(Calendar.HOUR_OF_DAY) + ":" + aCalendar[i].get(Calendar.MINUTE);
    	//Log.e(TAG, time);
    	calendar.set(Calendar.HOUR_OF_DAY, record.mHour);
    	calendar.set(Calendar.MINUTE, record.mMinute);
    	calendar.set(Calendar.SECOND,0);
    	calendar.set(Calendar.MILLISECOND,0);
        long setTime = calendar.getTimeInMillis();
        if (setTime < curTime) {
        	setTime += (24*60*60*1000);
        	calendar.setTimeInMillis(setTime);
        }
        	
        Intent intent = new Intent(AutoSwitchActivity.this, AlarmReceiver.class);
        intent.putExtra("wifi", record.mWifiState);
        intent.putExtra("mobile", record.mMobileState);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(AutoSwitchActivity.this, i, intent,  PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am;
        /* 获取闹钟管理的实例 */
        am = (AlarmManager)getSystemService(ALARM_SERVICE);
        /* 设置闹钟 周期闹 */
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), (24*60*60*1000), pendingIntent);     	
    }
    
	// 装载、读取数据   
	void load() {
		if (null == mSQLiteDatabase) {
			DatabaseHelper dbHelper = new DatabaseHelper(this, DATABASE_NAME,
					null, DATABASE_VERSION);
			try {
				mSQLiteDatabase = dbHelper.getWritableDatabase();
			} catch (SQLiteException ex) {
				mSQLiteDatabase = dbHelper.getReadableDatabase();
			}
		}
	}
		
	// 保存数据 
	void save() {
		Log.d(TAG, "Update table item and quit!");
		ContentValues cv = new ContentValues();
		for (Record record : mRecords) {
			cv.put(TABLE_ID, record.mId);
			cv.put(TABLE_HOUR, record.mHour);
			cv.put(TABLE_MINUTE, record.mMinute);
			cv.put(TABLE_WIFI, record.mWifiState);
			cv.put(TABLE_MOBILE, record.mMobileState);
			cv.put(TABLE_ACTIVE, record.mActive);
			mSQLiteDatabase.update(TABLE_NAME, cv, TABLE_ID + "=" + record.mId, null);
		}
	}
	
	public class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context, String name, CursorFactory cursorFactory,
				int version) {
			super(context, name, cursorFactory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO 创建数据库后，对数据库的操作
			/* 在数据库mSQLiteDatabase中创建一个表 */
			Log.d(TAG, "Create Database and talbe and item!");
			int hours[] 	= 	{7, 19, 23}; 
			int minutes[] 	= 	{0, 0, 30}; 
			boolean wifiStates[] = {false, true, false};
			boolean mobileStates[] = {true, true, false};
			boolean actives[] = {true, true, true};			
			db.execSQL(CREATE_TABLE);	
			ContentValues cv = new ContentValues();				
			for (int i = 0; i < 3; ++i)
			{
				cv.put(TABLE_ID, i);
				cv.put(TABLE_HOUR, hours[i]);
				cv.put(TABLE_MINUTE, minutes[i]);
				cv.put(TABLE_WIFI, wifiStates[i]);
				cv.put(TABLE_MOBILE, mobileStates[i]);
				cv.put(TABLE_ACTIVE, actives[i]);
				/* 插入数据 */
				db.insert(TABLE_NAME, null, cv);				
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO 更改数据库版本的操作
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			// TODO 每次成功打开数据库后首先被执行
			/* 读取表中的数据 */
			Log.d(TAG, "Open Database and read talbe!");
			mRecords.clear();
			Cursor cursor = db.rawQuery(
					"SELECT * FROM " + TABLE_NAME, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Record record = new Record(cursor.getInt(0), // id
											cursor.getInt(1), // hour
											cursor.getInt(2), // minute
											(cursor.getInt(3) == 0)? false:true, // wifi
											(cursor.getInt(4) == 0)? false:true, // mobile
											(cursor.getInt(5) == 0)? false:true, // enable
											Calendar.getInstance()); // calendar
				mRecords.add(record);
				cursor.moveToNext();
			}
			cursor.close();
		}
		
	}  	

}


