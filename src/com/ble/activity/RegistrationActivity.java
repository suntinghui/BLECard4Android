package com.ble.activity;

import com.ble.R;
import com.ble.client.ApplicationEnvironment;
import com.ble.client.BLEClient;
import com.ble.client.Constants;
import com.ble.util.ByteUtil;
import com.ble.util.SecurityUtil;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegistrationActivity extends BaseActivity implements OnClickListener {

	private EditText deviceNumText = null;
	private EditText pwdText = null;
	private Button regiButton = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_registration);

		this.deviceNumText = (EditText) this.findViewById(R.id.et_deviceNum);
		this.pwdText = (EditText) this.findViewById(R.id.et_pwd);
		this.regiButton = (Button) this.findViewById(R.id.btn_registration);
		this.regiButton.setOnClickListener(this);
		
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

		return true;
	}

	@Override
	public void onClick(View view) {
		if (!checkValue())
			return;

		if (view.getId() == R.id.btn_registration) {
			String md5Str = SecurityUtil.MD5Crypto(pwdText.getText().toString().trim());
			Log.e("===", md5Str);
			
			Editor editor = ApplicationEnvironment.getInstance().getPreferences().edit();
			editor.putString(Constants.SECURITY_KEY, md5Str);
			editor.commit();
			
			Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();;
		}
	}

}
