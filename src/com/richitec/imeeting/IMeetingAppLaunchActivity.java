package com.richitec.imeeting;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.richitec.commontoolkit.activityextension.AppLaunchActivity;
import com.richitec.commontoolkit.addressbook.AddressBookManager;
import com.richitec.commontoolkit.user.UserBean;
import com.richitec.commontoolkit.user.UserManager;
import com.richitec.imeeting.account.AccountSettingActivity;
import com.richitec.imeeting.talkinggroup.TalkingGroupHistoryListActivity;
import com.richitec.imeeting.util.AppDataSaveRestoreUtil;

public class IMeetingAppLaunchActivity extends AppLaunchActivity {
	// main activity class name storage key
	@Override
	public Drawable splashImg() {
		return getResources().getDrawable(R.drawable.ic_splash);
	}

	@Override
	public Intent intentActivity() {
		//start contact syncService
//		Intent service = new Intent(this, ContactSyncService.class);
//		startService(service);
		
		AppDataSaveRestoreUtil.loadAccount();
		
		Intent intent = null;
		UserBean user = UserManager.getInstance().getUser();
		if (user.getPassword() != null && !user.getPassword().equals("")
				&& user.getUserKey() != null && !user.getUserKey().equals("")) {
			intent = new Intent(IMeetingAppLaunchActivity.this,
					TalkingGroupHistoryListActivity.class);
		} else {
			intent = new Intent(IMeetingAppLaunchActivity.this,
					AccountSettingActivity.class);
		}

		return intent;
	}

	@Override
	public void didFinishLaunching() {
		// traversal address book
		AddressBookManager.setFilterMode(AddressBookManager.FILTER_IP_AND_CODE_PREFIX);
		AddressBookManager.getInstance().traversalAddressBook();
	}

	@Override
	public void doPostExecute() {
		AddressBookManager.getInstance().registContactOberver();
	}
	
	@Override
	public void onBackPressed() {
		AddressBookManager.getInstance().unRegistContactObserver();
	}
	
	@Override
	protected void onRestoreInstanceState (Bundle savedInstanceState) {
		AppDataSaveRestoreUtil.onRestoreInstanceState(savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		AppDataSaveRestoreUtil.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

}
