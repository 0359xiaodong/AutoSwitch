/**
 * 文件名：About.java
 *
 * 版本信息：
 * 日期：2012-12-24
 * Copyright powervv.com 2012
 * 版权所有
 *
 */

package com.powervv.autoswitch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutActivity extends Activity implements OnClickListener, OnTouchListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_switch);
		
		ImageButton button = (ImageButton) findViewById(R.id.button_back);
		TextView weiboView = (TextView) findViewById(R.id.weibo);
		TextView emailView = (TextView) findViewById(R.id.email);
		button.setOnClickListener(this);
		button.setOnTouchListener(this);
		weiboView.setOnClickListener(this);
		weiboView.setOnTouchListener(this);
		emailView.setOnClickListener(this);
		emailView.setOnTouchListener(this);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToMainView();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	void backToMainView() {
		Intent intent = new Intent();
		intent.setClass(AboutActivity.this, AutoSwitchActivity.class);
		/* 启动一个新的Activity */
		startActivity(intent);
		/* 关闭当前的Activity */
		AboutActivity.this.finish();
	}
	
	@Override
	public void onClick(final View v) {
		switch(v.getId()) {
		case R.id.button_back:
			backToMainView();
			break;
		case R.id.weibo:
			Uri uri = Uri.parse("http://weibo.com/u/1930725333");  
			Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);  
			startActivity(webIntent);
			break;
		case R.id.email:
			String[] reciver = new String[] { "powervv@gmail.com" };  
	        //String[] mySbuject = new String[] { "test" };   
	        String mybody = "至powervv：";  
	        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
	        emailIntent.setType("plain/text");  
	        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, reciver);    
	        //myIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mySbuject);  
	        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, mybody);  
	        startActivity(Intent.createChooser(emailIntent, "联系我们"));  
	        break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (v.getId() == R.id.button_back) {
				v.setBackgroundColor(Color.CYAN);
			}
			else {
				View parentView = (View) v.getParent();
				parentView.setBackgroundColor(Color.CYAN);	
			}


		} else if (event.getAction() == MotionEvent.ACTION_UP
				|| event.getAction() == MotionEvent.ACTION_CANCEL) {
			if (v.getId() == R.id.button_back) {
				v.setBackgroundColor(Color.BLACK);
			}
			else {
				View parentView = (View) v.getParent();
				parentView.setBackgroundColor(Color.WHITE);
			}
		}
		return false;

	}
}
