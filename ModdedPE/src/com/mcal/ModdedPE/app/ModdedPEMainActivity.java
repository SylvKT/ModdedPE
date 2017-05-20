package com.mcal.ModdedPE.app;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.support.v4.view.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import com.mcal.MCDesign.app.*;
import com.mcal.MCDesign.widget.*;
import com.mcal.ModdedPE.*;
import com.mcal.ModdedPE.nmod.*;
import com.mcal.ModdedPE.utils.*;
import com.mcal.ModdedPE.widget.*;
import java.util.*;
import android.support.v4.app.*;

public class ModdedPEMainActivity extends MCDActivity 
{
	private ViewPager mainViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.moddedpe_main_pager);
		
		List<Fragment> fragment_list=new Vector<Fragment>();
		List<CharSequence> titles_list=new Vector<CharSequence>();

		MainStartFragment startFragment = new MainStartFragment();
		fragment_list.add(startFragment);
		titles_list.add(getString(R.string.main_title));
		
		MainManageNModFragment manageNModFragment = new MainManageNModFragment();
		fragment_list.add(manageNModFragment);
		titles_list.add(getString(R.string.manage_nmod_title));
		
		MainSettingsFragment settingsFragment = new MainSettingsFragment();
		fragment_list.add(settingsFragment);
		titles_list.add(getString(R.string.settings_title));

		MainFragmentPagerAdapter pagerAdapter = new MainFragmentPagerAdapter(fragment_list, titles_list);

		mainViewPager = (ViewPager) findViewById(R.id.moddedpe_main_view_pager);
		mainViewPager.setAdapter(pagerAdapter);
		mainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
			{
				@Override
				public void onPageScrolled(int p1, float p2, int p3)
				{
					setTitle(mainViewPager.getAdapter().getPageTitle(p1));
				}

				@Override
				public void onPageSelected(int p1)
				{

				}

				@Override
				public void onPageScrollStateChanged(int p1)
				{

				}
			});
	}

	private void setViewOnClickScrollTo(View view, final int to)
	{
		view.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View p1)
				{
					mainViewPager.setCurrentItem(to);
				}

			});
	}

	@Override
	protected void setDefaultActionBar()
	{
		super.setDefaultActionBar();

		MCDBurgerButton burgerButton=new MCDBurgerButton(this);
		burgerButton.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View p1)
				{
					//on menu clicked
				}


			});
		setActionBarViewRight(burgerButton);

		AppCompatImageButton imageButton=new AppCompatImageButton(this);
		imageButton.setBackgroundResource(R.drawable.mcd_creeperface);
		imageButton.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View p1)
				{
					startActivity(new Intent(ModdedPEMainActivity.this, ModdedPEAboutActivity.class));
				}

			});
		setActionBarViewLeft(imageButton);
	}

	private Context getMcContext()
	{
		try
		{
			return createPackageContext(ModdedPEApplication.MC_PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private boolean isSupportedMinecraftPEVersion()
	{
		if (getMcContext() == null)
			return false;
		try
		{
			String mcpeVersionName=getMcContext().getPackageManager().getPackageInfo(getMcContext().getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;
			for (String nameItem : getResources().getStringArray(R.array.target_mcpe_versions))
			{
				if (nameItem.equals(mcpeVersionName))
					return true;
			}
		}
		catch (PackageManager.NameNotFoundException e)
		{}
		return false;
	}

	private String getMinecraftPEVersionName()
	{
		if (getMcContext() == null)
			return null;
		try
		{
			return getMcContext().getPackageManager().getPackageInfo(getMcContext().getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;
		}
		catch (PackageManager.NameNotFoundException e)
		{}
		return null;
	}

	/*
	 public void onNewsClicked(View view)
	 {
	 if (main_showingNMod != null)
	 ModdedPENModDescriptionActivity.startThisActivity(this, main_showingNMod);
	 }
	 */

	public void onPlayClicked(View v)
	{
		if (getMcContext() == null)
		{
			android.support.v7.app.AlertDialog.Builder mdialog = new AlertDialog.Builder(this);
			mdialog.setTitle(getString(R.string.no_mcpe_found_title));
			mdialog.setMessage(getString(R.string.no_mcpe_found));
			mdialog.setPositiveButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface p1, int id)
					{
						p1.dismiss();
					}
				});
			mdialog.show();
		}
		else if (!isSupportedMinecraftPEVersion())
		{
			android.support.v7.app.AlertDialog.Builder mdialog = new AlertDialog.Builder(this);
			mdialog.setTitle(getString(R.string.no_available_mcpe_version_found_title));
			mdialog.setMessage(getString(R.string.no_available_mcpe_version_found, new String[]{getMinecraftPEVersionName(),getString(R.string.target_mcpe_version_info)}));
			mdialog.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface p1, int id)
					{
						p1.dismiss();
					}
				});
			mdialog.setPositiveButton(getString(R.string.no_available_mcpe_version_continue), new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface p1, int id)
					{
						startMinecraft();
					}


				});
			mdialog.show();
			return;
		}
		else
			startMinecraft();
	}

	private void startMinecraft()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				Intent i=null;
				if (new UtilsSettings(ModdedPEMainActivity.this).getSafeMode())
					i = new Intent(ModdedPEMainActivity.this, ModdedPESafetyModeMinecraftActivity.class);
				else
					i = new Intent(ModdedPEMainActivity.this, ModdedPEMinecraftActivity.class);
				startActivity(i);
				finish();
			}
		}.start();
	}

	private class MainFragmentPagerAdapter extends FragmentPagerAdapter
	{
		private List<Fragment> fragments;
		private List<CharSequence> titles;
		public MainFragmentPagerAdapter(List<Fragment>fragments, List<CharSequence>titles)
		{
			super(getSupportFragmentManager());
			this.fragments = fragments;
			this.titles = titles;
		}

		@Override
		public int getCount()
		{
			return fragments.size();
		}

		@Override
		public Fragment getItem(int p1)
		{
			return fragments.get(p1);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return titles.get(position);
		}
	}
}
