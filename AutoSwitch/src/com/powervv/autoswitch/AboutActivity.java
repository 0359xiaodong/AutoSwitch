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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_switch);
		
		Button button = (Button) findViewById(R.id.button_back);
		/* 监听button的事件信息 */
		button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent();
				intent.setClass(AboutActivity.this, AutoSwitchActivity.class);
				/* 启动一个新的Activity */
				startActivity(intent);
				/* 关闭当前的Activity */
				AboutActivity.this.finish();
			}		
		});
	}
}
