package com.ble.ex.client;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.ble.ex.activity.BaseActivity;
import com.ble.ex.model.TransferModel;
import com.ble.ex.util.ByteUtil;
import com.ble.ex.util.SecurityUtil;

public class BLEClient {

	private final static String TAG = BLEClient.class.getSimpleName();

	public static BLEClient instance = null;

	public BLEService mBLEService;

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

	private int mAuthState = STATE_AUTO_NO;
	
	private final static int STATE_AUTO_NO 				= 0;
	private final static int STATE_AUTO_IN 				= 1;
	private final static int STATE_AUTO_OUT				= 2;
	private final static int STATE_AUTO_DONE 			= 3;
	private final static int STATE_AUTO_FAIURE 			= 4;

	private BLETransferTypeEnum currentTypeTemp;
	private String XORKey = null;

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
	public void sendData(BELActionListener listener, BLETransferTypeEnum type, byte[] value) {
		BaseActivity.getTopActivity().showDialog(BaseActivity.PROGRESS_DIALOG, "正在处理请稍候");

		if (mBLEService != null) {
			ApplicationEnvironment.getInstance().getApplication().unbindService(BLEClient.getInstance().mServiceConnection);
			BLEClient.getInstance().mBLEService = null;
		}

		Log.e(TAG, "================================================");
		Log.e(TAG, "sendData........");

		mAuthState = STATE_AUTO_NO;
		currentType = type;
		bleListener = listener;
		byteValue = value;

		Intent gattServiceIntent = new Intent(ApplicationEnvironment.getInstance().getApplication(), BLEService.class);
		ApplicationEnvironment.getInstance().getApplication().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	public ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBLEService = ((BLEService.LocalBinder) service).getService();
			
			mBLEService.connect();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBLEService = null;
		}
	};

	// 发送报文
	public void sendPack() {
		if (!mConnected) {
			return;
		}

		if (Constants.SECURITY_VERSION){
			if (mAuthState == STATE_AUTO_NO) {
				currentTypeTemp = currentType;

				doAuthIn();
			}
		}

		if (byteList.isEmpty()) {
			byteList = this.formatSendByte(byteValue);
		}

		// TODO
		if (nowDataSign >= byteList.size()) {
			return;
		}

		try {
			byte[] tempValue = byteList.get(nowDataSign);
			nowDataSign++;
			temp = tempValue[0];

			nowSendState = 1;

			mBLEService.writeCharacteristic(tempValue);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 做内部认证
	private void doAuthIn() {
		Log.e("AUTH", "发起内部认证");

		mAuthState = STATE_AUTO_IN;

		currentType = BLETransferTypeEnum.TRANSFER_AUTH_INNER;

		byte[] inValue = new byte[] { (byte) 0x10, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x84, (byte) 0x18 };

		byteList.clear();
		byteList.add(inValue);
		nowDataSign = 0;

		this.sendPack();
	}

	// 做外部认证
	private void doAuthOut(String hexStr) {
		Log.e("AUTH", "发起外部认证");

		mAuthState = STATE_AUTO_OUT;

		currentType = BLETransferTypeEnum.TRANSFER_AUTH_OUTER;

		byte[] outValue1 = new byte[] { (byte) 0x85, (byte) 0x08 };
		byte[] outValue2 = ByteUtil.hexStringToBytes(hexStr);
		byte[] outValue = ByteUtil.cancat(outValue1, outValue2);

		byteList = this.formatSendByte(outValue);

		this.sendPack();
	}

	private void sendRevRequest() {
		mBLEService.writeCharacteristic(new byte[] { temp });
		temp++;
	}

	private void sendDisConnRequest() {
		revPt = 0;
		temp = 0;
		nowSendState = 0;
		nowDataSign = 0;
		byteList.clear();

		Log.e("Req Dis", "发送断开请求命令...");

		byte[] disValue = new byte[] { (byte) 0x10, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x02, (byte) 0x00 };

		byteList.add(disValue);

		this.sendPack();
	}

	private ArrayList<byte[]> formatSendByte(byte[] tempByte) {
		Log.e("---", "<" + ByteUtil.bytesToHexString(tempByte) + ">");

		// 需要做XOR加密
		if (currentType.getType() == (byte) 0x04) {
			String str = SecurityUtil.xorHex(ByteUtil.byteArr2HexStr(tempByte), XORKey);
			tempByte = ByteUtil.hexStringToBytes(str);

			Log.e("---XOR:", "<" + str + ">");
		}

		ArrayList<byte[]> tempList = new ArrayList<byte[]>();

		int length = tempByte.length;
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
					tempData[j + 4] = tempByte[start];
				}
				dataLength = j + 4;
			} else {
				int j = 0;
				for (; start < length && j + 1 < 20; start++, j++) {
					tempData[j + 1] = tempByte[start];
				}
				dataLength = j + 1;
			}

			byte[] temp = new byte[dataLength];
			System.arraycopy(tempData, 0, temp, 0, dataLength);

			Log.e("TEMP SEND", "---" + ByteUtil.bytesToHexString(temp) + "---");

			tempList.add(temp);
		}

		return tempList;
	}

	public void parse(byte[] respByte) {
		try {
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

					revData = new byte[revLength];

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
					for (; i < 20 && revPt < revLength && i<respByte.length; revPt++, i++) {
						revData[revPt] = respByte[i];
					}
				}

				if (temp < ((revLength + (19 - 16 - 1)) / 19 + 1)) {
					this.sendRevRequest();

				} else {
					// 需要做XOR加密
					if (currentType.getType() == (byte) 0x04) {
						String str = SecurityUtil.xorHex(ByteUtil.byteArr2HexStr(revData), XORKey);
						revData = ByteUtil.hexStringToBytes(str);
					}

					if (currentType.getId() == BLETransferTypeEnum.TRANSFER_AUTH_INNER.getId()) { // 内部认证
						String revString = ByteUtil.byteArr2HexStr(revData).substring(4, 52);
						Log.e("AUTH", revString);

						String random1 = revString.substring(0, 16);
						String value1 = revString.substring(16, 32);
						String random2 = revString.substring(32, 48);

						String key = ApplicationEnvironment.getInstance().getPreferences().getString(Constants.SECURITY_KEY, "");
						String desValue1 = ByteUtil.byteArr2HexStr(SecurityUtil.TripleDESEncry(ByteUtil.hexStringToBytes(key), ByteUtil.hexStringToBytes(random1)));

						if (value1.equalsIgnoreCase(desValue1)) {
							Log.e("AUTH", "内部认证成功...");

							byte[] desByte2 = SecurityUtil.TripleDESEncry(ByteUtil.hexStringToBytes(key), ByteUtil.hexStringToBytes(random2));
							String desValue2 = ByteUtil.byteArr2HexStr(desByte2);
							String desValue3 = ByteUtil.byteArr2HexStr(SecurityUtil.TripleDESEncry(ByteUtil.hexStringToBytes(key), desByte2));

							XORKey = desValue3;

							this.doAuthOut(desValue2 + desValue3);

							return;

						} else {
							BaseActivity.getTopActivity().showDialog(BaseActivity.MODAL_DIALOG, "内部认证失败，请重试");
							mAuthState = STATE_AUTO_FAIURE;
						}

					} else if (currentType.getId() == BLETransferTypeEnum.TRANSFER_AUTH_OUTER.getId()) { // 外部认证
						String revString = ByteUtil.byteArr2HexStr(revData).substring(4, 6);
						Log.e("AUTH", revString);

						if (revString.equalsIgnoreCase("90")) {
							Log.e("AUTH", "外部认证成功...");

							mAuthState = STATE_AUTO_DONE;

							currentType = currentTypeTemp;
							byteList.clear();

							this.sendPack();

							return;

						} else {
							BaseActivity.getTopActivity().showDialog(BaseActivity.MODAL_DIALOG, "外部认证失败，请重试");
							mAuthState = STATE_AUTO_FAIURE;
						}

					} else if (currentType.getId() == BLETransferTypeEnum.TRANSFER_QUERYBALANCE.getId()) {
						int money = (revData[5] & 0xFF) * 256 * 256 + (revData[6] & 0xFF) * 256 + (revData[7] & 0xFF);
						Log.e("money", money + "");
						int state = (revData[8] & 0xFF) * 256 + (revData[9] & 0xFF);

						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("type", currentType.getId());
						map.put("money", Integer.valueOf(money));
						map.put("state", Boolean.valueOf(state == 0x90 * 256 + 0x00));
						bleListener.bleAction(map);

						BaseActivity.getTopActivity().hideDialog(BaseActivity.PROGRESS_DIALOG);

					} else if (currentType.getId() == BLETransferTypeEnum.TRANSFER_QUERYHISTORY.getId()) {
						ArrayList<TransferModel> modelList = new ArrayList<TransferModel>();

						String revString = ByteUtil.byteArr2HexStr(revData);

						Log.e("+++", revString);

						int start = 3;
						for (; start < revLength;) {
							int curLength = revData[start] & 0xFF; // 25
							// Log.e("***", "curLength:"+curLength);

							if (curLength == 0x19) {
								TransferModel model = new TransferModel();
								model.setNum((revData[start + 1] & 0xFF) * 256 + (revData[start + 2] & 0xFF));
								model.setTouzhi((revData[start + 3] & 0xFF) * 256 * 256 + (revData[start + 4] & 0xFF) * 256 + (revData[start + 5] & 0xFF));
								model.setJiaoyi((revData[start + 6] & 0xFF) * 256 * 256 * 256 + (revData[start + 7] & 0xFF) * 256 * 256 + (revData[start + 8] & 0xFF) * 256 + (revData[start + 9] & 0xFF));
								model.setType(revData[start + 10] & 0xFF);
								// model.setZhongduan(String.format("%.2x%.2x%.2x%.2x%.2x%.2x", revData[start + 11] & 0xFF, revData[start + 12] &0xFF, revData[start + 13] & 0xFF, revData[start +14] & 0xFF, revData[start + 15] & 0xFF,revData[start + 16] & 0xFF));
								// model.setDate((revData[start + 17] & 0xFF) * 256 * 256 * 256 + (revData[start + 18] & 0xFF) * 256 * 256 + (revData[start + 19] & 0xFF) * 256 + revData[start + 20] & 0xFF);
								// model.setTime((revData[start + 21] & 0xFF) * 256 * 256 + (revData[start + 22] & 0xFF) * 256 + revData[start + 23] & 0xFF);

								model.setZhongduan(revString.substring((start + 11) * 2, (start + 17) * 2));
								model.setDate(revString.substring((start + 17) * 2, (start + 21) * 2));
								model.setTime(revString.substring((start + 21) * 2, (start + 24) * 2));

								modelList.add(model);

								start += curLength + 1;

								// Log.e("***", "start:"+start); // 29 55
							} else {
								break;
							}

						}

						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("type", currentType.getId());
						map.put("list", modelList);
						map.put("state", Boolean.valueOf(true));
						bleListener.bleAction(map);

						BaseActivity.getTopActivity().hideDialog(BaseActivity.PROGRESS_DIALOG);

					} else if (currentType.getId() == BLETransferTypeEnum.TRANSFER_RECHARGE.getId()) {
						int state = (revData[4] & 0xFF) * 256 + (revData[5] & 0xFF);

						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("type", currentType.getId());
						map.put("state", Boolean.valueOf(state == 0x90 * 256 + 0x00));
						bleListener.bleAction(map);

						byteValue = new byte[] { (byte) 0x07, (byte) 0x00, (byte) 0xa4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x10, (byte) 0x01, (byte) 0x05, (byte) 0x80, (byte) 0x5c, (byte) 0x00, (byte) 0x02, (byte) 0x04 };
						currentType = BLETransferTypeEnum.TRANSFER_QUERYBALANCE;
						this.byteList.clear();
						this.sendPack();

						BaseActivity.getTopActivity().hideDialog(BaseActivity.PROGRESS_DIALOG);
					}

					// 断开链接
					sendDisConnRequest();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			BaseActivity.getTopActivity().hideDialog(BaseActivity.PROGRESS_DIALOG);
			
			BaseActivity.getTopActivity().showDialog(BaseActivity.MODAL_DIALOG, "程序异常，请重试");

			revLength = 0;
			revPt = 0;
			temp = 0;
			nowSendState = 0;
			byteList.clear();
		}

	}

	public BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
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

	public IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_GATT_CONNECTING);
		intentFilter.addAction(Constants.ACTION_GATT_CONNECTED);
		intentFilter.addAction(Constants.ACTION_GATT_DISCONNECTED);

		intentFilter.addAction(Constants.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(Constants.ACTION_GATT_DESCRIPTOR_WRITED);
		intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

}