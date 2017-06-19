package org.mcal.pesdk;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.Gson;

import org.json.JSONException;
import org.mcal.pesdk.nativeapi.LibraryLoader;
import org.mcal.pesdk.nmod.LoadFailedException;
import org.mcal.pesdk.nmod.NMod;
import org.mcal.pesdk.nmod.NModJSONEditor;
import org.mcal.pesdk.nmod.NModLib;
import org.mcal.pesdk.nmod.NModTextEditor;
import org.mcal.pesdk.utils.MinecraftInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Preloader
{
	private Bundle mBundle;
	private PESdk mPESdk;
	private PreloadListener mPreloadListener;
	private NModPreloadData mPreloadData = new NModPreloadData();
	private ArrayList<String> mAssetsArrayList = new ArrayList<>();
	private ArrayList<String> mLoadedNativeLibs = new ArrayList<>();
	private ArrayList<NMod> mLoadedEnabledNMods = new ArrayList<>();

	public Preloader(PESdk pesdk, Bundle bundle, PreloadListener listener)
	{
		mBundle = bundle;
		mPreloadListener = listener;
		mPESdk = pesdk;
		if (mPreloadListener == null)
			mPreloadListener = new PreloadListener();
	}

	public Preloader(PESdk pesdk, Bundle bundle)
	{
		this(pesdk, bundle, null);
	}

	public void preload(Context context) throws PreloadException
	{
		mPreloadListener.onStart();

		if (mBundle == null)
			mBundle = new Bundle();
		Gson gson = new Gson();
		boolean safeMode = mPESdk.getLauncherOptions().isSafeMode();
		
		try
		{
			mPreloadListener.onLoadNativeLibs();
			mPreloadListener.onLoadSubstrateLib();
			LibraryLoader.loadSubstrate();
			mPreloadListener.onLoadFModLib();
			LibraryLoader.loadFMod(mPESdk.getMinecraftInfo().getMinecraftPackageNativeLibraryDir());
			mPreloadListener.onLoadMinecraftPELib();
			LibraryLoader.loadMinecraftPE(mPESdk.getMinecraftInfo().getMinecraftPackageNativeLibraryDir());
			mPreloadListener.onLoadGameLauncherLib();
			LibraryLoader.loadLauncher(mPESdk.getMinecraftInfo().getMinecraftPackageNativeLibraryDir());
			if (!safeMode)
			{
				mPreloadListener.onLoadNModAPILib();
				LibraryLoader.loadNModAPI(mPESdk.getMinecraftInfo().getMinecraftPackageNativeLibraryDir());
			}
			mPreloadListener.onFinishedLoadingNativeLibs();
		}
		catch (Throwable throwable)
		{
			throw new PreloadException(PreloadException.TYPE_LOAD_LIBS_FAILED, throwable);
		}

		if (!safeMode)
		{
			mPreloadListener.onStartLoadingAllNMods();
			//init data
			mPreloadData = new NModPreloadData();
			mAssetsArrayList = new ArrayList<>();
			mLoadedNativeLibs = new ArrayList<>();
			mLoadedEnabledNMods = new ArrayList<>();
			mAssetsArrayList.add(mPESdk.getMinecraftInfo().getMinecraftPackageContext().getPackageResourcePath());
			//init index
			ArrayList<NMod> unIndexedNModArrayList = mPESdk.getNModAPI().getImportedEnabledNMods();
			for(int index = unIndexedNModArrayList.size()-1;index>=0;--index)
			{
				mLoadedEnabledNMods.add(unIndexedNModArrayList.get(index));
			}
			//start init nmods
			for (NMod nmod:mLoadedEnabledNMods)
			{
				if (nmod.isBugPack())
				{
					mPreloadListener.onFailedLoadingNMod(nmod);
					continue;
				}

				NMod.NModPreloadBean preloadDataItem = nmod.copyNModFiles();

				if (loadNMod(context,nmod, preloadDataItem))
					mPreloadListener.onNModLoaded(nmod);
				else
					mPreloadListener.onFailedLoadingNMod(nmod);
			}
			mPreloadData.assets_packs_path = mAssetsArrayList.toArray(new String[0]);
			mPreloadData.loaded_libs = mLoadedNativeLibs.toArray(new String[0]);
			mBundle.putString(PreloadingInfo.NMOD_DATA_TAG, gson.toJson(mPreloadData));
			mPreloadListener.onFinishedLoadingAllNMods();
		}
		else
			mBundle.putString(PreloadingInfo.NMOD_DATA_TAG, gson.toJson(new Preloader.NModPreloadData()));
		mPreloadListener.onFinish(mBundle);
	}

	private boolean loadNMod(Context context,NMod nmod, NMod.NModPreloadBean preloadDataItem)
	{
		MinecraftInfo minecraftInfo = mPESdk.getMinecraftInfo();

		if (preloadDataItem.assets_path != null)
			mAssetsArrayList.add(preloadDataItem.assets_path);

		//edit json files
		if(nmod.getInfo().json_edit!=null&&nmod.getInfo().json_edit.length>0)
		{
			ArrayList<File> assetFiles = new ArrayList<>();
			for(String filePath : mAssetsArrayList)
				assetFiles.add(new File(filePath));
			NModJSONEditor jsonEditor = new NModJSONEditor(context,nmod,assetFiles.toArray(new File[0]));
			try
			{
				File outResourceFile = jsonEditor.edit();
				mAssetsArrayList.add(outResourceFile.getAbsolutePath());
			}
			catch(IOException e)
			{
				if(e instanceof FileNotFoundException)
					nmod.setBugPack(new LoadFailedException(LoadFailedException.TYPE_FILE_NOT_FOUND,e));
				else
					nmod.setBugPack(new LoadFailedException(LoadFailedException.TYPE_IO_FAILED,e));
				return false;
			}
			catch(JSONException jsonE)
			{
				nmod.setBugPack(new LoadFailedException(LoadFailedException.TYPE_JSON_SYNTAX,jsonE));
				return false;
			}
		}
		//edit text files
		if(nmod.getInfo().text_edit!=null&&nmod.getInfo().text_edit.length>0)
		{
			ArrayList<File> assetFiles = new ArrayList<>();
			for(String filePath : mAssetsArrayList)
				assetFiles.add(new File(filePath));
			NModTextEditor textEditor = new NModTextEditor(context,nmod,assetFiles.toArray(new File[0]));
			try
			{
				File outResourceFile = textEditor.edit();
				mAssetsArrayList.add(outResourceFile.getAbsolutePath());
			}
			catch(IOException e)
			{
				if (e instanceof FileNotFoundException)
					nmod.setBugPack(new LoadFailedException(LoadFailedException.TYPE_FILE_NOT_FOUND, e));
				else
					nmod.setBugPack(new LoadFailedException(LoadFailedException.TYPE_IO_FAILED, e));
				return false;
			}
		}
		//load elf files
		if (preloadDataItem.native_libs != null && preloadDataItem.native_libs.length > 0)
		{
			for (NMod.NModLibInfo nameItem:preloadDataItem.native_libs)
			{
				try
				{
					System.load(nameItem.name);
				}
				catch (Throwable t)
				{
					nmod.setBugPack(new LoadFailedException(LoadFailedException.TYPE_LOAD_LIB_FAILED, t));
					return false;
				}
			}

			for (NMod.NModLibInfo nameItem:preloadDataItem.native_libs)
			{
				if(nameItem.use_api)
				{
					NModLib lib = new NModLib(nameItem.name);
					lib.callOnLoad(minecraftInfo.getMinecraftVersionName(), mPESdk.getNModAPI().getVersionName());
					mLoadedNativeLibs.add(nameItem.name);
				}
			}
		}
		return true;
	}

	static class NModPreloadData
	{
		String[] assets_packs_path;
		String[] loaded_libs;
	}

	public static class PreloadListener
	{
		public void onStart()
		{}
		public void onLoadNativeLibs()
		{}
		public void onLoadSubstrateLib()
		{}
		public void onLoadGameLauncherLib()
		{}
		public void onLoadFModLib()
		{}
		public void onLoadMinecraftPELib()
		{}
		public void onLoadNModAPILib()
		{}
		public void onFinishedLoadingNativeLibs()
		{}

		public void onStartLoadingAllNMods()
		{}
		public void onNModLoaded(NMod nmod)
		{}
		public void onFailedLoadingNMod(NMod nmod)
		{}
		public void onFinishedLoadingAllNMods()
		{}

		public void onFinish(Bundle bundle)
		{}
	}
}
