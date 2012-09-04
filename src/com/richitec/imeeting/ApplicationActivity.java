package com.richitec.imeeting;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.richitec.commontoolkit.user.User;
import com.richitec.commontoolkit.user.UserBean;
import com.richitec.commontoolkit.user.UserManager;
import com.richitec.imeeting.account.AccountSettingActivity;
import com.richitec.imeeting.constants.SystemConstants;
import com.richitec.imeeting.talkinggroup.TalkingGroupHistoryListActivity;

public class ApplicationActivity extends Activity {

	// delay 3 seconds
	private final int SPLASH_DISPLAY_TIME = 1000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// loading splash image
		setContentView(R.layout.application_activity_layout);

		loadAccount();
		// process application loading
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// go to user account setting activity and finish application
				// root activity
				UserBean user = UserManager.getInstance().getUser();
				if (user.getPassword() != null
						&& !user.getPassword().equals("")
						&& user.getUserkey() != null
						&& !user.getUserkey().equals("")) {
					Intent intent = new Intent(ApplicationActivity.this,
							TalkingGroupHistoryListActivity.class);
					startActivity(intent);
				} else {
					Intent _intent = new Intent(ApplicationActivity.this,
							AccountSettingActivity.class);
					startActivity(_intent);
				}
				finish();
			}
		}, SPLASH_DISPLAY_TIME);
	}

	private void loadAccount() {
		SharedPreferences userInfoSettings = getSharedPreferences(
				SystemConstants.USER_INFO, 0);
		String userName = userInfoSettings.getString(User.username.name(), "");
		String userkey = userInfoSettings.getString(User.userkey.name(), "");
		String password = userInfoSettings.getString(User.password.name(), "");
		UserBean userBean = new UserBean();
		userBean.setName(userName);
		userBean.setUserkey(userkey);
		userBean.setPassword(password);
	}
}
