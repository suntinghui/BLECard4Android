package com.ble.client;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.ble.activity.BaseActivity;

public class ApplicationEnvironment {
	
	public static final String LKOA4ANDROID 		= "LKOA4ANDROID";
	
	private static ApplicationEnvironment appEnv 	= null;
	private Application application 				= null;
	private SharedPreferences preferences 			= null;
	
	public static ApplicationEnvironment getInstance(){
		if (null == appEnv){
			appEnv = new ApplicationEnvironment();
			
		}
		
		return appEnv;
	}
	
	public Application getApplication(){
		if (null == this.application){
			this.application = BaseActivity.getTopActivity().getApplication();
			BLEClient.getInstance();
		}
		
		return this.application;
	}
	
	public DisplayMetrics getPixels(){
		DisplayMetrics dm = new DisplayMetrics();  
		((WindowManager)getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		return dm;
	}
	
	public SharedPreferences getPreferences(){
		if (null == preferences)
			preferences = this.getApplication().getSharedPreferences(ApplicationEnvironment.LKOA4ANDROID, Context.MODE_PRIVATE);
		
		return preferences;
	}
	
	public boolean checkNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) this.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null)
			return false;
		
		NetworkInfo netinfo = manager.getActiveNetworkInfo();
		if (netinfo == null) {
			return false;
		}
		if (netinfo.isConnected()) {
			return true;
		}
		return false;
	}
	
	public void restartApp(){
		Intent intent = getApplication().getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(getApplication().getBaseContext().getPackageName());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		getApplication().startActivity(intent);
	}
	
	public void exitApp(){
		this.application.unregisterReceiver(BLEClient.getInstance().mGattUpdateReceiver);
		
		this.application.unbindService(BLEClient.getInstance().mServiceConnection);
		BLEClient.getInstance().mBLEService = null;
	}

}
