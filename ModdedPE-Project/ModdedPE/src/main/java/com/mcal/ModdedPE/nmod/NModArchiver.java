package com.mcal.ModdedPE.nmod;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import com.google.gson.*;
import com.mcal.ModdedPE.utils.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class NModArchiver
{
	private Context mContext;

	NModArchiver(Context context)
	{
		this.mContext = context;
	}

	PackagedNMod archiveFromInstalledPackage(String packageName)throws ArchiveFailedException
	{
		try
		{
			Context contextPackage = mContext.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			contextPackage.getAssets().open(NMod.MANIFEST_NAME).close();
			return new PackagedNMod(mContext, contextPackage);
		}
		catch (IOException e)
		{
			throw new ArchiveFailedException(ArchiveFailedException.TYPE_NO_MANIFEST);
		}
		catch (PackageManager.NameNotFoundException notFoundE)
		{
			throw new ArchiveFailedException(ArchiveFailedException.TYPE_PACKAGE_NOT_FOUND, notFoundE);
		}
	}

	ZippedNMod archiveFromStorage(String path)throws ArchiveFailedException
	{
		PackageManager packageManager = mContext.getPackageManager();
		PackageInfo packageInfo = packageManager.getPackageArchiveInfo(path, PackageManager.GET_CONFIGURATIONS);
		NMod.NModInfo nmodInfo = null;
		try
		{
			nmodInfo = archiveInfoFromZipped(new File(path));
		}
		catch (IOException e)
		{
			throw new ArchiveFailedException(ArchiveFailedException.TYPE_NO_MANIFEST, e);
		}
		catch (JsonSyntaxException e)
		{
			throw new ArchiveFailedException(ArchiveFailedException.TYPE_JSON_SYNTAX_EXCEPTION, e);
		}

		if (packageInfo != null)
		{
			if(nmodInfo.package_name != null && !nmodInfo.package_name.equals(packageInfo.packageName))
				throw new ArchiveFailedException(ArchiveFailedException.TYPE_INEQUAL_PACKAGE_NAME);
			
			nmodInfo.package_name = packageInfo.packageName;

			try
			{
				File nmodDir = new File(new NModFilePathManager(mContext).getNModCacheDir());
				nmodDir.mkdirs();
				File toFile = new File(new NModFilePathManager(mContext).getNModCachePath());
				toFile.createNewFile();
				ZipFile zipFile = new ZipFile(path);
				String packageName = packageInfo.packageName;
				String versionName = packageInfo.versionName;
				int versionCode = packageInfo.versionCode;
				packageInfo.applicationInfo.sourceDir = path;
				packageInfo.applicationInfo.publicSourceDir = path;
				Drawable icon = packageManager.getApplicationIcon(packageInfo.applicationInfo);
				ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(toFile));
				Enumeration<ZipEntry> zipfile_ents = (Enumeration<ZipEntry>) zipFile.entries();

				while (zipfile_ents.hasMoreElements())
				{
					ZipEntry entry=zipfile_ents.nextElement();

					if (!entry.isDirectory() && entry.getName() != ("assets" + File.separator + NMod.MANIFEST_NAME))
					{
						zipOutputStream.putNextEntry(entry);
						InputStream from = zipFile.getInputStream(entry);
						int byteReaded = -1;
						byte[] buffer = new byte[1024];
						while ((byteReaded = from.read(buffer)) != -1)
						{
							zipOutputStream.write(buffer, 0, byteReaded);
						}
						from.close();
						zipOutputStream.closeEntry();
					}
				}

				//Manifest
				nmodInfo.package_name = packageName;
				nmodInfo.version_code = versionCode;
				nmodInfo.version_name = versionName;
				zipOutputStream.putNextEntry(new ZipEntry(NMod.MANIFEST_NAME));
				zipOutputStream.write(new Gson().toJson(nmodInfo).getBytes());
				zipOutputStream.closeEntry();

				//Icon
				Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), icon.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565);
				Canvas canvas = new Canvas(bitmap);
				icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
				icon.draw(canvas);
				zipOutputStream.putNextEntry(new ZipEntry("icon.png"));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();  
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);  
				zipOutputStream.write(baos.toByteArray());
				zipOutputStream.closeEntry();

				zipOutputStream.close();
				
				return new ZippedNMod(mContext, copyCachedNModToData(toFile,packageName));
			}
			catch (IOException ioe)
			{
				throw new ArchiveFailedException(ArchiveFailedException.TYPE_IO_EXCEPTION, ioe);
			}
		}
		else
		{
			if(nmodInfo.package_name == null)
				throw new ArchiveFailedException(ArchiveFailedException.TYPE_UNDEFINED_PACKAGE_NAME,new IllegalArgumentException("Undefined package name in manifest."));
			if(!NModUtils.isValidPackageName(nmodInfo.package_name))
				throw new ArchiveFailedException(ArchiveFailedException.TYPE_INVAILD_PACKAGE_NAME,new IllegalArgumentException("The provided package name is not a valid java-styled package name."));
			
			try
			{
				ZipFile zipFile = new ZipFile(path);
				File dir = new File(new NModFilePathManager(mContext).getNModCacheDir());
				dir.mkdirs();
				File nmodFile = new File(new NModFilePathManager(mContext).getNModCachePath());
				nmodFile.createNewFile();
				ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(nmodFile));
				Enumeration<ZipEntry> zipfile_ents = (Enumeration<ZipEntry>) zipFile.entries();

				while (zipfile_ents.hasMoreElements())
				{
					ZipEntry entry=zipfile_ents.nextElement();
					if (!entry.isDirectory())
					{
						zipOutputStream.putNextEntry(entry);
						InputStream from = zipFile.getInputStream(entry);
						int byteReaded = -1;
						byte[] buffer = new byte[1024];
						while ((byteReaded = from.read(buffer)) != -1)
						{
							zipOutputStream.write(buffer, 0, byteReaded);
						}
						from.close();
						zipOutputStream.closeEntry();
					}
				}
				ZipEntry entry=new ZipEntry("AndroidManifest.xml");
				zipOutputStream.putNextEntry(entry);
				zipOutputStream.closeEntry();
				zipOutputStream.close();
				ZippedNMod zippedNMod_cached = new ZippedNMod(mContext, nmodFile);
				String pkgName = zippedNMod_cached.getPackageName();
				File finalFileDir = new File(new NModFilePathManager(mContext).getNModsDir());
				finalFileDir.mkdirs();
				File finalFile = new File(new NModFilePathManager(mContext).getNModsDir() + File.separator + pkgName);
				finalFile.createNewFile();
				FileOutputStream finalFileOutput = new FileOutputStream(finalFile);
				FileInputStream fileInput = new FileInputStream(nmodFile);
				int byteReaded = -1;
				byte[] buffer = new byte[1024];
				while ((byteReaded = fileInput.read(buffer)) != -1)
				{
					finalFileOutput.write(buffer, 0, byteReaded);
				}
				finalFileOutput.close();
				fileInput.close();
				return new ZippedNMod(mContext, copyCachedNModToData(finalFile,nmodInfo.package_name));
			}
			catch (IOException ioe)
			{
				throw new ArchiveFailedException(ArchiveFailedException.TYPE_IO_EXCEPTION, ioe);
			}
		}
	}
	
	private File copyCachedNModToData(File cachedNModFile,String packageName)throws ArchiveFailedException
	{
		try
		{
			File finalFileDir = new File(new NModFilePathManager(mContext).getNModsDir());
			finalFileDir.mkdirs();
			File finalFile = new File(new NModFilePathManager(mContext).getNModsDir() + File.separator + packageName);
			finalFile.createNewFile();
			FileOutputStream finalFileOutput = new FileOutputStream(finalFile);
			FileInputStream fileInput = new FileInputStream(cachedNModFile);
			int byteReaded = -1;
			byte[] buffer = new byte[1024];
			while ((byteReaded = fileInput.read(buffer)) != -1)
			{
				finalFileOutput.write(buffer, 0, byteReaded);
			}
			finalFileOutput.close();
			fileInput.close();
			cachedNModFile.delete();
			return finalFile;
		}
		catch(IOException ioe)
		{
			throw new ArchiveFailedException(ArchiveFailedException.TYPE_IO_EXCEPTION);
		}
	}

	ArrayList<NMod> archiveAllFromInstalled()
	{
		PackageManager packageManager = mContext.getPackageManager();
		List<PackageInfo> infos = packageManager.getInstalledPackages(0);
		ArrayList<NMod> list = new ArrayList<NMod>();
		for (PackageInfo info:infos)
		{
			try
			{
				PackagedNMod packagedNMod = archiveFromInstalledPackage(info.packageName);
				list.add(packagedNMod);
				continue;
			}
			catch (ArchiveFailedException e)
			{}
		}
		return list;
	}

	NMod.NModInfo archiveInfoFromZipped(File filePath)throws IOException,JsonSyntaxException
	{
		ZipFile zipFile = new ZipFile(filePath);
		InputStream input = zipFile.getInputStream(zipFile.getEntry(NMod.MANIFEST_NAME));
		byte[] buffer = new byte[input.available()];
		input.read(buffer);
		String jsonString = new String(buffer);
		return new Gson().fromJson(jsonString, NMod.NModInfo.class);
	}
}