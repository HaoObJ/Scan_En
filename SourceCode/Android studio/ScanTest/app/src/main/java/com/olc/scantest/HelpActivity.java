package com.olc.scantest;
import java.util.Locale;

import com.olc.scantest.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
public class HelpActivity extends Activity implements OnClickListener {
	// Image
	private WebView mWebView;
	public int stype = 1;
	// Image
	private TextView t_versionName;
	private ImageButton m_mainback;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activityhelp);
		t_versionName = (TextView) findViewById(R.id.txt_version);
		m_mainback = (ImageButton) findViewById(R.id.btn_main_back);
		m_mainback.setOnClickListener(this);
		try {
			String viewe = getLocalVersionCode(this);
			t_versionName.setText(getResources().getText(R.string.App_version)
					+ viewe);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		mWebView = (WebView) findViewById(R.id.wv_help);
		if(isZhlanguag())
		{
		  mWebView.loadUrl("file:///android_asset/scanzhhelp.html");
		}
		else
		{
			 mWebView.loadUrl("file:///android_asset/scanEnglish.html");
		}
	}
	/**
	 * zh or en
	 * */
	private boolean isZhlanguag() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.endsWith("zh"))
			return true;
		else
			return false;
	}
	/**
	 * @param context
	 * @throws NameNotFoundException
	 */
	public static String getLocalVersionCode(Context context)
			throws NameNotFoundException {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo = packageManager.getPackageInfo(
				context.getPackageName(), 0);
		return packageInfo.versionName;
	}
	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == R.id.btn_main_back) {
			finish();
		}
	}

}
