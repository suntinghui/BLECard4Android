package com.ble.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ble.R;

public class CatalogActivity extends BaseActivity {
	
	private static final int REQUEST_ENABLE_BT = 1;

	private Integer[] imageIds = { R.drawable.catalog_1_button, R.drawable.catalog_2_button, R.drawable.catalog_3_button };
	private String[] titles = { "余额查询", "充值", "明细查询", };

	private GridView gridView = null;
	private CatalogAdapter adapter = null;

	private long exitTimeMillis = 0;
	
	private BluetoothAdapter mBluetoothAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_catalog);

		gridView = (GridView) findViewById(R.id.gridveiw);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(onclickcistener);

		adapter = new CatalogAdapter(this);
		gridView.setAdapter(adapter);
		
		this.checkBLEEnvironment();
	}
	
	private void checkBLEEnvironment(){
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "该设备不支持蓝牙4.0，无法使用程序", Toast.LENGTH_SHORT).show();
			finish();
		}

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "该设备不支持蓝牙，无法使用程序", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// 如果设备没有开启蓝牙则弹出对话框提示用户开启蓝牙
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 如果用户选择不开启蓝牙则直接关闭应用程序
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private OnItemClickListener onclickcistener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			switch (arg2) {
			case 0:
				Intent intent0 = new Intent(CatalogActivity.this, QueryBalanceActivity.class);
				startActivity(intent0);
				break;
				
			case 1:
				Intent intent1 = new Intent(CatalogActivity.this, RechargeActivity.class);
				startActivity(intent1);
				break;
				
			case 2:
				Intent intent2 = new Intent(CatalogActivity.this, QueryTransHistoryActivity.class);
				startActivity(intent2);
				break;
			}
		}

	};

	// 程序退出
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTimeMillis) > 2000) {
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				exitTimeMillis = System.currentTimeMillis();
			} else {
				ArrayList<BaseActivity> list = BaseActivity.getAllActiveActivity();
				for (BaseActivity activity : list) {
					activity.finish();
				}

				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public final class CatalogHolder {
		public ImageView CatalogCellImage;
		public TextView catalogTitleText;
	}

	public class CatalogAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public CatalogAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return imageIds.length;
		}

		public Object getItem(int arg0) {
			return titles[arg0];
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			CatalogHolder holder = null;

			if (null == convertView) {
				convertView = this.mInflater.inflate(R.layout.listitem_catalog, null);
				holder = new CatalogHolder();

				holder.CatalogCellImage = (ImageView) convertView.findViewById(R.id.catalogCellImage);
				holder.catalogTitleText = (TextView) convertView.findViewById(R.id.catalogTitleText);

				convertView.setTag(holder);
			} else {
				holder = (CatalogHolder) convertView.getTag();
			}

			holder.CatalogCellImage.setImageResource(imageIds[position]);
			holder.catalogTitleText.setText(titles[position]);

			return convertView;
		}
	}
}
