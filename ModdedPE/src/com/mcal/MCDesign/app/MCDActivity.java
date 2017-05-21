package com.mcal.MCDesign.app;
import android.support.v7.app.*;
import android.os.*;
import com.mcal.ModdedPE.*;
import android.view.*;
import android.support.v7.widget.*;
import android.graphics.*;
import android.widget.*;
import java.io.*;
import android.graphics.drawable.*;
import com.mcal.MCDesign.widget.*;
import com.mcal.MCDesign.utils.*;

public class MCDActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

		setDefaultActionBar();

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mcd_bg);
		bitmap = BitmapRepeater.repeat(getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight(), bitmap);
		getWindow().getDecorView().setBackgroundDrawable(new BitmapDrawable(bitmap));
	}

	protected void setDefaultActionBar()
	{
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			RelativeLayout actionBarCustomView = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.mcd_actionbar, null);
			ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
			layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_HORIZONTAL;
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setCustomView(actionBarCustomView, layoutParams);
			android.support.v7.widget.Toolbar parent = (android.support.v7.widget.Toolbar) actionBarCustomView.getParent();
			parent.setContentInsetsAbsolute(0, 0);

			AppCompatTextView titleTV=(AppCompatTextView)actionBarCustomView.findViewById(R.id.mcd_actionbar_title);
			titleTV.setText(getTitle());
		}
	}

	@Override
	public void setTitle(int titleId)
	{
		super.setTitle(titleId);

		View actionBarCustomView=getSupportActionBar().getCustomView();
		AppCompatTextView titleTV=(AppCompatTextView)actionBarCustomView.findViewById(R.id.mcd_actionbar_title);
		titleTV.setText(titleId);
	}

	@Override
	public void setTitle(CharSequence title)
	{
		super.setTitle(title);

		View actionBarCustomView=getSupportActionBar().getCustomView();
		AppCompatTextView titleTV=(AppCompatTextView)actionBarCustomView.findViewById(R.id.mcd_actionbar_title);
		titleTV.setText(title);
	}

	protected void setActionBarViewRight(View view)
	{
		View actionBarCustomView=getSupportActionBar().getCustomView();
		RelativeLayout layout=(RelativeLayout)actionBarCustomView.findViewById(R.id.mcd_actionbar_ViewRight);
		layout.removeAllViews();
		layout.addView(view);
	}

	protected void setActionBarViewLeft(View view)
	{
		View actionBarCustomView=getSupportActionBar().getCustomView();
		RelativeLayout layout=(RelativeLayout)actionBarCustomView.findViewById(R.id.mcd_actionbar_ViewLeft);
		layout.removeAllViews();
		layout.addView(view);
	}

	protected void setActionBarButtonCloseRight()
	{
		MCDBurgerButtonClose buttonClose=new MCDBurgerButtonClose(this);
		buttonClose.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View p1)
				{
					finish();
				}


			});
		setActionBarViewRight(buttonClose);
	}
}
