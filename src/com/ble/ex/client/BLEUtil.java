package com.ble.ex.client;

import java.util.HashSet;
import java.util.Iterator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

public class BLEUtil {

	private static BluetoothAdapter mBluetoothAdapter = null;

	private static boolean mScanning;
	private static Handler mHandler;

	private static BluetoothDevice myDevice = null;

	private static HashSet<String> myDeviceSet = null;

	// 判断设备是否支持蓝牙
	public static boolean isSupportBLE() {
		return ApplicationEnvironment.getInstance().getApplication().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
	}

	public static HashSet<String> getMyDeviceSet() {
		if (myDeviceSet == null) {
			myDeviceSet = (HashSet<String>) ApplicationEnvironment.getInstance().getPreferences().getStringSet(Constants.MY_DEVICELIST, new HashSet<String>());
		}

		return myDeviceSet;
	}
	
	public static void resetDeviceSet(){
		myDeviceSet = null;
	}

	public static BluetoothManager getBluetoothManager() {
		BluetoothManager bluetoothManager = (BluetoothManager) ApplicationEnvironment.getInstance().getApplication().getSystemService(Context.BLUETOOTH_SERVICE);
		return bluetoothManager;
	}

	public static BluetoothAdapter getBluetoothAdapter() {
		if (null == mBluetoothAdapter) {
			BluetoothManager bluetoothManager = getBluetoothManager();
			mBluetoothAdapter = bluetoothManager.getAdapter();
		}

		return mBluetoothAdapter;
	}

	// 由于搜索需要尽量减少功耗，因此在实际使用时需要注意：
	// 1、当找到对应的设备后，立即停止扫描；
	// 2、不要循环搜索设备，为每次搜索设置适合的时间限制。避免设备不在可用范围的时候持续不停扫描，消耗电量。
	private static void scanLeDevice(boolean enable) {
		mHandler = new Handler();

		if (enable) {
			// Stops scanning after a pre-defined scan period.
			// 使用PostDelayed方法，两秒后调用此Runnable对象，停止扫描。
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
	private static BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			Log.e("===", device.getName() + "---" + device.getAddress());

			for (Iterator<String> it = getMyDeviceSet().iterator(); it.hasNext();) {
				String str = it.next();
				if (str.split("&")[0].equalsIgnoreCase(BLEUtil.getSimpleDeviceName(device.getName()))&&str.split("&")[1].equalsIgnoreCase(device.getAddress())) {
					myDevice = device;

					mScanning = false;
					BLEUtil.getBluetoothAdapter().stopLeScan(mLeScanCallback);
				}
			}
		}
	};

	public static BluetoothDevice getMyDevice() {
		scanLeDevice(true);

		while (mScanning) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return myDevice;
	}

	public static String getSimpleDeviceName(String deviceName) {
		// BLE CARD:12345678901234567890 -> BLECARD12345678901234567890
		return deviceName.replace(" ", "").replace(":", "").replace("：", "").toUpperCase().trim();
	}
}
