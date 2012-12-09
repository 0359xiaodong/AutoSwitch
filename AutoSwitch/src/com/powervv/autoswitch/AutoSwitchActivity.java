/**
 * �ļ�����AutoSwitchActivity.java
 *
 * �汾��Ϣ��
 * ���ڣ�2012-11-22
 * Copyright powervv.com 2012
 * ��Ȩ����
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
 * ��Ŀ���ƣ�com.powervv.autoswitch.AutoSwitchActivity �����ƣ�AutoSwitchActivity ��������
 * �����ˣ�paul ����ʱ�䣺2012-12-1 ����6:50:22 �޸��ˣ�paul �޸�ʱ�䣺2012-12-1 ����6:50:22 �޸ı�ע��
 * 
 * @version
 * 
 */
public class AutoSwitchActivity extends Activity implements OnClickListener,
		OnLongClickListener, OnTouchListener {
	private static final String TAG = "AutoSwitch";

	/* View Id ����: ViewId = VIEW_ID_BASE + _id * VIEW_ID_CYCLE + ID_OFFSET */
	private final static int VIEW_ID_BASE = 1000;
	private final static int VIEW_ID_CYCLE = 10;
	private final static int ROW_ID_OFFSET = 0;
	private final static int ENABLE_ID_OFFSET = ROW_ID_OFFSET + 1;
	private final static int TEXTVIEW_ID_OFFSET = ENABLE_ID_OFFSET + 1;
	private final static int WIFI_ID_OFFSET = TEXTVIEW_ID_OFFSET + 1;
	private final static int MOBILE_ID_OFFSET = WIFI_ID_OFFSET + 1;
	private final static int LINE_ID_OFFSET = MOBILE_ID_OFFSET + 1;

	/* ���ݿ��� */
	private final static String DATABASE_NAME = "AutoSwitch.db";

	/* ���� */
	private final static String TABLE_NAME = "EventTable";

	/* �汾 */
	private final static int DATABASE_VERSION = 1;

	/* ���е��ֶ� */
	private final static String TABLE_ID = "_id";
	private final static String TABLE_HOUR = "hour";
	private final static String TABLE_MINUTE = "minute";
	private final static String TABLE_WIFI = "wifi";
	private final static String TABLE_MOBILE = "mobile";
	private final static String TABLE_ACTIVE = "active";

	/* �������SQL��� */
	private final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
			+ " (" + TABLE_ID + " INTEGER PRIMARY KEY," + TABLE_HOUR
			+ " INTERGER," + TABLE_MINUTE + " INTEGER," + TABLE_WIFI
			+ " INTEGER," + TABLE_MOBILE + " INTEGER," + TABLE_ACTIVE
			+ " INTEGER)";

	/* ���ݿ���� */
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

		// ��ȡ�����ļ�����ʼ����
		load();

		// ���½�����ʾ
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
			// ����¼�¼������
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

			// �������ݿ�
			ContentValues cv = new ContentValues();
			cv.put(TABLE_ID, record.mId);
			cv.put(TABLE_HOUR, record.mHour);
			cv.put(TABLE_MINUTE, record.mMinute);
			cv.put(TABLE_WIFI, record.mWifiState);
			cv.put(TABLE_MOBILE, record.mMobileState);
			cv.put(TABLE_ACTIVE, record.mActive);
			mSQLiteDatabase.insert(TABLE_NAME, TABLE_ID, cv);

			// ���½�����ʾ
			updateOneItemView(record);

			// �����µĶ�ʱ�¼�
			// ToDo:Ŀǰ�����¼�ʱĬ��active��������������ã�������Ϊ�Ը��¼�Enable��ʱ�������ӣ�����Ĭ����inactive�ġ�
			// setSwitchRule(record.mId); // ���޸ġ�
			break;
		}
		case R.id.menu_settings2: {
			for (Record record : mRecords) {
				// �������ݿ�
				mSQLiteDatabase.delete(TABLE_NAME, TABLE_ID + "=" + record.mId,
						null);
				// ȡ����ʱ�¼�
				if (record.mActive) {
					cancelSwitchRule(record.mId);
				}
			}

			// ɾ������Ԫ��
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
		/* �����������˳�Ӧ�ó���ʱ�������� */
		save();
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			/* �����������˳�Ӧ�ó���ʱ�������� */
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
		Dialog dialog = new AlertDialog.Builder(this).setTitle("��ʾ")
				.setMessage("ȷ��Ҫɾ������Ŀ��")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int id = (v.getId() - VIEW_ID_BASE) / VIEW_ID_CYCLE;
						Record record = getRecordbyId(id);

						// ɾ������Ԫ�� ����������ķָ��ߣ�
						int index = mTable.indexOfChild((View) v.getParent());
						mTable.removeViews(index, 2);

						// �������ݿ�
						mSQLiteDatabase.delete(TABLE_NAME, TABLE_ID + "="
								+ record.mId, null);

						// ȡ����ʱ�¼�
						if (record.mActive) {
							cancelSwitchRule(record.mId);
						}

						// ɾ������Ԫ��
						mRecords.remove(record);
					}
				}).setNegativeButton("�˳�", null).create();
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

	// �����л�����������Ӧ��alarm
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
		/* ��ȡ���ӹ����ʵ�� */
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		/* �������� ������ */
		am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				(24 * 60 * 60 * 1000), pendingIntent);
	}

	private void cancelSwitchRule(int i) {
		Intent intent = new Intent(AutoSwitchActivity.this, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				AutoSwitchActivity.this, i, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am;
		/* ��ȡ���ӹ����ʵ�� */
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(pendingIntent);

	}

	// װ�ء���ȡ����
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

	// ��������
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
			// TODO �������ݿ�󣬶����ݿ�Ĳ���
			/* �����ݿ�mSQLiteDatabase�д���һ���� */
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
				/* �������� */
				db.insert(TABLE_NAME, null, cv);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO �������ݿ�汾�Ĳ���
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			// TODO ÿ�γɹ������ݿ�����ȱ�ִ��
			/* ��ȡ���е����� */
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
