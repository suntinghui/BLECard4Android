package com.ble.ex.activity;

import java.util.HashMap;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ble.ex.R;
import com.ble.ex.client.BELActionListener;
import com.ble.ex.client.BLEClient;
import com.ble.ex.client.BLETransferTypeEnum;
import com.ble.ex.util.DateUtil;

public class RechargeActivity extends BaseActivity implements OnClickListener, BELActionListener {

	private LinearLayout balanceLayout = null;
	private TextView balanceView = null;
	private EditText moneyEdit = null;
	private Button queryBtn = null;
	private Button backBtn = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_recharge);

		balanceLayout = (LinearLayout) this.findViewById(R.id.layout_balance);
		balanceView = (TextView) this.findViewById(R.id.tv_balance);
		moneyEdit = (EditText) this.findViewById(R.id.et_money);
		queryBtn = (Button) this.findViewById(R.id.btn_recharge);
		queryBtn.setOnClickListener(this);
		backBtn = (Button) this.findViewById(R.id.btn_back);
		backBtn.setOnClickListener(this);

		balanceView.setText("");
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_back) {
			this.finish();

		} else if (view.getId() == R.id.btn_recharge) {
			if (moneyEdit.getText().length() == 0) {
				Toast.makeText(RechargeActivity.this, "请输入金额", Toast.LENGTH_SHORT).show();
				return;
			}

			byte[] value1 = new byte[] { (byte) 0x07, (byte) 0x00, (byte) 0xa4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x10, (byte) 0x01, (byte) 0x15, (byte) 0x80, (byte) 0x7a, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

			// 充值金额
			double rechargeValue = Integer.parseInt(moneyEdit.getText().toString()) * 100;
			byte[] rechargeByte = new byte[3];
			rechargeByte[0] = (byte) (rechargeValue / (256 * 256));
			rechargeByte[1] = (byte) (rechargeValue % (256 * 256) / 256);
			rechargeByte[2] = (byte) (rechargeValue % 256);

			// 日期
			byte[] dateByte = new byte[7];
			dateByte[0] = (byte) (DateUtil.getCurrentYear() / 1000 * 16 + DateUtil.getCurrentYear() % 1000 / 100);
			dateByte[1] = (byte) (DateUtil.getCurrentYear() % 100 / 10 * 16 + DateUtil.getCurrentYear() % 10);
			dateByte[2] = (byte) (DateUtil.getCurrentMonth() / 10 * 16 + DateUtil.getCurrentMonth() % 10);
			dateByte[3] = (byte) (DateUtil.getCurrentDay() / 10 * 16 + DateUtil.getCurrentDay() % 10);
			dateByte[4] = (byte) (DateUtil.getCurrentHour() / 10 * 16 + DateUtil.getCurrentHour() % 10);
			dateByte[5] = (byte) (DateUtil.getCurrentMin() / 10 * 16 + DateUtil.getCurrentMin() % 10);
			dateByte[6] = (byte) (DateUtil.getCurrentSec() / 10 * 16 + DateUtil.getCurrentSec() % 10);

			byte[] value = new byte[30];
			System.arraycopy(value1, 0, value, 0, value1.length);
			System.arraycopy(rechargeByte, 0, value, value1.length, rechargeByte.length);
			System.arraycopy(dateByte, 0, value, value1.length + rechargeByte.length, dateByte.length);

			BLEClient.getInstance().sendData(this, BLETransferTypeEnum.TRANSFER_RECHARGE, value);
		}
	}

	@Override
	public void bleAction(Object obj) {
		HashMap<String, Object> map = (HashMap<String, Object>) obj;
		int id = (Integer) map.get("type");

		if (id == BLETransferTypeEnum.TRANSFER_QUERYBALANCE.getId()) {
			int money = (Integer) map.get("money");
			boolean state = (Boolean) map.get("state");
			if (state) {
				String val1 = String.format("%,d", money / 100);
				String val2 = String.format("%2d", money % 100);

				balanceLayout.setVisibility(View.VISIBLE);
				balanceView.setText(val1 + "." + val2);
			} else {
				balanceLayout.setVisibility(View.VISIBLE);
				balanceView.setText("查询余额失败");
			}

		} else if (id == BLETransferTypeEnum.TRANSFER_RECHARGE.getId()) {
			boolean state = (Boolean) map.get("state");
			if (state) {
				Toast.makeText(this, "充值成功！", Toast.LENGTH_SHORT).show();

				this.moneyEdit.setText("");

			} else {
				Toast.makeText(this, "充值失败！", Toast.LENGTH_SHORT).show();
			}

		}
	}
}
