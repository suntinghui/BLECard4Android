package com.ble.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ble.R;
import com.ble.client.ApplicationEnvironment;
import com.ble.client.Constants;
import com.ble.util.SecurityUtil;
import com.ble.view.LKAlertDialog;

public class RegistrationActivity extends BaseActivity implements OnClickListener {

	private EditText deviceNumText = null;
	private EditText pwdText = null;
	private Button regiButton = null;
	private Button backButton = null;

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

			BaseActivity.getTopActivity().showDialog(BaseActivity.MODAL_DIALOG, "注册成功，密钥已生成。");
		}
	}
	
}
