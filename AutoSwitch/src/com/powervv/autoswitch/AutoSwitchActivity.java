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
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.LinearLayout;
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

/**
 * 
 * 项目名称：com.powervv.autoswitch.AutoSwitchActivity 类名称：AutoSwitchActivity 类描述：
 * 创建人：paul 创建时间：2012-12-1 下午6:50:22 修改人：paul 修改时间：2012-12-1 下午6:50:22 修改备注：
 * 
 * @version
 * 
 */
public class AutoSwitchActivity extends Activity implements OnClickListener,
		OnLongClickListener, OnTouchListener {
	private static final String TAG = "AutoSwitch";

	/* View Id 编码: ViewId = VIEW_ID_BASE + _id * VIEW_ID_CYCLE + ID_OFFSET */
	private final static int VIEW_ID_BASE = 1000;
	private final static int VIEW_ID_CYCLE = 10;
	private final static int ROW_ID_OFFSET = 0;
	private final static int ENABLE_ID_OFFSET = ROW_ID_OFFSET + 1;
	private final static int TEXTVIEW_ID_OFFSET = ENABLE_ID_OFFSET + 1;
	private final static int WIFI_ID_OFFSET = TEXTVIEW_ID_OFFSET + 1;
	private final static int MOBILE_ID_OFFSET = WIFI_ID_OFFSET + 1;
	private final static int LINE_ID_OFFSET = MOBILE_ID_OFFSET + 1;

	/* 数据库名 */
	private final static String DATABASE_NAME = "AutoSwitch.db";

	/* 表名 */
	private final static String TABLE_NAME = "EventTable";

	/* 版本 */
	private final static int DATABASE_VERSION = 1;

	/* 表中的字段 */
	private final static String TABLE_ID = "_id";
	private final static String TABLE_HOUR = "hour";
	private final static String TABLE_MINUTE = "minute";
	private final static String TABLE_WIFI = "wifi";
	private final static String TABLE_MOBILE = "mobile";
	private final static String TABLE_ACTIVE = "active";

	/* 创建表的SQL语句 */
	private final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
			+ " (" + TABLE_ID + " INTEGER PRIMARY KEY," + TABLE_HOUR
			+ " INTERGER," + TABLE_MINUTE + " INTEGER," + TABLE_WIFI
			+ " INTEGER," + TABLE_MOBILE + " INTEGER," + TABLE_ACTIVE
			+ " INTEGER)";

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
		private Calendar mCalendar;

		Record() {
			mCalendar = Calendar.getInstance();
		}

		Record(int id, int hour, int minute, boolean wifiState,
				boolean mobileState, boolean active, Calendar calendar) {
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
		for (Record record : mRecords) {
			updateOneItemView(record);
		}

		setSwitchRules();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.menu_settings: {
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
			mSQLiteDatabase.insert(TABLE_NAME, TABLE_ID, cv);

			// 更新界面显示
			updateOneItemView(record);

			// 增加新的定时事件
			// ToDo:目前创建事件时默认active，因此在这里设置，后续改为对该事件Enable的时候再增加，这里默认是inactive的。
			// setSwitchRule(record.mId); // 已修改。
			break;
		}
		case R.id.menu_settings2: {
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
		default:
			break;
		}
		return true;
	}

	private void updateOneItemView(Record record) {
		int i = record.mId;
		TableRow row = new TableRow(this);
		row.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + ROW_ID_OFFSET);
		row.setMinimumHeight(48);
		row.setBackgroundColor(Color.WHITE);

		CheckBox activeBox = new CheckBox(this);
		activeBox.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + ENABLE_ID_OFFSET);
		activeBox.setChecked(record.mActive);
		activeBox.setOnClickListener(this);

		TextView timeView = new TextView(this);
		timeView.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + TEXTVIEW_ID_OFFSET);
		timeView.setTextSize(18);
		timeView.setText(String.format("%02d", record.mHour) + ":"
				+ String.format("%02d", record.mMinute));
		timeView.setOnClickListener(this);
		timeView.setOnLongClickListener(this);
		timeView.setOnTouchListener(this);

		CheckBox wifiBox = new CheckBox(this);
		wifiBox.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + WIFI_ID_OFFSET);
		wifiBox.setChecked(record.mWifiState);
		wifiBox.setOnClickListener(this);

		CheckBox mobileBox = new CheckBox(this);
		mobileBox.setId(VIEW_ID_BASE + i * VIEW_ID_CYCLE + MOBILE_ID_OFFSET);
		mobileBox.setChecked(record.mMobileState);
		mobileBox.setOnClickListener(this);

		row.addView(activeBox);
		row.addView(timeView);
		row.addView(wifiBox);
		row.addView(mobileBox);
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
		CheckBox box = null;
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
			box = (CheckBox) v;
			tmpRecord.mWifiState = box.isChecked();
			if (tmpRecord.mActive) {
				setSwitchRule(tmpRecord.mId);
			}
			break;
		case MOBILE_ID_OFFSET: // mobile checkbox
			box = (CheckBox) v;
			tmpRecord.mMobileState = box.isChecked();
			if (tmpRecord.mActive) {
				setSwitchRule(tmpRecord.mId);
			}
			break;
		case ENABLE_ID_OFFSET: // active checkbox
			box = (CheckBox) v;
			tmpRecord.mActive = box.isChecked();
			if (tmpRecord.mActive) {
				setSwitchRule(tmpRecord.mId);
			}
			else {
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
			Log.i("log", "action_down");
			View parentView = (View) v.getParent();
			parentView.setBackgroundColor(Color.CYAN);
			return false;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			Log.i("log", "action_up");
			View parentView = (View) v.getParent();
			parentView.setBackgroundColor(Color.WHITE);
			return false;
		} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
			Log.i("log", "action_cancel");
			View parentView = (View) v.getParent();
			parentView.setBackgroundColor(Color.WHITE);
			return false;
		}
		return false;
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
			db.execSQL(CREATE_TABLE);
			ContentValues cv = new ContentValues();
			for (int i = 0; i < 3; ++i) {
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
			Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Record record = new Record(cursor.getInt(0), // id
						cursor.getInt(1), // hour
						cursor.getInt(2), // minute
						(cursor.getInt(3) == 0) ? false : true, // wifi
						(cursor.getInt(4) == 0) ? false : true, // mobile
						(cursor.getInt(5) == 0) ? false : true, // enable
						Calendar.getInstance()); // calendar
				mRecords.add(record);
				cursor.moveToNext();
			}
			cursor.close();
		}

	}

}
