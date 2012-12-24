/**
 * �ļ�����About.java
 *
 * �汾��Ϣ��
 * ���ڣ�2012-12-24
 * Copyright powervv.com 2012
 * ��Ȩ����
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
		/* ����button���¼���Ϣ */
		button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent();
				intent.setClass(AboutActivity.this, AutoSwitchActivity.class);
				/* ����һ���µ�Activity */
				startActivity(intent);
				/* �رյ�ǰ��Activity */
				AboutActivity.this.finish();
			}		
		});
	}
}
