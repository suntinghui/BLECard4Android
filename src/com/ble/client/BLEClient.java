package com.ble.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.ble.model.TransferModel;
import com.ble.util.ByteUtil;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.GpsStatus.Listener;
import android.os.IBinder;
import android.util.Log;

public class BLEClient {

	public static BLEClient instance = null;

	private BLEService mBluetoothLeService;

	private BLETransferTypeEnum currentType;
	private BELActionListener bleListener;
	private byte[] byteValue;

	private ArrayList<byte[]> byteList = new ArrayList<byte[]>();

	private int nowDataSign = 0;
	private int nowSendState = 0;
	private byte temp;

	private int revLength = 0;
	private byte[] revData = new byte[350];
	private int revPt = 0;

	private boolean mConnected = false;

	public static BLEClient getInstance() {
		if (null == instance) {
			instance = new BLEClient();
		}

		return instance;
	}

	public BLEClient() {
		ApplicationEnvironment.getInstance().getApplication().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	// 只负责初始化组织数据
	public void sendData(Context context, BELActionListener listener, BLETransferTypeEnum type, byte[] value) {
		currentType = type;
		bleListener = listener;
		byteValue = value;

		Intent gattServiceIntent = new Intent(context, BLEService.class);
		context.bindService(gattServiceIntent, new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName componentName, IBinder service) {
				mBluetoothLeService = ((BLEService.LocalBinder) service).getService();
				mBluetoothLeService.connect();
			}

			@Override
			public void onServiceDisconnected(ComponentName componentName) {
				mBluetoothLeService = null;
			}
		}, Context.BIND_AUTO_CREATE);
	}

	// 发送报文
	public void sendPack() {
		if (!mConnected) {
			return;
		}

		if (byteList.isEmpty()) {
			this.formatSendByte();
		}

		// TODO
		try {
			byte[] tempValue = byteList.get(nowDataSign);
			nowDataSign++;
			temp = tempValue[0];

			nowSendState = 1;

			mBluetoothLeService.writeCharacteristic(tempValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendRevRequest() {
		mBluetoothLeService.writeCharacteristic(new byte[] { temp });
		temp++;
	}

	private void sendDisConnRequest() {
		Log.e("Req Dis", "发送断开请求命令...");
		byte[] disValue = new byte[] { (byte) 0x10, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x02, (byte) 0x00 };

		byteList.clear();
		byteList.add(disValue);
		nowDataSign = 0;

		this.sendPack();
	}

	private void formatSendByte() {
		Log.e("---", "<" + ByteUtil.bytesToHexString(byteValue) + ">");
		
		int length = byteValue.length;
		for (int start = 0; start < length;) {
			byte[] tempData = new byte[20];
			tempData[0] = (byte) (((length + (19 - 16 - 1)) / 19 + 1) * 16 + ((start + (19 - 16)) / 19));
			int dataLength = 0;
			if (start == 0) {
				tempData[1] = currentType.getType();
				tempData[2] = (byte) (length / 256);
				tempData[3] = (byte) (length % 256);
				
				int j = 0;
				for (; start < length && j + 4 < 20; start++, j++) {
					tempData[j + 4] = byteValue[start];
				}
				dataLength = j + 4;
			} else {
				int j = 0;
				for (; start < length && j + 1 < 20; start++, j++) {
					tempData[j + 1] = byteValue[start];
				}
				dataLength = j + 1;
			}

			byte[] tempByte = new byte[dataLength];
			System.arraycopy(tempData, 0, tempByte, 0, dataLength);

			Log.e("TEMP SEND", "---" + ByteUtil.bytesToHexString(tempByte) + "---");

			byteList.add(tempByte);
		}
	}

	public void parse(byte[] respByte) {
		int length = respByte.length;
		if (nowSendState == 1) {
			if (length == 1 && temp == respByte[0]) {
				if (nowDataSign >= byteList.size()) {
					byteList.clear();
					nowDataSign = 0;
					nowSendState = 2;

					temp = 0;
					this.sendRevRequest();
					return;
				} else {
					this.sendPack();
				}
			}
		} else {
			if (nowSendState == 2) {
				int type = respByte[1];
				if (type != currentType.getType() + 0x10) {
					return;
				}

				revLength = respByte[2] * 256 + respByte[3];

				int i = 4;
				for (; i < 20 && i < length; i++) {
					revData[i - 4] = respByte[i];
				}

				if (length > 16) {
					revPt = 16;
				}

				nowSendState = 3;
				
			} else if (nowSendState == 3) {
				int i = 1;
				for (; i < 20 && revPt < revLength; revPt++, i++) {
					revData[revPt] = respByte[i];
				}
			}

			if (temp < ((revLength + (19 - 16 - 1)) / 19 + 1)) {
				this.sendRevRequest();
			} else {
				if (currentType.getId() == BLETransferTypeEnum.TRANSFER_QUERYBALANCE.getId()) {
					int money = (revData[5] & 0xFF) * 256 * 256 + (revData[6] & 0xFF) * 256 + (revData[7] & 0xFF);
					Log.e("money", money + "");
					int state = (revData[8] & 0xFF) * 256 + (revData[9] & 0xFF);

					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("type", currentType.getId());
					map.put("money", Integer.valueOf(money));
					map.put("state", Boolean.valueOf(state == 0x90 * 256 + 0x00));
					bleListener.bleAction(map);

				} else if (currentType.getId() == BLETransferTypeEnum.TRANSFER_QUERYHISTORY.getId()) {
					ArrayList<TransferModel> modelList = new ArrayList<TransferModel>();

					int start = 3;
					for (; start < revLength;) {
						int curLength = revData[start] & 0xFF;
						if (curLength == 0x19) {
							TransferModel model = new TransferModel();
							model.setNum(revData[start + 1] & 0xFF * 256 + revData[start + 2] & 0xFF);
							model.setTouzhi(revData[start + 3] & 0xFF * 256 * 256 + revData[start + 4] & 0xFF * 256 + revData[start + 5] & 0xFF);
							model.setJiaoyi(revData[start + 6] & 0xFF * 256 * 256 * 256 + revData[start + 7] & 0xFF * 256 * 256 + revData[start + 8] & 0xFF * 256 + revData[start + 9] & 0xFF);
							model.setType(revData[start + 10] & 0xFF);
							model.setZhongduan(String.format("%.2x%.2x%.2x%.2x%.2x%.2x", revData[start + 11] & 0xFF, revData[start + 12] & 0xFF, revData[start + 13] & 0xFF, revData[start + 14] & 0xFF, revData[start + 15] & 0xFF, revData[start + 16] & 0xFF));
							model.setDate(revData[start + 17] & 0xFF * 256 * 256 * 256 + revData[start + 18] & 0xFF * 256 * 256 + revData[start + 19] & 0xFF * 256 + revData[start + 20] & 0xFF);
							model.setTime(revData[start + 21] & 0xFF * 256 * 256 + revData[start + 22] & 0xFF * 256 + revData[start + 23] & 0xFF);

							modelList.add(model);
							start += curLength + 1;
						} else {
							break;
						}

						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("type", currentType.getId());
						map.put("list", modelList);
						map.put("state", Boolean.valueOf(true));
						bleListener.bleAction(map);
					}

				} else if (currentType.getId() == BLETransferTypeEnum.TRANSFER_RECHARGE.getId()) {
					int state = (revData[4] & 0xFF) * 256 + (revData[5] & 0xFF);
					
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("type", currentType.getId());
					map.put("state", Boolean.valueOf(state == 0x90 * 256 + 0x00));
					bleListener.bleAction(map);
				}

				Log.e("---", "FINISH");
				revPt = 0;
				temp = 0;
				nowSendState = 0;

				// 断开链接
				sendDisConnRequest();
			}
		}

	}

	private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (Constants.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;

			} else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
				mConnected = false;

			} else if (Constants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

			} else if (Constants.ACTION_GATT_DESCRIPTOR_WRITED.equals(action)) {
				sendPack();

			} else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
				byte[] respValue = intent.getByteArrayExtra("DATA");
				parse(respValue);
			}
		}
	};

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_GATT_CONNECTED);
		intentFilter.addAction(Constants.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(Constants.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(Constants.ACTION_GATT_DESCRIPTOR_WRITED);
		intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

}
