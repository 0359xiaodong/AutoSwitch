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
import android.R.integer;
import android.app.Activity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
//import android.widget.CompoundButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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

import net.youmi.android.appoffers.*;

/**
 * 
 * 项目名称：com.powervv.autoswitch.AutoSwitchActivity 类名称：AutoSwitchActivity 类描述：
 * 创建人：paul 创建时间：2012-12-1 下午6:50:22 修改人：paul 修改时间：2012-12-1 下午6:50:22 修改备注：
 * 
 * @version
 * 
 */
public class AutoSwitchActivity extends Activity implements OnClickListener,
		OnLongClickListener, OnTouchListener, OnItemSelectedListener {
	private final static String TAG = "AutoSwitch";

	/* View Id 编码: ViewId = VIEW_ID_BASE + _id * VIEW_ID_CYCLE + ID_OFFSET */
	private final static int VIEW_ID_BASE = 1000;
	private final static int VIEW_ID_CYCLE = 10;
	private final static int ROW_ID_OFFSET = 0;
	private final static int ENABLE_ID_OFFSET = ROW_ID_OFFSET + 1;
	private final static int TEXTVIEW_ID_OFFSET = ENABLE_ID_OFFSET + 1;
	private final static int WIFI_ID_OFFSET = TEXTVIEW_ID_OFFSET + 1;
	private final static int MOBILE_ID_OFFSET = WIFI_ID_OFFSET + 1;
	private final static int REPEAT_ID_OFFSET = MOBILE_ID_OFFSET + 1;
	private final static int LINE_ID_OFFSET = REPEAT_ID_OFFSET + 1;

	/* 周日期 */
//	private final static String[] WEEKS = { "星期一", "星期二", "星期三", "星期四", "星期五",
//			"星期六", "星期日" };
	private final static String[] WEEKS = {"每日", "工作日", "双休日"};
	final static int  EVERY_DAY = 0;
	final static int WORK_DAY = 1;
	final static int FREE_DAY = 2;
	
	/* 数据库名 */
	private final static String DATABASE_NAME = "AutoSwitch.db";

	/* 表名 */
	private final static String TABLE_NAME = "EventTable";

	/* 版本 */
	private final static int DATABASE_VERSION = 2;

	/* 表中的字段 */
	private final static String TABLE_ID = "_id";
	private final static String TABLE_HOUR = "hour";
	private final static String TABLE_MINUTE = "minute";
	private final static String TABLE_WIFI = "wifi";
	private final static String TABLE_MOBILE = "mobile";
	private final static String TABLE_ACTIVE = "active";
	private final static String TABLE_REPEAT = "repeat";

	/* 创建表的SQL语句 */
	private final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
			+ " (" + TABLE_ID + " INTEGER PRIMARY KEY," + TABLE_HOUR
			+ " INTERGER," + TABLE_MINUTE + " INTEGER," + TABLE_WIFI
			+ " INTEGER," + TABLE_MOBILE + " INTEGER," + TABLE_ACTIVE
			+ " INTEGER," + TABLE_REPEAT + " INTEGER)";

	/* 删除表的SQL语句 */
	private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	
	/* 添加表Colum的语句 */
	private final static String ALTER_TABLE = "ALTER TABLE " + TABLE_NAME
			+ " ADD COLUMN " + TABLE_REPEAT + " INTEGER";
	
	/* 数据库对象 */
	private SQLiteDatabase mSQLiteDatabase = null;

	private TableLayout mTable = null;

	private ArrayList<Record> mRecords = null;
	
	private class Record {
		private int mId;
		private int mHour;
		private int mMinute;
		private boolean mWifiState;
		private boolean mMobileState;
		private boolean mActive;
		private int mRepeat;
		private Calendar mCalendar;

		Record() {
			mCalendar = Calendar.getInstance();
		}

		Record(int id, int hour, int minute, boolean wifiState,
				boolean mobileState, boolean active, int repeat, Calendar calendar) {
			mId = id;
			mHour = hour;
			mMinute = minute;
			mWifiState = wifiState;
			mMobileState = mobileState;
			mActive = active;
			mRepeat = repeat;
			mCalendar = calendar;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tableview);

		YoumiOffersManager.init(this, "50cd2ee7ad94103d", "8e22a1db3c5b0143");
		
		final ImageButton imgButton = (ImageButton)findViewById(R.id.imageButton);
		 imgButton.setOnClickListener(new View.OnClickListener() {	             
		             @Override
		             public void onClick(View v) {
		     			addOneItem();
		             }
		         });
			 
		mRecords = new ArrayList<Record>();
		// 读取配置文件，初始化。
		load();

		// android:background="#CC0B0B3B">
		
		// 更新界面显示
		mTable = (TableLayout) findViewById(R.id.table);
		for (Record record : mRecords) {
			updateOneItemView(record);
		}

		setSwitchRules();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.menu_setting_add: {
			addOneItem();
			break;
		}
		case R.id.menu_setting_delete: {			
			Dialog dialog = new AlertDialog.Builder(this).setTitle("提示")
					.setMessage("确认要删除所有条目吗？")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							for (Record record : mRecords) {
								// 清理数据库
								mSQLiteDatabase.delete(TABLE_NAME, TABLE_ID + "=" + record.mId,
										null);
								// 取消定时事件
								if (record.mActive) {
									cancelSwitchRule(record.mId);
								}
							}
							// 删除数组元素
							mRecords.clear();
							mTable.removeViews(2, mTable.getChildCount() - 2);
						}
					}).setNegativeButton("退出", null).create();
			dialog.show();	
			break;
		}
		case R.id.menu_setting_about: {
			Intent intent = new Intent();
			intent.setClass(AutoSwitchActivity.this, AboutActivity.class);
			startActivity(intent);
			save();
			mSQLiteDatabase.close();
			AutoSwitchActivity.this.finish();
			break;
		}
		default:
			break;
		}
		return true;
	}
	
	private void addOneItem() {
		// 无积分时，最多支持三个定时任务
		if (mRecords.size() >=3 && YoumiPointsManager.queryPoints(this) == 0) {
			Dialog dialog = new AlertDialog.Builder(this).setTitle("提示")
					.setMessage("免费下载任一推荐应用，即可无限制添加定时任务。是否前往？")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							YoumiOffersManager.showOffers(AutoSwitchActivity.this, YoumiOffersManager.TYPE_REWARD_OFFERS);
						}
					}).setNegativeButton("退出", null).create();
			dialog.show();											
			return;
		}
					
		// 添加新记录到数组
		Record record = new Record();
		Calendar calendar = record.mCalendar;
		long curTime = System.currentTimeMillis();
		calendar.setTimeInMillis(curTime);
		record.mHour = calendar.get(Calendar.HOUR_OF_DAY);
		record.mMinute = calendar.get(Calendar.MINUTE);
		if (mRecords.size() != 0) {
			record.mId = mRecords.get(mRecords.size() - 1).mId + 1;
		}
		mRecords.add(record);

		// 更新数据库
		ContentValues cv = new ContentValues();
		cv.put(TABLE_ID, record.mId);
		cv.put(TABLE_HOUR, record.mHour);
		cv.put(TABLE_MINUTE, record.mMinute);
		cv.put(TABLE_WIFI, record.mWifiState);
		cv.put(TABLE_MOBILE, record.mMobileState);
		cv.put(TABLE_ACTIVE, record.mActive);
		cv.put(TABLE_REPEAT, record.mRepeat);
		mSQLiteDatabase.insert(TABLE_NAME, TABLE_ID, cv);

		// 更新界面显示
		updateOneItemView(record);		
	}

	private void updateOneItemView(Record record) {
		int i = record.mId;
		TableRow row = new TableRow(this);
		row.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + ROW_ID_OFFSET);
		row.setMinimumHeight(48);
		if (record.mActive) {
			row.setBackgroundColor(Color.WHITE);
			//row.setBackgroundResource(R.drawable.row_on);
		} else {
			row.setBackgroundColor(Color.LTGRAY);
			//row.setBackgroundResource(R.drawable.row_off);
		}
		
		ToggleButton activeBox = new ToggleButton(this);
		activeBox.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + ENABLE_ID_OFFSET);
		activeBox.setBackgroundResource(R.drawable.button_switch);
		activeBox.setChecked(record.mActive);
		activeBox.setOnClickListener(this);
		activeBox.setText("");
		activeBox.setTextOn("");
		activeBox.setTextOff("");
		
		TextView timeView = new TextView(this);
		timeView.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + TEXTVIEW_ID_OFFSET);
		timeView.setTextSize(24);
		timeView.setText(String.format("%02d", record.mHour) + ":"
				+ String.format("%02d", record.mMinute));
		timeView.setOnClickListener(this);
		timeView.setOnLongClickListener(this);
		timeView.setOnTouchListener(this);

		ToggleButton wifiBox = new ToggleButton(this);
		wifiBox.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + WIFI_ID_OFFSET);
		wifiBox.setBackgroundResource(R.drawable.button_wifi);
		wifiBox.setChecked(record.mWifiState);
		wifiBox.setOnClickListener(this);
		wifiBox.setText("");
		wifiBox.setTextOn("");
		wifiBox.setTextOff("");
	
		ToggleButton mobileBox = new ToggleButton(this);
		mobileBox.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + MOBILE_ID_OFFSET);
		mobileBox.setBackgroundResource(R.drawable.button_mobile);
		mobileBox.setChecked(record.mMobileState);
		mobileBox.setOnClickListener(this);
		mobileBox.setText("");
		mobileBox.setTextOn("");
		mobileBox.setTextOff("");		
		
		Spinner spinner = new Spinner(this);
		spinner.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + REPEAT_ID_OFFSET);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, WEEKS);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(record.mRepeat);
		spinner.setOnItemSelectedListener(this);
		
		TableRow.LayoutParams params = new TableRow.LayoutParams();
		params.setMargins(0, 8, 0, 8);
		params.gravity = Gravity.CENTER_VERTICAL;
		row.addView(activeBox, params);
		row.addView(timeView, params);
		row.addView(spinner, params);
		row.addView(wifiBox, params);
		row.addView(mobileBox, params);
		mTable.addView(row);

		View cutLine = new View(this);
		cutLine.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + LINE_ID_OFFSET);
		cutLine.setBackgroundColor(Color.parseColor("#FFE6E6E6"));
		cutLine.setMinimumHeight(1);
		mTable.addView(cutLine);
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
	public void onClick(final View v) {
		int id = v.getId();
		int recordId = (id - VIEW_ID_BASE) / VIEW_ID_CYCLE;
		int viewOffset = (id - VIEW_ID_BASE) % VIEW_ID_CYCLE;
		ToggleButton box = null;
		final Record tmpRecord = getRecordbyId(recordId);
		switch (viewOffset) {
		case TEXTVIEW_ID_OFFSET: // textview
			Calendar calendar = tmpRecord.mCalendar;
			int mHour = calendar.get(Calendar.HOUR_OF_DAY);
			int mMinute = calendar.get(Calendar.MINUTE);

			new TimePickerDialog(AutoSwitchActivity.this,
					new TimePickerDialog.OnTimeSetListener() {
						public void onTimeSet(TimePicker view, int hourOfDay,
								int minute) {
							TextView textView = (TextView) v;
							tmpRecord.mHour = hourOfDay;
							tmpRecord.mMinute = minute;
							textView.setText(String.format("%02d",
									tmpRecord.mHour)
									+ ":"
									+ String.format("%02d", tmpRecord.mMinute));
							if (tmpRecord.mActive) {
								setSwitchRule(tmpRecord.mId);					
							}

						}
					}, mHour, mMinute, true).show();

			break;
		case WIFI_ID_OFFSET: // wifi checkbox
			box = (ToggleButton) v;
			tmpRecord.mWifiState = box.isChecked();
			if (tmpRecord.mActive) {
				setSwitchRule(tmpRecord.mId);
			}
			break;
		case MOBILE_ID_OFFSET: // mobile checkbox
			box = (ToggleButton) v;
			tmpRecord.mMobileState = box.isChecked();
			if (tmpRecord.mActive) {
				setSwitchRule(tmpRecord.mId);
			}
			break;
		case ENABLE_ID_OFFSET: // active checkbox
			box = (ToggleButton) v;
			tmpRecord.mActive = box.isChecked();
			View row = (View)v.getParent();
			if (tmpRecord.mActive) {
				//row.setBackgroundResource(R.drawable.row_on);
				row.setBackgroundColor(Color.WHITE);
				setSwitchRule(tmpRecord.mId);
				int wifiInfoIdx = tmpRecord.mWifiState ? 0 : 1;
				int mobileInfoIdx = tmpRecord.mMobileState ? 0 : 1;
				Toast.makeText(
						AutoSwitchActivity.this,
						"已设置" + String.format("%02d", tmpRecord.mHour) + ":" 
						+ String.format("%02d", tmpRecord.mMinute) 
						+ ": " + AlarmReceiver.statesInfo[wifiInfoIdx] + "Wifi, "
						+ AlarmReceiver.statesInfo[mobileInfoIdx] + "数据", Toast.LENGTH_LONG).show();
			}
			else {
				//row.setBackgroundResource(R.drawable.row_off);
				row.setBackgroundColor(Color.LTGRAY);
				cancelSwitchRule(tmpRecord.mId);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onLongClick(final View v) {
		Dialog dialog = new AlertDialog.Builder(this).setTitle("提示")
				.setMessage("确认要删除此条目吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int id = (v.getId() - VIEW_ID_BASE) / VIEW_ID_CYCLE;
						Record record = getRecordbyId(id);

						// 删除界面元素 （包括后面的分割线）
						int index = mTable.indexOfChild((View) v.getParent());
						mTable.removeViews(index, 2);

						// 清理数据库
						mSQLiteDatabase.delete(TABLE_NAME, TABLE_ID + "="
								+ record.mId, null);

						// 取消定时事件
						if (record.mActive) {
							cancelSwitchRule(record.mId);
						}

						// 删除数组元素
						mRecords.remove(record);
					}
				}).setNegativeButton("退出", null).create();
		dialog.show();
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			View parentView = (View) v.getParent();
			parentView.setBackgroundColor(Color.CYAN);

		} else if (event.getAction() == MotionEvent.ACTION_UP
				|| event.getAction() == MotionEvent.ACTION_CANCEL) {
			int id = (v.getId() - VIEW_ID_BASE) / VIEW_ID_CYCLE;
			Record record = getRecordbyId(id);
			View parentView = (View) v.getParent();
			parentView.setBackgroundColor(record.mActive ? Color.WHITE
					: Color.LTGRAY);
		}
		return false;

	}
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		int id = arg0.getId();
		int recordId = (id - VIEW_ID_BASE) / VIEW_ID_CYCLE;
		final Record tmpRecord = getRecordbyId(recordId);
		tmpRecord.mRepeat = arg2;
		if (tmpRecord.mActive) {
			setSwitchRule(tmpRecord.mId);
		}
		//设置显示当前选择的项
		arg0.setVisibility(View.VISIBLE);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}

	private Record getRecordbyId(int id) {
		for (Record record : mRecords) {
			if (record.mId == id)
				return record;
		}
		return null;
	}

	// 更新切换规则，生成相应的alarm
	private void setSwitchRules() {
		for (Record record : mRecords) {
			if (record.mActive) {
				setSwitchRule(record.mId);
			}
		}
	}

	private void setSwitchRule(int i) {
		Record record = getRecordbyId(i);
		Calendar calendar = record.mCalendar;
		long curTime = System.currentTimeMillis();
		calendar.setTimeInMillis(curTime);
		// String time = aCalendar[i].get(Calendar.HOUR_OF_DAY) + ":" +
		// aCalendar[i].get(Calendar.MINUTE);
		// Log.e(TAG, time);
		calendar.set(Calendar.HOUR_OF_DAY, record.mHour);
		calendar.set(Calendar.MINUTE, record.mMinute);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long setTime = calendar.getTimeInMillis();
		if (setTime < curTime) {
			setTime += (24 * 60 * 60 * 1000);
			calendar.setTimeInMillis(setTime);
		}

		Intent intent = new Intent(AutoSwitchActivity.this, AlarmReceiver.class);
		intent.putExtra("wifi", record.mWifiState);
		intent.putExtra("mobile", record.mMobileState);
		intent.putExtra("weekday", record.mRepeat);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				AutoSwitchActivity.this, i, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am;
		/* 获取闹钟管理的实例 */
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		/* 设置闹钟 周期闹 */
		am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				(24 * 60 * 60 * 1000), pendingIntent);
	}

	private void cancelSwitchRule(int i) {
		Intent intent = new Intent(AutoSwitchActivity.this, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				AutoSwitchActivity.this, i, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am;
		/* 获取闹钟管理的实例 */
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(pendingIntent);

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
			cv.put(TABLE_REPEAT, record.mRepeat);
			mSQLiteDatabase.update(TABLE_NAME, cv, TABLE_ID + "=" + record.mId,
					null);
		}
	}

	public class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context, String name,
				CursorFactory cursorFactory, int version) {
			super(context, name, cursorFactory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO 创建数据库后，对数据库的操作
			/* 在数据库mSQLiteDatabase中创建一个表 */
			Log.d(TAG, "Create Database and talbe and item!");
			int hours[] = { 7, 19, 23 };
			int minutes[] = { 0, 0, 30 };
			boolean wifiStates[] = { false, true, false };
			boolean mobileStates[] = { true, true, false };
			boolean actives[] = { false, false, false };
			int repeats[] = {AutoSwitchActivity.EVERY_DAY, AutoSwitchActivity.EVERY_DAY, AutoSwitchActivity.EVERY_DAY};
			db.execSQL(CREATE_TABLE);
			ContentValues cv = new ContentValues();
			for (int i = 0; i < 3; ++i) {
				cv.put(TABLE_ID, i);
				cv.put(TABLE_HOUR, hours[i]);
				cv.put(TABLE_MINUTE, minutes[i]);
				cv.put(TABLE_WIFI, wifiStates[i]);
				cv.put(TABLE_MOBILE, mobileStates[i]);
				cv.put(TABLE_ACTIVE, actives[i]);
				cv.put(TABLE_REPEAT, repeats[i]);
				/* 插入数据 */
				db.insert(TABLE_NAME, null, cv);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO 更改数据库版本的操作
			Log.d(TAG, "Upgrade Database!");
			if (AutoSwitchActivity.DATABASE_VERSION == newVersion
					&& oldVersion < newVersion) {
				db.execSQL(ALTER_TABLE);
			}
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			// TODO 每次成功打开数据库后首先被执行
			/* 读取表中的数据 */
			Log.d(TAG, "Open Database and read talbe!");
			int version = db.getVersion();
			mRecords.clear();
			Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
			cursor.moveToFirst();
			
			while (!cursor.isAfterLast()) {
				Record record = new Record(cursor.getInt(0), // id
						cursor.getInt(1), // hour
						cursor.getInt(2), // minute
						(cursor.getInt(3) == 0) ? false : true, // wifi
						(cursor.getInt(4) == 0) ? false : true, // mobile
						(cursor.getInt(5) == 0) ? false : true, // enable
						(version == AutoSwitchActivity.DATABASE_VERSION) ? cursor.getInt(6) 
								: AutoSwitchActivity.EVERY_DAY, // repeat
						Calendar.getInstance()); // calendar
				mRecords.add(record);
				cursor.moveToNext();
			}
			cursor.close();
		}
		
	}

}
