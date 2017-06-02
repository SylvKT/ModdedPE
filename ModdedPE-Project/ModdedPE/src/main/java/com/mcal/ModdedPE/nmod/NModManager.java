package com.mcal.ModdedPE.nmod;
import android.content.*;
import com.mcal.ModdedPE.utils.*;
import java.io.*;
import java.util.*;

class NModManager
{
	private ArrayList<NMod> mEnabledNMods=new ArrayList<NMod>();
	private ArrayList<NMod> mAllNMods=new ArrayList<NMod>();
	private ArrayList<NMod> mDisabledNMods=new ArrayList<NMod>();
	private Context mContext;

	NModManager(Context context)
	{
		this.mContext = context;
	}

	ArrayList<NMod> getEnabledNMods()
	{
		return mEnabledNMods;
	}

	ArrayList<NMod> getEnabledNModsIsValidBanner()
	{
		ArrayList<NMod> ret=new ArrayList<NMod>();
		for (NMod nmod:getEnabledNMods())
		{
			if (nmod.isValidBanner())
				ret.add(nmod);
		}
		return ret;
	}

	ArrayList<NMod> getAllNMods()
	{
		return mAllNMods;
	}

	void init()
	{
		mAllNMods = new ArrayList<NMod>();
		mEnabledNMods = new ArrayList<NMod>();
		mDisabledNMods = new ArrayList<NMod>();

		NModsDataLoader dataloader = new NModsDataLoader(mContext);

		for (String item:dataloader.getAllList())
		{
			if (!NModUtils.isValidPackageName(item))
			{
				dataloader.removeByName(item);
			}
		}

		forEachItemToAddNMod(dataloader.getEnabledList(), true);
		forEachItemToAddNMod(dataloader.getDisabledList(), false);
		refreshDatas();
	}

	void removeImportedNMod(NMod nmod)
	{
		mEnabledNMods.remove(nmod);
		mDisabledNMods.remove(nmod);
		mAllNMods.remove(nmod);
		NModsDataLoader dataloader=new NModsDataLoader(mContext);
		dataloader.removeByName(nmod.getPackageName());
		if (nmod.getNModType() == NMod.NMOD_TYPE_ZIPPED)
		{
			String zippedNModPath = new NModFilePathManager(mContext).getNModsDir() + File.separator + nmod.getPackageName();
			File file = new File(zippedNModPath);
			if (file.exists())
			{
				file.delete();
			}
		}
	}

	private void forEachItemToAddNMod(ArrayList<String> list, boolean enabled)
	{
		for (String packageName:list)
		{
			try
			{
				String zippedNModPath = new NModFilePathManager(mContext).getNModsDir() + File.separator + packageName;
				ZippedNMod zippedNMod = new ZippedNMod(mContext, new File(zippedNModPath));
				if (zippedNMod != null)
				{
					//zippedNMod.setPackageName(packageName);
					importNMod(zippedNMod, enabled);
					continue;
				}

			}
			catch (IOException e)
			{}
			
			try
			{
				NModArchiver archiver = new NModArchiver(mContext);
				PackagedNMod packagedNMod = archiver.archiveFromInstalledPackage(packageName);
				importNMod(packagedNMod, enabled);
				continue;
			}
			catch (ArchiveFailedException e)
			{}
		}
	}

	boolean importNMod(NMod newNMod, boolean enabled)
	{
		boolean replaced = false;
		for (NMod nmod : mAllNMods)
		{
			if (nmod.getPackageName().equals(newNMod.getPackageName()))
			{
				mAllNMods.remove(nmod);
				replaced = true;
			}
		}
		for (NMod nmod : mEnabledNMods)
		{
			if (nmod.getPackageName().equals(newNMod.getPackageName()))
			{
				mEnabledNMods.remove(nmod);
				replaced = true;
			}
		}
		for (NMod nmod : mDisabledNMods)
		{
			if (nmod.getPackageName().equals(newNMod.getPackageName()))
			{
				mDisabledNMods.remove(nmod);
				replaced = true;
			}
		}
		
		mAllNMods.add(newNMod);
		if (enabled)
			setEnabled(newNMod);
		else
			setDisable(newNMod);
		return replaced;
	}



	void refreshDatas()
	{
		NModsDataLoader dataloader=new NModsDataLoader(mContext);

		for (String item:dataloader.getAllList())
		{
			if (getImportedNMod(item) == null)
			{
				dataloader.removeByName(item);
			}
		}
	}

	NMod getImportedNMod(String pkgname)
	{
		for (NMod nmod : mAllNMods)
			if (nmod.getPackageName().equals(pkgname))
				return nmod;
		return null;
	}

	void makeUp(NMod nmod)
	{
		NModsDataLoader dataloader=new NModsDataLoader(mContext);
		dataloader.upNMod(nmod);
		refreshEnabledOrderList();
	}

	void makeDown(NMod nmod)
	{
		NModsDataLoader dataloader=new NModsDataLoader(mContext);
		dataloader.downNMod(nmod);
		refreshEnabledOrderList();
	}

	private void refreshEnabledOrderList()
	{
		NModsDataLoader dataloader=new NModsDataLoader(mContext);
		ArrayList<String> enabledList = dataloader.getEnabledList();
		mEnabledNMods.clear();
		for (String pkgName : enabledList)
		{
			NMod nmod = getImportedNMod(pkgName);
			if (nmod != null)
			{
				mEnabledNMods.add(nmod);
			}
		}
	}

	void setEnabled(NMod nmod)
	{
		if (nmod.isBugPack())
			return;
		NModsDataLoader dataloader=new NModsDataLoader(mContext);
		dataloader.setIsEnabled(nmod, true);
		mEnabledNMods.add(nmod);
		mDisabledNMods.remove(nmod);
	}

	void setDisable(NMod nmod)
	{
		NModsDataLoader dataloader=new NModsDataLoader(mContext);
		dataloader.setIsEnabled(nmod, false);
		mDisabledNMods.add(nmod);
		mEnabledNMods.remove(nmod);
	}

	ArrayList<NMod> getDisabledNMods()
	{
		return mDisabledNMods;
	}
}
