package com.ble.activity;

import java.util.HashSet;
import java.util.Iterator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ble.R;
import com.ble.client.ApplicationEnvironment;
import com.ble.client.BLEUtil;
import com.ble.client.Constants;
import com.ble.util.SecurityUtil;

public class RegistrationActivity extends BaseActivity implements OnClickListener {

	private EditText deviceNumText = null;
	private EditText pwdText = null;
	private Button regiButton = null;
	private Button backButton = null;

	private static boolean mScanning;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_registration);

		this.deviceNumText = (EditText) this.findViewById(R.id.et_deviceNum);
		this.pwdText = (EditText) this.findViewById(R.id.et_pwd);
		this.regiButton = (Button) this.findViewById(R.id.btn_registration);
		this.regiButton.setOnClickListener(this);
		this.backButton = (Button) this.findViewById(R.id.btn_back);
		this.backButton.setOnClickListener(this);

		// this.deviceNumText.setText("BLE CARD:12345678901234567890");
		this.pwdText.setText("22334455");
	}

	private boolean checkValue() {
		if (deviceNumText.getText().equals("")) {
			Toast.makeText(this, "请输入设备账号", Toast.LENGTH_SHORT).show();
			return false;
		} else if (pwdText.getText().equals("")) {
			Toast.makeText(this, "请输入认证号", Toast.LENGTH_SHORT).show();
			return false;
		} else if (pwdText.getText().length() < 4) {
			Toast.makeText(this, "认证号不能少于4位。", Toast.LENGTH_SHORT).show();
			return false;
		}

		HashSet<String> devices = (HashSet<String>) ApplicationEnvironment.getInstance().getPreferences().getStringSet(Constants.MY_DEVICELIST, new HashSet<String>());
		for (Iterator<String> it = devices.iterator(); it.hasNext();) {
			if (it.next().split("&")[0].equalsIgnoreCase(BLEUtil.getSimpleDeviceName(this.deviceNumText.getText().toString()))) {
				BaseActivity.getTopActivity().showDialog(BaseActivity.MODAL_DIALOG, "该设备已经注册！");
				return false;
			}
		}

		return true;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_back) {
			this.finish();

		} else if (view.getId() == R.id.btn_registration) {
			if (!checkValue())
				return;

			String md5Str = SecurityUtil.MD5Crypto(pwdText.getText().toString().trim());
			Log.e("===", md5Str);

			Editor editor = ApplicationEnvironment.getInstance().getPreferences().edit();
			editor.putString(Constants.SECURITY_KEY, md5Str);
			editor.commit();

			matchBLEDevice();
		}
	}

	private void matchBLEDevice() {
		BaseActivity.getTopActivity().showDialog(BaseActivity.PROGRESS_DIALOG, "请触发设备...");

		scanLeDevice(true);
	}

	private void scanLeDevice(boolean enable) {
		Handler mHandler = new Handler();

		if (enable) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					BLEUtil.getBluetoothAdapter().stopLeScan(mLeScanCallback);
				}
			}, Constants.SCAN_PERIOD);

			mScanning = true;
			BLEUtil.getBluetoothAdapter().startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			BLEUtil.getBluetoothAdapter().stopLeScan(mLeScanCallback);
		}
	}

	// BLE设备的搜索结果将通过这个callback返回
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			Log.e("===", device.getName() + "---" + device.getAddress());

			String deviceName = BLEUtil.getSimpleDeviceName(device.getName());
			String matchName = BLEUtil.getSimpleDeviceName(deviceNumText.getText().toString());

			mScanning = false;
			BLEUtil.getBluetoothAdapter().stopLeScan(mLeScanCallback);

			if (deviceName.equals(matchName)) {
				HashSet<String> deviceSet = (HashSet<String>) ApplicationEnvironment.getInstance().getPreferences().getStringSet(Constants.MY_DEVICELIST, new HashSet<String>());
				deviceSet.add(deviceName + "&" + device.getAddress());

				Editor editor = ApplicationEnvironment.getInstance().getPreferences().edit();
				editor.putStringSet(Constants.MY_DEVICELIST, deviceSet);
				editor.commit();
				
				BLEUtil.resetDeviceSet();

				BaseActivity.getTopActivity().showDialog(BaseActivity.MODAL_DIALOG, "注册成功，设备已添加。");
				
			} else {
				BaseActivity.getTopActivity().showDialog(BaseActivity.MODAL_DIALOG, "注册失败，不能匹配设备。");
			}

			BaseActivity.getTopActivity().hideDialog(BaseActivity.PROGRESS_DIALOG);

		}
	};

}
