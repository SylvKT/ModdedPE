package com.mcal.ModdedPE.nmodpe;
import android.content.*;
import android.content.pm.*;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import android.app.*;

public class NModPEManager
{
	private Vector<NModPE> activeNModPEs=new Vector<NModPE>();
	private Vector<NModPE> allNModPEs=new Vector<NModPE>();
	private Vector<NModPE> disabledNModPEs=new Vector<NModPE>();
	private Context contextThis;
	private static NModPEManager instance=null;

	public static void reCalculate(Context c)
	{
		instance=new NModPEManager(c);
	}
	
	public static NModPEManager getNModPEManager(Context c)
	{
		if(instance==null)
			instance=new NModPEManager(c);
		return instance;
	}
	
	private NModPEManager(Context contextThis)
	{
		this.contextThis=contextThis;
		searchAndAddNModPE();
	}
	
	public Vector<NModPE> getActiveNModPEs()
	{
		return activeNModPEs;
	}
	
	public Vector<NModPE> getAllNModPEs()
	{
		return allNModPEs;
	}
	
	public void searchAndAddNModPE()
	{
		allNModPEs=new Vector<NModPE>();
		activeNModPEs=new Vector<NModPE>();
		
		PackageManager packageManager = contextThis.getPackageManager();
		List<PackageInfo> infos=packageManager.getInstalledPackages(0);
		for(PackageInfo info:infos)
		{
			try
			{
				Context contextPackage=contextThis.createPackageContext(info.applicationInfo.packageName,Context.CONTEXT_IGNORE_SECURITY|Context.CONTEXT_INCLUDE_CODE);
				InputStream is=contextPackage.getAssets().open(NModPE.TAG_MANIFEST_NAME);
				if(is!=null)
					addNewNModPE(new NModPE(contextPackage,contextThis));
			}
			catch(Throwable e)
			{
				
			}
		}
		
		refreshDatas();
	}
	
	private void addNewNModPE(NModPE newNModPE)
	{
		for(NModPE nmodpe:allNModPEs)
			if(nmodpe.getPackageName().equals(newNModPE.getPackageName()))
				return;
		allNModPEs.add(newNModPE);
	}
	
	public void refreshDatas()
	{
		NModPEOptions options=new NModPEOptions(contextThis);
		Vector<String> activeList=options.getActiveList();
		for(String item:activeList)
		{
			if(getNModPE(item)==null||getNModPE(item).isBugPack())
				options.removeActive(getNModPE(item));
		}
		
		activeNModPEs=new Vector<NModPE>();
		for(String item:activeList)
		{
			NModPE nmodpe=getNModPE(item);
			if(nmodpe!=null)
				activeNModPEs.add(nmodpe);
		}
		
		disabledNModPEs=new Vector<NModPE>();
		for(NModPE nmodpe:allNModPEs)
		{
			if(activeNModPEs.indexOf(nmodpe)==-1)
				disabledNModPEs.add(nmodpe);
		}
	}
	
	private NModPE getNModPE(String name)
	{
		for(NModPE nmodpe:allNModPEs)
			if(nmodpe.getPackageName().equals(name))
				return nmodpe;
		return null;
	}
	
	public void removeActive(NModPE nmodpe)
	{
		NModPEOptions options=new NModPEOptions(contextThis);
		options.setIsActive(nmodpe,false);
		activeNModPEs.remove(nmodpe);
		refreshDatas();
	}
	
	public void makeUp(NModPE nmodpe)
	{
		NModPEOptions options=new NModPEOptions(contextThis);
		options.upNModPE(nmodpe);
		refreshDatas();
	}
	
	public void makeDown(NModPE nmodpe)
	{
		NModPEOptions options=new NModPEOptions(contextThis);
		options.downNModPE(nmodpe);
		refreshDatas();
	}
	
	public void addActive(NModPE nmodpe)
	{
		if(nmodpe.isBugPack())
			return;
		NModPEOptions options=new NModPEOptions(contextThis);
		options.setIsActive(nmodpe,true);
		activeNModPEs.add(nmodpe);
		refreshDatas();
	}
	
	public Vector<NModPE> getDisabledNModPEs()
	{
		return disabledNModPEs;
	}
}
