package com.ble.activity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
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
import com.ble.client.BLEUtil;

@SuppressLint("CommitPrefEdits")
public class CatalogActivity extends BaseActivity {

	private Integer[] imageIds = { R.drawable.btn_message, R.drawable.btn_payment, R.drawable.btn_reimburse, R.drawable.btn_travel, R.drawable.btn_else, R.drawable.btn_set };

	private ArrayList<String> badgeList = new ArrayList<String>();
	private GridView gridView = null;
	private CatalogAdapter adapter = null;

	private long exitTimeMillis = 0;

	private boolean firstLoad = true;

	private BluetoothAdapter mBluetoothAdapter;

	private static final int REQUEST_ENABLE_BT = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_catalog);

		gridView = (GridView) findViewById(R.id.gridveiw);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(onclickcistener);

		// 初始化
		for (int i = 0; i < imageIds.length; i++) {
			badgeList.add(i, "0");
		}
		adapter = new CatalogAdapter(this);
		gridView.setAdapter(adapter);

		this.checkBLEEnvironment();

	}

	private void checkBLEEnvironment() {
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

	@Override
	protected void onPause() {
		super.onPause();

		firstLoad = false;
	}

	@Override
	protected void onNewIntent(Intent intent) {

	}

	// 点击事件
	private OnItemClickListener onclickcistener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			switch (arg2) {
			case 0:
				if (CatalogActivity.this.checkRegiDevice()) {
					Intent intent0 = new Intent(CatalogActivity.this, QueryBalanceActivity.class);
					startActivity(intent0);
				}
				break;

			case 1:
				if (CatalogActivity.this.checkRegiDevice()) {
					Intent intent1 = new Intent(CatalogActivity.this, RechargeActivity.class);
					startActivity(intent1);
				}

				break;

			case 2:
				if (CatalogActivity.this.checkRegiDevice()) {
					Intent intent2 = new Intent(CatalogActivity.this, QueryTransHistoryActivity.class);
					startActivity(intent2);
				}

				break;

			case 3:
			case 4:
				Toast.makeText(CatalogActivity.this, "暂未实现此功能", Toast.LENGTH_SHORT).show();
				break;

			case 5:
				Intent intent5 = new Intent(CatalogActivity.this, RegistrationActivity.class);
				startActivity(intent5);
				break;
			}
		}

	};

	private boolean checkRegiDevice() {
		if (BLEUtil.getMyDeviceSet().isEmpty()) {
			BaseActivity.getTopActivity().showDialog(BaseActivity.MODAL_DIALOG, "您尚未注册设备，请您先添加设备。");

			return false;
		}

		return true;
	}

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
		public ImageView badgeImage;
		public TextView badgeNumText;
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
			return arg0;
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			CatalogHolder holder = null;

			if (null == convertView) {
				convertView = this.mInflater.inflate(R.layout.item_catalog, null);
				holder = new CatalogHolder();

				holder.CatalogCellImage = (ImageView) convertView.findViewById(R.id.catalogCellImage);
				holder.badgeImage = (ImageView) convertView.findViewById(R.id.badgeImageView);
				holder.badgeNumText = (TextView) convertView.findViewById(R.id.badgeNumText);

				convertView.setTag(holder);
			} else {
				holder = (CatalogHolder) convertView.getTag();
			}

			holder.CatalogCellImage.setImageResource(imageIds[position]);

			if (badgeList.get(position).equals("0")) {
				holder.badgeImage.setVisibility(View.GONE);
				holder.badgeNumText.setVisibility(View.GONE);
			} else {
				holder.badgeImage.setVisibility(View.VISIBLE);
				holder.badgeNumText.setVisibility(View.VISIBLE);
				String tmp = badgeList.get(position);
				int numInt = Integer.parseInt(tmp);
				if (numInt > 99) {
					tmp = "99";
				}
				holder.badgeNumText.setText(tmp);
			}

			return convertView;
		}
	}

}
