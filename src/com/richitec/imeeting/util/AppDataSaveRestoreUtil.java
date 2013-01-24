package com.richitec.imeeting.util;

import android.os.Bundle;
import android.util.Log;

import com.richitec.commontoolkit.addressbook.AddressBookManager;
import com.richitec.commontoolkit.user.User;
import com.richitec.commontoolkit.user.UserBean;
import com.richitec.commontoolkit.user.UserManager;
import com.richitec.commontoolkit.utils.DataStorageUtils;
import com.richitec.imeeting.constants.SystemConstants;

public class AppDataSaveRestoreUtil {
	public static void onSaveInstanceState (Bundle outState) {
		UserBean user = UserManager.getInstance().getUser();
		outState.putString(User.username.name(), user.getName());
	}
	
	
	public static void onRestoreInstanceState (Bundle savedInstanceState) {
		Log.d(SystemConstants.TAG, "AppDataSaveRestoreUtil - onRestoreInstanceState");
		if (!AddressBookManager.getInstance().isInited()) {
			AddressBookManager.getInstance().traversalAddressBook();
			AddressBookManager.getInstance().registContactOberver();
		}
		
		String userName = savedInstanceState.getString(User.username.name());
		
		UserBean user = UserManager.getInstance().getUser();
		if (userName == null || userName.equals("")) {
		} else if (user.getName() == null || user.getName().equals("")) {
			loadAccount();
		}
		
	}
	
	public static void loadAccount() {
		String userName = DataStorageUtils.getString(User.username.name());
		String userkey = DataStorageUtils.getString(User.userkey.name());
		String password = DataStorageUtils.getString(User.password.name());

		UserBean user = new UserBean();
		user.setName(userName);
		user.setUserKey(userkey);
		user.setPassword(password);
		
		
		if (password != null && !password.equals("") && userkey != null
				&& !userkey.equals("")) {
			user.setRememberPwd(true);
		}
		UserManager.getInstance().setUser(user);
		Log.d(SystemConstants.TAG, " load account: " + user.toString());
	}
}
