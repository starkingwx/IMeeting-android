package com.richitec.imeeting.account;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.richitec.commontoolkit.user.User;
import com.richitec.commontoolkit.user.UserBean;
import com.richitec.commontoolkit.user.UserManager;
import com.richitec.commontoolkit.utils.DataStorageUtils;
import com.richitec.commontoolkit.utils.HttpUtils;
import com.richitec.commontoolkit.utils.HttpUtils.HttpRequestType;
import com.richitec.commontoolkit.utils.HttpUtils.HttpResponseResult;
import com.richitec.commontoolkit.utils.HttpUtils.OnHttpRequestListener;
import com.richitec.commontoolkit.utils.HttpUtils.PostRequestFormat;
import com.richitec.commontoolkit.utils.MyToast;
import com.richitec.imeeting.R;
import com.richitec.imeeting.constants.SystemConstants;
import com.richitec.imeeting.customcomponent.IMeetingNavigationActivity;
import com.richitec.imeeting.talkinggroup.TalkingGroupHistoryListActivity;
import com.richitec.imeeting.util.AppUpdateManager;

public class AccountSettingActivity extends IMeetingNavigationActivity {
	private ProgressDialog progressDialog;
	private String loginUrl;
	// private SharedPreferences userInfoSettings;

	private boolean useSavedPwd;
	private String PWD_MASK = "#@1d~`*)";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set content view
		setContentView(R.layout.account_setting_activity_layout);

		// set title text
		setTitle(R.string.account_setting_nav_title_text);

		loginUrl = getString(R.string.server_url)
				+ getString(R.string.login_url);

		useSavedPwd = true;

		EditText phoneET = (EditText) findViewById(R.id.login_name_editText);
		phoneET.addTextChangedListener(onTextChanged);
		EditText pwdET = (EditText) findViewById(R.id.login_pwd_editText);
		pwdET.addTextChangedListener(onTextChanged);
		CheckBox rememberPwdCB = (CheckBox) findViewById(R.id.account_remember_psw_cbtn);
		UserBean user = UserManager.getInstance().getUser();
		phoneET.setText(user.getName());

		if (user.getPassword() != null && !user.getPassword().equals("")) {
			pwdET.setText(PWD_MASK);
			rememberPwdCB.setChecked(true);
		} else {
			rememberPwdCB.setChecked(false);
		}

		useSavedPwd = rememberPwdCB.isChecked();

		AppUpdateManager aum = new AppUpdateManager(this);
		aum.checkVersion();
	}

	private TextWatcher onTextChanged = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			useSavedPwd = false;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}
	};

	public void onRegister(View v) {
		pushActivity(AccountRegisterActivity.class);
	}

	public void onLogin(View v) {
		EditText phoneET = (EditText) findViewById(R.id.login_name_editText);
		EditText pwdET = (EditText) findViewById(R.id.login_pwd_editText);
		CheckBox remeberPwdCB = (CheckBox) findViewById(R.id.account_remember_psw_cbtn);
		boolean rememberPwd = remeberPwdCB.isChecked();
		String userName = phoneET.getText().toString().trim();
		String password = pwdET.getText().toString().trim();

		if (userName.equals("")) {
			MyToast.show(this, R.string.number_cannot_be_null,
					Toast.LENGTH_SHORT);
			return;
		}

		if (password.equals("")) {
			MyToast.show(this, R.string.password_cannot_be_null,
					Toast.LENGTH_SHORT);
			return;
		}

		if (!useSavedPwd) {
			UserManager.getInstance().setUser(userName, password);
		}
		UserBean user = UserManager.getInstance().getUser();
		user.setRememberPwd(rememberPwd);

		progressDialog = ProgressDialog.show(this, null,
				getString(R.string.logining), true);
		HashMap<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("loginName", user.getName());
		paramMap.put("loginPwd", user.getPassword());
		HttpUtils.postRequest(loginUrl, PostRequestFormat.URLENCODED, paramMap,
				null, HttpRequestType.ASYNCHRONOUS, onFinishedLogin);
	}
	
	public void onForgetPSWBtnClick(View v) {
		
	}

	private OnHttpRequestListener onFinishedLogin = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			try {

				JSONObject data = new JSONObject(
						responseResult.getResponseText());
				String result = data.getString("result");
				if (result.equals("0")) {
					// login success
					loginSuccess(data);
				} else if (result.equals("1") || result.equals("2")) {
					loginFailed();
				} else {
					loginError();
				}

			} catch (Exception e) {
				e.printStackTrace();
				loginError();
			}

		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			loginError();
		}
	};

	public void loginError() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		MyToast.show(this, R.string.login_error, Toast.LENGTH_LONG);
	}

	public void loginFailed() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		MyToast.show(this, R.string.login_failed, Toast.LENGTH_LONG);
	}

	public void loginSuccess(JSONObject data) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		try {
			String userKey = data.getString("userkey");
			UserManager.getInstance().setUserKey(userKey);
			saveUserAccount();
//			pushActivity(TalkingGroupHistoryListActivity.class);
			Intent intent = new Intent(AccountSettingActivity.this, TalkingGroupHistoryListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			
			finish();

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void saveUserAccount() {
		Log.d(SystemConstants.TAG, "save user account");
		UserBean user = UserManager.getInstance().getUser();
		Log.d(SystemConstants.TAG, "user: " + user.toString());
		DataStorageUtils.putObject(User.username.name(), user.getName());
		if (user.isRememberPwd()) {
			DataStorageUtils
					.putObject(User.password.name(), user.getPassword());
			DataStorageUtils.putObject(User.userkey.name(), user.getUserKey());
		} else {
			DataStorageUtils.putObject(User.password.name(), "");
			user.setPassword("");
		}
	}
}
