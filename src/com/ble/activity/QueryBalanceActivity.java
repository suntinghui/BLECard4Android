package com.ble.activity;

import java.util.HashMap;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.ble.R;
import com.ble.client.BELActionListener;
import com.ble.client.BLEClient;
import com.ble.client.BLETransferTypeEnum;

public class QueryBalanceActivity extends BaseActivity implements OnClickListener, BELActionListener {

	private TextView balanceView = null;
	private Button queryBtn = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_querybalance);

		balanceView = (TextView) this.findViewById(R.id.tv_balance);
		queryBtn = (Button) this.findViewById(R.id.btn_query);
		queryBtn.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(BLEClient.getInstance().mGattUpdateReceiver, BLEClient.getInstance().makeGattUpdateIntentFilter());
	}

	protected void onPause() {
		super.onPause();
		unregisterReceiver(BLEClient.getInstance().mGattUpdateReceiver);
	}

	protected void onDestroy() {
		super.onDestroy();

		try {
			unbindService(BLEClient.getInstance().mServiceConnection);
			BLEClient.getInstance().mBluetoothLeService = null;
		} catch (Exception e) {

		}

	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_query) {
			balanceView.setText("");

			byte[] value = new byte[] { (byte) 0x07, (byte) 0x00, (byte) 0xa4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x10, (byte) 0x01, (byte) 0x05, (byte) 0x80, (byte) 0x5c, (byte) 0x00, (byte) 0x02, (byte) 0x04 };
			BLEClient.getInstance().sendData(this, this, BLETransferTypeEnum.TRANSFER_QUERYBALANCE, value);
		}
	}

	@Override
	public void bleAction(Object obj) {
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = (HashMap<String, Object>) obj;
		int money = (Integer) map.get("money");
		boolean state = (Boolean) map.get("state");

		if (state) {
			String val1 = String.format("%,d", money / 100);
			String val2 = String.format("%2d", money % 100);

			balanceView.setText(val1 + "." + val2 + " 元");
		} else {
			balanceView.setText("查询失败");
		}

	}

}
