package com.mcal.ModdedPE.app;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.mcal.MCDesign.app.*;
import com.mcal.ModdedPE.*;
import com.mcal.ModdedPE.utils.*;
import com.mcal.ModdedPE.widget.*;

public class ModdedPEStartActivity extends MCDActivity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.moddedpe_start);

		new Thread()
		{
			public void run()
			{
				ModdedPEApplication.instance.init();
				mHandler.sendEmptyMessage(0);
			}
		}.start();
		
	}
	
	Handler mHandler=new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			
			Intent intent=new Intent(ModdedPEStartActivity.this,ModdedPEMainActivity.class);
			startActivity(intent);
			
			finish();
		}
	};
}