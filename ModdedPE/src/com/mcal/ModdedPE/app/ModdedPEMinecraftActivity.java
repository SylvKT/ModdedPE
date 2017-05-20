package com.mcal.ModdedPE.app;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.os.*;
import com.mcal.ModdedPE.*;
import com.mcal.ModdedPE.nativeapi.*;
import com.mcal.ModdedPE.nmod.*;
import com.mcal.ModdedPE.utils.*;
import java.util.*;
import java.io.*;

public class ModdedPEMinecraftActivity extends com.mojang.minecraftpe.MainActivity
{
	private Context mcPackageContext=null;
	private String mcLibDir=ModdedPEApplication.MC_NATIVE_DIR;
	
	private void initFields()
	{
		mcPackageContext = getMcContext();
		mcLibDir = getNativeLibDirectory();
	}

	private Context getMcContext()
	{
		if (mcPackageContext != null)
			return mcPackageContext;
		try
		{
			return mcPackageContext = createPackageContext(ModdedPEApplication.MC_PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	protected void loadNativeLibraries()
	{
		checkNullMcContext();
		LibraryLoader.loadGameLibs(mcLibDir, false);
	}

	private String getNativeLibDirectory()
	{
		if (checkNullMcContext())
			return mcPackageContext.getApplicationInfo().nativeLibraryDir;
		else
			return ModdedPEApplication.MC_NATIVE_DIR;
	}

	private boolean checkNullMcContext()
	{
		if (mcPackageContext == null)
			mcPackageContext = getMcContext();
		return mcPackageContext != null;
	}

	@Override
	public void onCreate(Bundle p1)
	{
		initFields();
		loadNativeLibraries();
		mergeGameAssets();
		NativeUtils.setValues(this);
		new OpenGameLoadingDialog(this).show();
		prepareNMods(p1);
		super.onCreate(p1);
		GameLauncher.launch();
	}
	
	private void prepareNMods(Bundle saved_instance_state)
	{
		NModManager.reCalculate(this);
		loadNMods();
		mergeNModAssets();
		callOnNModActivityCreate(saved_instance_state);
	}
	
	private void mergeGameAssets()
	{
		AssetOverrideManager.getInstance().addAssetOverride(mcPackageContext.getPackageResourcePath());
	}
	
	private void callOnNModActivityCreate(Bundle savedInstanceState)
	{
		NModManager nmodManager=NModManager.getNModManager(this);
		for (int i=nmodManager.getActiveNMods().size() - 1;i >= 0;--i)
		{
			NMod nmod=nmodManager.getActiveNMods().get(i);
			if(!nmod.isBugPack())
				nmod.getLoader().callOnActivityCreate(this, savedInstanceState);
		}
	}
	
	private void mergeNModAssets()
	{
		NModManager nmodManager=NModManager.getNModManager(this);
		for (int i=nmodManager.getActiveNMods().size() - 1;i >= 0;--i)
		{
			NMod nmod=nmodManager.getActiveNMods().get(i);
			if(!nmod.isBugPack())
				AssetOverrideManager.getInstance().addAssetOverride(nmod.getPackageResourcePath());
		}
	}

	private void setNativeUtilsAttributes()
	{
		

	}

	private void loadNMods()
	{
		NModManager nmodManager=NModManager.getNModManager(this);
		String mcVer=new String();
		String moddedpeVer=getString(R.string.app_version);
		try
		{
			mcVer = getMcContext().getPackageManager().getPackageInfo(getMcContext().getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;
		}
		catch (PackageManager.NameNotFoundException e)
		{}
		for (int i=nmodManager.getActiveNMods().size() - 1;i >= 0;--i)
		{
			NMod nmod=nmodManager.getActiveNMods().get(i);

			try
			{
				nmod.load(mcVer, moddedpeVer);
			}
			catch (Throwable e)
			{
				nmod.setBugPack(NModLoadException.getLoadElfFail(e, getResources()));
				ModdedPENModLoadFailActivity.startThisActivity(this, nmod);
				continue;
			}
		}
	}

	@Override
	public AssetManager getAssets()
	{
		AssetManager mgr=AssetOverrideManager.getInstance().getLocalAssetManager();
		if (mgr != null)
			return mgr;
		return super.getAssets();
	}

	@Override
	protected void onDestroy()
	{
		NModManager nmodManager=NModManager.getNModManager(this);
		for (int i=nmodManager.getActiveNMods().size() - 1;i >= 0;--i)
		{
			NMod nmod=nmodManager.getActiveNMods().get(i);
			if(!nmod.isBugPack())
				nmod.getLoader().callOnActivityFinish(this);
		}
		super.onDestroy();
	}
}
