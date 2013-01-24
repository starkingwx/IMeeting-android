package com.richitec.imeeting.account;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.richitec.commontoolkit.utils.HttpUtils;
import com.richitec.commontoolkit.utils.HttpUtils.HttpRequestType;
import com.richitec.commontoolkit.utils.HttpUtils.HttpResponseResult;
import com.richitec.commontoolkit.utils.HttpUtils.OnHttpRequestListener;
import com.richitec.commontoolkit.utils.HttpUtils.PostRequestFormat;
import com.richitec.commontoolkit.utils.MyToast;
import com.richitec.imeeting.R;
import com.richitec.imeeting.customcomponent.IMeetingNavigationActivity;
import com.richitec.imeeting.util.AppDataSaveRestoreUtil;

public class AccountRegisterActivity extends IMeetingNavigationActivity {
	private ProgressDialog progressDlg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set content view
		setContentView(R.layout.account_register_activity_step1_layout);

		// set title text
		setTitle(R.string.account_register_nav_title_text);
	}

	public void onGetAuthCodeAction(View v) {
		EditText numberET = (EditText) findViewById(R.id.verificationCode_editText);
		String phoneNumber = numberET.getText().toString().trim();
		if (phoneNumber == null || phoneNumber.equals("")) {
			MyToast.show(this, R.string.number_cannot_be_null,
					Toast.LENGTH_SHORT);
			return;
		}

		progressDlg = ProgressDialog.show(this, null,
				getString(R.string.sending_request));
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("phone", phoneNumber);
		HttpUtils.postRequest(getString(R.string.server_url)
				+ getString(R.string.retrieve_auth_code_url),
				PostRequestFormat.URLENCODED, params, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedGetAuthCode);
	}

	private OnHttpRequestListener onFinishedGetAuthCode = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			dismissProgressDlg();

			try {
				JSONObject data = new JSONObject(
						responseResult.getResponseText());
				String result = data.getString("result");

				if (result.equals("0")) {
					// get phone code successfully, jump to step 2
					setBody(R.layout.account_register_activity_step2_layout);
				} else if (result.equals("2")) {
					MyToast.show(AccountRegisterActivity.this,
							R.string.invalid_phone_number, Toast.LENGTH_SHORT);
				} else if (result.equals("3")) {
					MyToast.show(AccountRegisterActivity.this,
							R.string.existed_phone_number, Toast.LENGTH_SHORT);
				} else {
					MyToast.show(AccountRegisterActivity.this,
							R.string.error_in_retrieve_auth_code,
							Toast.LENGTH_SHORT);
				}

			} catch (Exception e) {
				e.printStackTrace();
				MyToast.show(AccountRegisterActivity.this,
						R.string.error_in_retrieve_auth_code,
						Toast.LENGTH_SHORT);
			}

		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			dismissProgressDlg();
			MyToast.show(AccountRegisterActivity.this,
					R.string.error_in_retrieve_auth_code, Toast.LENGTH_SHORT);
		}
	};

	private void dismissProgressDlg() {
		if (progressDlg != null) {
			progressDlg.dismiss();
		}
	}

	private void setBody(int resID) {
		LinearLayout body = (LinearLayout) getBody();
		body.removeAllViewsInLayout();
		LayoutInflater.from(this).inflate(resID, body);
	}

	public void onVerifyAuthCodeAction(View v) {
		EditText codeET = (EditText) findViewById(R.id.verificationCode_verify_editText);
		String code = codeET.getText().toString().trim();
		if (code == null || code.equals("")) {
			MyToast.show(this, R.string.pls_input_auth_code, Toast.LENGTH_SHORT);
			return;
		}
		progressDlg = ProgressDialog.show(this, null,
				getString(R.string.verifying_auth_code));
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("code", code);
		HttpUtils.postRequest(getString(R.string.server_url)
				+ getString(R.string.check_auth_code_url),
				PostRequestFormat.URLENCODED, params, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedVerifyAuthCode);
	}

	private OnHttpRequestListener onFinishedVerifyAuthCode = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			dismissProgressDlg();

			try {
				JSONObject data = new JSONObject(
						responseResult.getResponseText());
				String result = data.getString("result");

				if (result.equals("0")) {
					// check phone code successfully, jump to step 3 to fill
					// password
					setBody(R.layout.account_register_activity_step3_layout);
				} else if (result.equals("2")) {
					MyToast.show(AccountRegisterActivity.this,
							R.string.wrong_auth_code, Toast.LENGTH_SHORT);
				} else if (result.equals("6")) {
					MyToast.show(AccountRegisterActivity.this,
							R.string.auth_code_timeout, Toast.LENGTH_SHORT);
					setBody(R.layout.account_register_activity_step1_layout);
				}

			} catch (Exception e) {
				e.printStackTrace();
				MyToast.show(AccountRegisterActivity.this, R.string.auth_error,
						Toast.LENGTH_SHORT);
			}
		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			dismissProgressDlg();
			MyToast.show(AccountRegisterActivity.this, R.string.auth_error,
					Toast.LENGTH_SHORT);
		}
	};

	public void onRegisterWithPwdAction(View v) {
		EditText nicknameET = (EditText) findViewById(R.id.nickname_editText);
		EditText pwd1ET = (EditText) findViewById(R.id.pwd_editText);
		EditText pwd2ET = (EditText) findViewById(R.id.confirmationPwd_editText);
		String nickname = nicknameET.getText().toString().trim();
		String pwd1 = pwd1ET.getText().toString().trim();
		String pwd2 = pwd2ET.getText().toString().trim();

		if (nickname == null || nickname.equals("")) {
			MyToast.show(this, R.string.pls_input_nickname, Toast.LENGTH_SHORT);
			return;
		}

		if (pwd1 == null || pwd1.equals("")) {
			MyToast.show(this, R.string.pls_input_pwd, Toast.LENGTH_SHORT);
			return;
		}

		if (pwd2 == null || pwd2.equals("")) {
			MyToast.show(this, R.string.pls_input_confirm_pwd,
					Toast.LENGTH_SHORT);
			return;
		}

		if (!pwd1.equals(pwd2)) {
			MyToast.show(this, R.string.pwd1_is_different_from_pwd2,
					Toast.LENGTH_SHORT);
			return;
		}

		progressDlg = ProgressDialog.show(this, null,
				getString(R.string.finishing_register));
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("password", pwd1);
		params.put("password1", pwd2);
		params.put("nickname", nickname);

		HttpUtils.postRequest(getString(R.string.server_url)
				+ getString(R.string.user_register_url),
				PostRequestFormat.URLENCODED, params, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedRegister);

	}

	private OnHttpRequestListener onFinishedRegister = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			dismissProgressDlg();
			try {
				JSONObject data = new JSONObject(
						responseResult.getResponseText());
				String result = data.getString("result");
				if (result.equals("0")) {
					// register ok, jump to login view
					new AlertDialog.Builder(AccountRegisterActivity.this)
							.setTitle(R.string.alert_title)
							.setMessage(R.string.register_ok)
							.setPositiveButton(R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											popActivity();
										}
									}).show();
				} else if (result.equals("6")) {
					MyToast.show(AccountRegisterActivity.this,
							R.string.register_timeout, Toast.LENGTH_SHORT);
					setBody(R.layout.account_register_activity_step1_layout);
				} else {
					MyToast.show(AccountRegisterActivity.this,
							R.string.error_in_regsiter, Toast.LENGTH_SHORT);
				}
			} catch (Exception e) {
				e.printStackTrace();
				MyToast.show(AccountRegisterActivity.this,
						R.string.error_in_regsiter, Toast.LENGTH_SHORT);
			}
		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			dismissProgressDlg();
			MyToast.show(AccountRegisterActivity.this,
					R.string.error_in_regsiter, Toast.LENGTH_SHORT);
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
