package com.mcal.ModdedPE;

import android.app.*;
import android.content.res.*;
import com.mcal.ModdedPE.app.*;
import com.mcal.pesdk.nmod.*;
import com.mcal.pesdk.utils.*;
import java.io.*;
import com.mcal.ModdedPE.utils.*;
import com.mcal.pesdk.*;

public class ModdedPEApplication extends Application
{
	public static PESdk mPESdk;
	
	public void onCreate()
	{
		super.onCreate();
		Thread.setDefaultUncaughtExceptionHandler(restartHandler);
		mPESdk = new PESdk(this,new UtilsSettings(this));
	}

	private Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler()
	{
		public void uncaughtException(Thread thread, Throwable ex)
		{
			restartAppAndReport(ex);
		}
	};

	public void restartAppAndReport(Throwable ex)
	{
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ex.printStackTrace(new PrintStream(ous));
		ModdedPEErrorActivity.startThisActivity(this, new String(ous.toByteArray()));
	}

	@Override
	public AssetManager getAssets()
	{
		return mPESdk.getMinecraftInfo().getAssets();
	}
}
