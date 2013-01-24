package com.richitec.imeeting.account;

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.richitec.commontoolkit.user.User;
import com.richitec.commontoolkit.user.UserBean;
import com.richitec.commontoolkit.user.UserManager;
import com.richitec.commontoolkit.utils.HttpUtils;
import com.richitec.commontoolkit.utils.HttpUtils.HttpRequestType;
import com.richitec.commontoolkit.utils.HttpUtils.HttpResponseResult;
import com.richitec.commontoolkit.utils.HttpUtils.OnHttpRequestListener;
import com.richitec.commontoolkit.utils.HttpUtils.PostRequestFormat;
import com.richitec.commontoolkit.utils.MyToast;
import com.richitec.imeeting.R;
import com.richitec.imeeting.customcomponent.IMeetingNavigationActivity;
import com.richitec.imeeting.util.AppDataSaveRestoreUtil;

public class AccountGetPasswordActivity extends IMeetingNavigationActivity {
	private ProgressDialog progressDlg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set content view
		setContentView(R.layout.account_get_pwd_layout);

		// set title text
		setTitle(R.string.get_password);

	}

	private void dismissProgressDlg() {
		if (progressDlg != null) {
			progressDlg.dismiss();
		}
	}

	public void onGetPwdAction(View v) {
		EditText phoneET = (EditText) findViewById(R.id.get_pwd_phone_number_input);
		String phoneNumber = phoneET.getText().toString().trim();
		if (phoneNumber.equals("")) {
			MyToast.show(this, R.string.login_name_editText_hint,
					Toast.LENGTH_SHORT);
			return;
		}
		progressDlg = ProgressDialog.show(this, null,
				getString(R.string.sending_request));

		HashMap<String, String> params = new HashMap<String, String>();
		params.put(User.username.name(), phoneNumber);
		HttpUtils.postRequest(getString(R.string.server_url)
				+ getString(R.string.user_get_pwd_url),
				PostRequestFormat.URLENCODED, params, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedGetPwd);
	}

	private OnHttpRequestListener onFinishedGetPwd = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			dismissProgressDlg();
			new AlertDialog.Builder(AccountGetPasswordActivity.this)
					.setTitle(R.string.alert_title)
					.setMessage(R.string.ur_pwd_is_sent_to_your_phone)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							UserBean user = UserManager.getInstance().getUser();
							user.setRememberPwd(false);
							user.setPassword("");
							Intent intent = new Intent(AccountGetPasswordActivity.this, AccountSettingActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}
					}).show();
		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			dismissProgressDlg();
			switch (responseResult.getStatusCode()) {
			case 404:
				MyToast.show(AccountGetPasswordActivity.this,
						R.string.no_such_phone, Toast.LENGTH_SHORT);
				break;

			default:
				MyToast.show(AccountGetPasswordActivity.this,
						R.string.network_error, Toast.LENGTH_SHORT);
				break;
			}
		}
	};
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		AppDataSaveRestoreUtil.onRestoreInstanceState(savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		AppDataSaveRestoreUtil.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}
}
