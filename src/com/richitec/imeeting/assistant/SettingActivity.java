package com.richitec.imeeting.assistant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.richitec.commontoolkit.addressbook.AddressBookManager;
import com.richitec.commontoolkit.addressbook.ContactBean;
import com.richitec.commontoolkit.customcomponent.CommonPopupWindow;
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
import com.richitec.commontoolkit.utils.StringUtils;
import com.richitec.imeeting.R;
import com.richitec.imeeting.account.AccountSettingActivity;
import com.richitec.imeeting.constants.CloudAddressBookConstants;
import com.richitec.imeeting.customcomponent.IMeetingNavigationActivity;

public class SettingActivity extends IMeetingNavigationActivity {
	private ProgressDialog progressDlg;

	private ModifyPSWPopupWindow modifyPSWPopupWindow;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set content view
		setContentView(R.layout.setting_activity_layout);

		// set title text
		setTitle(R.string.setting_nav_title_text);

		// bind account setting button on click listener

		((LinearLayout) findViewById(R.id.accountSetting_item))
				.setOnClickListener(new AccountSettingBtnOnClickListener());
		((LinearLayout) findViewById(R.id.about_item))
				.setOnClickListener(new AboutBtnOnClickListener());
		((LinearLayout) findViewById(R.id.help_item))
				.setOnClickListener(new HelpBtnOnClickListener());

	}

	// inner class
	// account setting button on click listener
	class AccountSettingBtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// go to user account setting activity
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("first_login", false);
			pushActivity(AccountSettingActivity.class, data);
		}

	}

	// help button on click listener
	class HelpBtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// go to help activity
			pushActivity(SupportActivity.class);
		}

	}

	// about button on click listener
	class AboutBtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// go to about activity
			pushActivity(AboutActivity.class);
		}

	}

	public void onAddressBookUploadButtonClick(View v) {
		new AlertDialog.Builder(this)
				.setTitle(R.string.addressbook_upload_btn_title)
				.setMessage(R.string.need_upload_addressbook)
				.setPositiveButton(R.string.backup,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								progressDlg = ProgressDialog
										.show(SettingActivity.this,
												null,
												getString(R.string.uploading_address_book));
								new Thread(new Runnable() {

									@Override
									public void run() {
										uploadAddressBook();
									}
								}).start();
							}
						}).setNegativeButton(R.string.cancel, null).show();
	}

	private void uploadAddressBook() {
		String accountName = UserManager.getInstance().getUser().getName();

		AddressBookManager abm = AddressBookManager.getInstance();
		List<ContactBean> contacts = abm.getAllContactsInfoArray();
		JSONArray contactArray = new JSONArray();

		for (ContactBean contact : contacts) {
			StringBuffer searchName = new StringBuffer();
			List<String> fullNames = contact.getFullNames();
			if (fullNames != null) {
				for (String name : fullNames) {
					searchName.append(name);
				}
			}

			JSONObject contactJson = new JSONObject();
			try {
				contactJson.put(CloudAddressBookConstants.owner.name(),
						accountName);
				contactJson.put(CloudAddressBookConstants.display_name.name(),
						contact.getDisplayName());

				contactJson.put(
						CloudAddressBookConstants.phonetic_array.name(),
						new JSONArray(
								contact.getNamePhonetics() != null ? contact
										.getNamePhonetics()
										: new ArrayList<String>()));
				contactJson.put(
						CloudAddressBookConstants.name_array.name(),
						new JSONArray(contact.getFullNames() != null ? contact
								.getFullNames() : new ArrayList<String>()));
				contactJson.put(CloudAddressBookConstants.search_name.name(),
						searchName.toString());
				contactJson.put(
						CloudAddressBookConstants.phone_array.name(),
						new JSONArray(
								contact.getPhoneNumbers() != null ? contact
										.getPhoneNumbers()
										: new ArrayList<String>()));

			} catch (JSONException e) {
				e.printStackTrace();
			}
			contactArray.put(contactJson);
		}
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("contacts", contactArray.toString());
		HttpUtils.postSignatureRequest(getString(R.string.server_url)
				+ getString(R.string.upload_addressbook_url),
				PostRequestFormat.URLENCODED, params, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedUploadAddressBook);
	}

	private OnHttpRequestListener onFinishedUploadAddressBook = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			dismissProgressDlg();
			new AlertDialog.Builder(SettingActivity.this)
					.setTitle(R.string.alert_title)
					.setMessage(R.string.addressbook_upload_ok)
					.setPositiveButton(R.string.ok, null).show();
		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			dismissProgressDlg();
			new AlertDialog.Builder(SettingActivity.this)
					.setTitle(R.string.alert_title)
					.setMessage(R.string.addressbook_upload_fail)
					.setPositiveButton(R.string.ok, null).show();
		}
	};

	private void dismissProgressDlg() {
		if (progressDlg != null) {
			progressDlg.dismiss();
		}
	}

	public void onChangePwdAction(View v) {
		modifyPSWPopupWindow = new ModifyPSWPopupWindow(
				R.layout.modify_psw_popupwindow_layout,
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		modifyPSWPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
	}

	class ModifyPSWPopupWindow extends CommonPopupWindow {

		public ModifyPSWPopupWindow(int resource, int width, int height,
				boolean focusable, boolean isBindDefListener) {
			super(resource, width, height, focusable, isBindDefListener);
		}

		public ModifyPSWPopupWindow(int resource, int width, int height) {
			super(resource, width, height);
		}

		@Override
		protected void bindPopupWindowComponentsListener() {

			// bind contact phone select cancel button click listener
			((Button) getContentView().findViewById(R.id.modify_psw_confirmBtn))
					.setOnClickListener(new ModifyPSWConfirmBtnOnClickListener());
			((Button) getContentView().findViewById(R.id.modify_psw_cancelBtn))
					.setOnClickListener(new ModifyPSWCancelBtnOnClickListener());
		}

		@Override
		protected void resetPopupWindow() {
			// hide contact phones select phone list view
			((EditText) getContentView().findViewById(R.id.old_psw_editText))
					.setText("");
			((EditText) getContentView().findViewById(R.id.new_psw_editText))
					.setText("");
			((EditText) getContentView()
					.findViewById(R.id.confirm_psw_editText)).setText("");
		}

		// inner class
		// contact phone select phone button on click listener
		class ModifyPSWConfirmBtnOnClickListener implements OnClickListener {

			@Override
			public void onClick(View v) {
				// dismiss contact phone select popup window
				String oldpsw = ((EditText) getContentView().findViewById(
						R.id.old_psw_editText)).getEditableText().toString()
						.trim();
				String newpsw = ((EditText) getContentView().findViewById(
						R.id.new_psw_editText)).getEditableText().toString()
						.trim();
				String confirm = ((EditText) getContentView().findViewById(
						R.id.confirm_psw_editText)).getEditableText()
						.toString().trim();

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(((EditText) getContentView()
						.findViewById(R.id.old_psw_editText)).getWindowToken(),
						0);
				modifyPSW(oldpsw, newpsw, confirm);
			}

		}

		// contact phone select cancel button on click listener
		class ModifyPSWCancelBtnOnClickListener implements OnClickListener {

			@Override
			public void onClick(View v) {
				// dismiss contact phone select popup window
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(((EditText) getContentView()
						.findViewById(R.id.old_psw_editText)).getWindowToken(),
						0);
				dismiss();
			}

		}

	}

	private void modifyPSW(String oldpsw, String newpsw, String confirm) {
		if (oldpsw == null || oldpsw.equals("")) {
			MyToast.show(this, R.string.old_psw_not_null, Toast.LENGTH_SHORT);
			return;
		}

		if (newpsw == null || confirm == null || newpsw.equals("")
				|| confirm.equals("")) {
			MyToast.show(this, R.string.new_confirm_not_null,
					Toast.LENGTH_SHORT);
			return;
		}

		UserBean userBean = UserManager.getInstance().getUser();
		String oldmd5 = StringUtils.md5(oldpsw);
		String username = userBean.getName();
		if (!newpsw.equals(confirm)) {
			MyToast.show(this, R.string.new_confirm_not_equal,
					Toast.LENGTH_SHORT);
			return;
		}

		userBean.setPassword(StringUtils.md5(newpsw));

		progressDlg = ProgressDialog.show(this, null,
				getString(R.string.sending_request), true);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("oldPwd", oldmd5);
		params.put("newPwd", newpsw);
		params.put("newPwdConfirm", confirm);
		Log.d("modify", "modify");
		HttpUtils.postSignatureRequest(getString(R.string.server_url)
				+ getString(R.string.modify_psw_url),
				PostRequestFormat.URLENCODED, params, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedModifyPsw);

	}

	private OnHttpRequestListener onFinishedModifyPsw = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			dismissProgressDlg();
			modifyPSWPopupWindow.dismiss();

			try {
				JSONObject ret = new JSONObject(
						responseResult.getResponseText());
				String userkey = ret.getString(User.userkey.name());
				UserBean user = UserManager.getInstance().getUser();
				user.setUserKey(userkey);
				if (user.isRememberPwd()) {
					DataStorageUtils.putObject(User.password.name(),
							user.getPassword());
					DataStorageUtils.putObject(User.userkey.name(),
							user.getUserKey());
				}
				new AlertDialog.Builder(SettingActivity.this)
						.setTitle(R.string.alert_title)
						.setMessage(R.string.modify_success)
						.setPositiveButton(R.string.ok, null).show();
			} catch (JSONException e) {
				e.printStackTrace();
				MyToast.show(SettingActivity.this, R.string.server_error,
						Toast.LENGTH_SHORT);
			}

		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			dismissProgressDlg();
			int code = responseResult.getStatusCode();
			if (code == 401) {
				MyToast.show(SettingActivity.this, R.string.auth_not_pass,
						Toast.LENGTH_SHORT);
			} else if (code == 400) {
				MyToast.show(SettingActivity.this, R.string.new_psw_error,
						Toast.LENGTH_SHORT);
			} else if (code == 500) {
				MyToast.show(SettingActivity.this, R.string.server_error,
						Toast.LENGTH_SHORT);
			}
		}
	};
	
	public void onSMSShareAction(View v) {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"));
		String smsBody = getString(R.string.app_share_content);
		intent.putExtra("sms_body", smsBody);
		startActivity(intent);
	}
}
