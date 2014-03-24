package com.ble.ex.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.ble.ex.R;
import com.ble.ex.client.BELActionListener;
import com.ble.ex.client.BLEClient;
import com.ble.ex.client.BLETransferTypeEnum;
import com.ble.ex.model.TransferModel;
import com.ble.ex.util.DateUtil;

public class QueryTransHistoryActivity extends BaseActivity implements BELActionListener, OnClickListener {

	private ListView listView = null;
	private ArrayList<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();

	private Button backBtn = null;
	private Button queryBtn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_transfer_history);

		listView = (ListView) this.findViewById(R.id.transList);
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, mapList, R.layout.listitem_transfer_history, new String[] { "transNum", "transType", "transAmount", "transTime", "transTerm" }, new int[] { R.id.transNum, R.id.transType, R.id.transAmount, R.id.transTime, R.id.transTerm });
		listView.setAdapter(listItemAdapter);

		backBtn = (Button) this.findViewById(R.id.btn_back);
		backBtn.setOnClickListener(this);
		queryBtn = (Button) this.findViewById(R.id.btn_query);
		queryBtn.setOnClickListener(this);
	}

	private void queryHistory() {
		byte[] tempData = new byte[350];
		tempData[0] = (byte) 0x07;
		tempData[1] = (byte) 0x00;
		tempData[2] = (byte) 0xa4;
		tempData[3] = (byte) 0x00;
		tempData[4] = (byte) 0x00;
		tempData[5] = (byte) 0x02;
		tempData[6] = (byte) 0x10;
		tempData[7] = (byte) 0x01;

		for (int i = 0; i < 10; i++) {
			int j = 8 + i * 6;
			tempData[j] = (byte) 0x05;
			tempData[j + 1] = (byte) 0x00;
			tempData[j + 2] = (byte) 0xb2;
			tempData[j + 3] = (byte) (i + 1);
			tempData[j + 4] = (byte) 0xc4;
			tempData[j + 5] = (byte) 0x17;
		}

		byte[] value = new byte[8 + 10 * 6];
		System.arraycopy(tempData, 0, value, 0, value.length);

		BLEClient.getInstance().sendData(this, BLETransferTypeEnum.TRANSFER_QUERYHISTORY, value);
	}

	@Override
	public void bleAction(Object obj) {
		mapList.clear();
		
		HashMap<String, Object> map = (HashMap<String, Object>) obj;
		ArrayList<TransferModel> modelList = (ArrayList<TransferModel>) map.get("list");

		Log.e("明细", "历史记录数：" + modelList.size());
		
		for (TransferModel model : modelList) {
			HashMap<String, String> tempMap = new HashMap<String, String>();
			tempMap.put("transNum", model.getNum() + "");
			tempMap.put("transType", this.getTransType(model.getType()));
			tempMap.put("transAmount", this.getAmount(model.getJiaoyi()));
			tempMap.put("transTime", DateUtil.formatDateTime(model.getDate()+model.getTime()));
			tempMap.put("transTerm", model.getZhongduan());

			mapList.add(tempMap);
		}

		((SimpleAdapter) listView.getAdapter()).notifyDataSetChanged();
	}

	/*
	public String getTime(TransferModel model) {
		long date = model.getDate();
		long time = model.getTime();

		long year = date / (256 * 256);
		year = year / (256 * 16) * 1000 + (year % (256 * 16)) / (256) * 100 + (year % 256) / 16 * 10 + (year % 16);
		long month = date % (256 * 256) / 256;
		month = month / 16 * 10 + month % 16;
		long day = date % 256;
		day = day / 16 * 10 + day % 16;

		long hour = time / (256 * 256);
		hour = hour / 16 * 10 + hour % 16;
		long min = time % (256 * 256) / 256;
		min = min / 16 * 10 + min % 16;
		long second = time % 256;
		second = second / 16 * 10 + second % 16;

		StringBuffer sb = new StringBuffer();
		sb.append(year).append("-").append(month).append("-").append(day);
		sb.append(" ").append(hour).append(":").append(min).append(":").append(second);

		return sb.toString();
	}
	*/

	public String getAmount(int amount) {
		String val1 = String.format("%,d", amount / 100);
		String val2 = String.format("%2d", amount % 100);

		return val1 + "." + val2 + " 元";
	}

	private String getTransType(int type) {
		String typeStr = "未知";
		switch (type) {
		case 1:
			typeStr = "存折圈存";
			break;
		case 2:
			typeStr = "钱包圈存";
			break;
		case 3:
			typeStr = "圈提";
			break;
		case 4:
			typeStr = "存折取款";
			break;
		case 5:
			typeStr = "存折消费";
			break;
		case 6:
			typeStr = "钱包消费";
			break;
		case 7:
			typeStr = "存折修改透支限额";
			break;
		default:
			typeStr = "专用";
			break;
		}

		return typeStr;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_back) {
			this.finish();
			
		} else if (view.getId() == R.id.btn_query) {
			mapList.clear();
			((SimpleAdapter) listView.getAdapter()).notifyDataSetChanged();
			
			this.queryHistory();
			
		}
	}

}
