package com.ble.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;

import com.ble.R;
import com.ble.client.ApplicationEnvironment;

public class SplashActivity extends BaseActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //设置全屏
        
        setContentView(R.layout.activity_splash);
        
        new SplashTask().execute();
	}
	
	class SplashTask extends AsyncTask<Object, Object, Object>{

		@Override
		protected Object doInBackground(Object... arg0) {
			try{
				
				ApplicationEnvironment.getInstance().getApplication();
				
				Thread.sleep(1500);
				
				return null;
				
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			Intent intent = new Intent(SplashActivity.this, CatalogActivity.class);
			SplashActivity.this.startActivity(intent);
			SplashActivity.this.finish();
		}
		
	}

}
