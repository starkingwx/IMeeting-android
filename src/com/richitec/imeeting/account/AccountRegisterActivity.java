package com.richitec.imeeting.account;

import java.util.HashMap;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
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
import com.richitec.commontoolkit.utils.HttpUtils.OnHttpRequestListener;
import com.richitec.commontoolkit.utils.HttpUtils.PostRequestFormat;
import com.richitec.imeeting.R;
import com.richitec.imeeting.customcomponent.IMeetingNavigationActivity;

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
			Toast.makeText(this, R.string.number_cannot_be_null,
					Toast.LENGTH_SHORT).show();
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
		public void onFinished(HttpRequest request, HttpResponse response) {
			dismissProgressDlg();

			try {
				String responseText = EntityUtils.toString(
						response.getEntity(), HTTP.UTF_8);
				JSONObject data = new JSONObject(responseText);
				String result = data.getString("result");

				if (result.equals("0")) {
					// get phone code successfully, jump to step 2
					setBody(R.layout.account_register_activity_step2_layout);
				} else if (result.equals("2")) {
					Toast.makeText(AccountRegisterActivity.this,
							R.string.invalid_phone_number, Toast.LENGTH_SHORT)
							.show();
				} else if (result.equals("3")) {
					Toast.makeText(AccountRegisterActivity.this,
							R.string.existed_phone_number, Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(AccountRegisterActivity.this,
							R.string.error_in_retrieve_auth_code,
							Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(AccountRegisterActivity.this,
						R.string.error_in_retrieve_auth_code,
						Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		public void onFailed(HttpRequest request, HttpResponse response) {
			dismissProgressDlg();
			Toast.makeText(AccountRegisterActivity.this,
					R.string.error_in_retrieve_auth_code, Toast.LENGTH_SHORT)
					.show();
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
			Toast.makeText(this, R.string.pls_input_auth_code,
					Toast.LENGTH_SHORT).show();
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
		public void onFinished(HttpRequest request, HttpResponse response) {
			dismissProgressDlg();

			try {
				String responseText = EntityUtils.toString(
						response.getEntity(), HTTP.UTF_8);
				JSONObject data = new JSONObject(responseText);
				String result = data.getString("result");

				if (result.equals("0")) {
					// check phone code successfully, jump to step 3 to fill
					// password
					setBody(R.layout.account_register_activity_step3_layout);
				} else if (result.equals("2")) {
					Toast.makeText(AccountRegisterActivity.this,
							R.string.wrong_auth_code, Toast.LENGTH_SHORT)
							.show();
				} else if (result.equals("6")) {
					Toast.makeText(AccountRegisterActivity.this,
							R.string.auth_code_timeout, Toast.LENGTH_SHORT)
							.show();
					setBody(R.layout.account_register_activity_step1_layout);
				}

			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(AccountRegisterActivity.this,
						R.string.auth_error, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onFailed(HttpRequest request, HttpResponse response) {
			dismissProgressDlg();
			Toast.makeText(AccountRegisterActivity.this, R.string.auth_error,
					Toast.LENGTH_SHORT).show();
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
			Toast.makeText(this, R.string.pls_input_nickname, Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (pwd1 == null || pwd1.equals("")) {
			Toast.makeText(this, R.string.pls_input_pwd, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (pwd2 == null || pwd2.equals("")) {
			Toast.makeText(this, R.string.pls_input_confirm_pwd,
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (!pwd1.equals(pwd2)) {
			Toast.makeText(this, R.string.pwd1_is_different_from_pwd2,
					Toast.LENGTH_SHORT).show();
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
		public void onFinished(HttpRequest request, HttpResponse response) {
			dismissProgressDlg();
			try {
				String responseText = EntityUtils.toString(
						response.getEntity(), HTTP.UTF_8);
				JSONObject data = new JSONObject(responseText);
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
					Toast.makeText(AccountRegisterActivity.this, R.string.register_timeout, Toast.LENGTH_SHORT).show();
					setBody(R.layout.account_register_activity_step1_layout);
				} else {
					Toast.makeText(AccountRegisterActivity.this, R.string.error_in_regsiter, Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(AccountRegisterActivity.this, R.string.error_in_regsiter, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onFailed(HttpRequest request, HttpResponse response) {
			dismissProgressDlg();
			Toast.makeText(AccountRegisterActivity.this, R.string.error_in_regsiter, Toast.LENGTH_SHORT).show();
		}
	};
}
