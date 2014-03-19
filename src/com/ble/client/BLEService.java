package com.ble.client;

import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ble.util.ByteUtil;

public class BLEService extends Service {

	private final static String TAG = BLEService.class.getSimpleName();

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGatt mBluetoothGatt = null;

	private BluetoothGattService identifiedService = null;
	private BluetoothGattCharacteristic identifiedCharacter_write = null;
	private BluetoothGattCharacteristic identifiedCharacter_notify = null;

	private int mConnectionState = STATE_DISCONNECTED;
	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public BLEService getService() {
			return BLEService.this;
		}
	}

	@Override
	public IBinder onBind(Intent args) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		close();
		return super.onUnbind(intent);
	}

	public boolean connect() {
		Log.e(TAG, "开始尝试连接设备...");
		
		this.disconnect();
		
		Log.e(TAG, "××××××××××××××××××××××××××××××");
		
		if (null == mBluetoothAdapter) {
			mBluetoothAdapter = BLEUtil.getBluetoothAdapter();
		}
		
		String address = BLEUtil.getMyDevice().getAddress();
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		mConnectionState = STATE_CONNECTING;
		
		return true;
	}

	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			return;
		}
		
		mBluetoothGatt.disconnect();
	}

	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}

		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	public boolean writeCharacteristic(byte[] value) {
		Log.e("SEND", ByteUtil.bytesToHexString(value));

		identifiedCharacter_write.setValue(value);

		boolean status = mBluetoothGatt.writeCharacteristic(identifiedCharacter_write);
		Log.e("writeCharacteristic", status?"写成功！":"写失败！");
		return status;
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			Log.e(TAG, "onConnectionStateChange");

			String intentAction;
			
			if (newState == BluetoothProfile.STATE_CONNECTING) {
				intentAction = Constants.ACTION_GATT_CONNECTING;
				mConnectionState = STATE_CONNECTING;
				Log.e(TAG, "Connecting to GATT server");
				
				broadcastUpdate(intentAction);
				
			} else if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = Constants.ACTION_GATT_CONNECTED;
				mConnectionState = STATE_CONNECTED;
				Log.e(TAG, "Connected to GATT server.");
				
				broadcastUpdate(intentAction);

				// 开始查找服务
				mBluetoothGatt.discoverServices();

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = Constants.ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
				Log.e(TAG, "Disconnected from GATT server.");

				broadcastUpdate(intentAction);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.e(TAG, "onServicesDiscovered-" + status);

			if (status == BluetoothGatt.GATT_SUCCESS) {
				
				identifiedService = mBluetoothGatt.getService(UUID.fromString(Constants.SERVICE_UUID));
				if (identifiedService == null) {
					Log.e(TAG, "没有找到指定的Service！！！");
				}

				identifiedCharacter_write = identifiedService.getCharacteristic(UUID.fromString(Constants.Characteristic_UUID_WRITE));
				BLEService.this.setCharacteristicNotification(identifiedCharacter_write, false);
				if (identifiedCharacter_write == null) {
					Log.e(TAG, "在服务中没有找到指定的Characteristic write！！！");
				}

				identifiedCharacter_notify = identifiedService.getCharacteristic(UUID.fromString(Constants.Characteristic_UUID_NOTIFY));
				BLEService.this.setCharacteristicNotification(identifiedCharacter_notify, true);
				if (identifiedCharacter_notify == null) {
					Log.e(TAG, "在服务中没有找到指定的Characteristic notify！！！");
				}
				
				broadcastUpdate(Constants.ACTION_GATT_SERVICES_DISCOVERED);

			} else {
				Log.e(TAG, "ERROR onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			Log.e(TAG, "onCharacteristicWrite");
			// BLEService.this.setCharacteristicNotification(identifiedCharacter_notify, true);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			Log.e(TAG, "onCharacteristicRead");
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			Log.e(TAG, "onCharacteristicChanged****************************");
			Log.e("REV", "<" + ByteUtil.bytesToHexString(characteristic.getValue()) + "> " + characteristic.getValue().length);

			broadcastUpdate(Constants.ACTION_DATA_AVAILABLE, characteristic.getValue());
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			Log.e(TAG, "onDescriptorRead");
			super.onDescriptorRead(gatt, descriptor, status);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			Log.e(TAG, "onDescriptorWrite");
			super.onDescriptorWrite(gatt, descriptor, status);

			broadcastUpdate(Constants.ACTION_GATT_DESCRIPTOR_WRITED);
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			Log.e(TAG, "onReliableWriteCompleted");
			super.onReliableWriteCompleted(gatt, status);
		}
	};

	public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		if (Constants.Characteristic_UUID_NOTIFY.equals(characteristic.getUuid().toString())) {
			BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(Constants.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID));
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			boolean flag = mBluetoothGatt.writeDescriptor(descriptor);
			return flag;
		}
		return false;
	}

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action, byte[] value) {
		final Intent intent = new Intent(action);
		intent.putExtra("DATA", value);
		sendBroadcast(intent);
	}
}