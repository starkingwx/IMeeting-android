package com.richitec.imeeting.assistant;

import java.util.HashMap;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.richitec.commontoolkit.utils.HttpUtils;
import com.richitec.commontoolkit.utils.HttpUtils.HttpRequestType;
import com.richitec.commontoolkit.utils.HttpUtils.HttpResponseResult;
import com.richitec.commontoolkit.utils.HttpUtils.OnHttpRequestListener;
import com.richitec.commontoolkit.utils.HttpUtils.PostRequestFormat;
import com.richitec.imeeting.R;
import com.richitec.imeeting.customcomponent.IMeetingNavigationActivity;

public class AccountChargeActivity extends IMeetingNavigationActivity {
	private ProgressDialog progressDlg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_charge);
		setTitle(R.string.account_charge);
		loadAccountBalance();
	}

	private void loadAccountBalance() {
		progressDlg = ProgressDialog.show(this, null,
				getString(R.string.getting_balance));
		HttpUtils.postSignatureRequest(getString(R.string.server_url)
				+ getString(R.string.account_balance_url),
				PostRequestFormat.URLENCODED, null, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedGetAccountBalance);
	}

	private OnHttpRequestListener onFinishedGetAccountBalance = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			dismissProgressDlg();
			try {
				JSONObject data = new JSONObject(responseResult.getResponseText());
				int result = data.getInt("result");
				if (result == 0) {
					double balance = data.getDouble("balance");
					TextView balanceTV = (TextView) findViewById(R.id.account_balance);
					balanceTV.setText(String.format("￥%.2f", balance));
				} else {
					Toast.makeText(AccountChargeActivity.this,
							R.string.get_balance_failed, Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			dismissProgressDlg();
			Toast.makeText(AccountChargeActivity.this,
					R.string.get_balance_failed, Toast.LENGTH_SHORT).show();
		}
	};

	private void dismissProgressDlg() {
		if (progressDlg != null) {
			progressDlg.dismiss();
		}
	}

	public void onChargeClick(View v) {
		EditText numberET = (EditText) findViewById(R.id.card_number_et);
		EditText pwdET = (EditText) findViewById(R.id.card_pwd_et);
		String cardNumber = numberET.getText().toString().trim();
		String cardPwd = pwdET.getText().toString().trim();
		if (cardNumber == null || cardNumber.equals("")) {
			Toast.makeText(this, R.string.input_card_number, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (cardPwd == null || cardPwd.equals("")) {
			Toast.makeText(this, R.string.input_card_pwd, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		progressDlg = ProgressDialog.show(this, null,
				getString(R.string.charging));
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("pin", cardNumber);
		params.put("password", cardPwd);
		HttpUtils.postSignatureRequest(getString(R.string.server_url)
				+ getString(R.string.card_charge_url),
				PostRequestFormat.URLENCODED, params, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedCharge);
	}

	private OnHttpRequestListener onFinishedCharge = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			dismissProgressDlg();
			EditText numberET = (EditText) findViewById(R.id.card_number_et);
			EditText pwdET = (EditText) findViewById(R.id.card_pwd_et);
			numberET.setText("");
			pwdET.setText("");
			new AlertDialog.Builder(AccountChargeActivity.this)
					.setTitle(R.string.alert_title)
					.setMessage(R.string.charge_successfully)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									loadAccountBalance();
								}
							}).show();
		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			dismissProgressDlg();
			int status = responseResult.getStatusCode();

			switch (status) {
			case HttpStatus.SC_NOT_FOUND:
				Toast.makeText(AccountChargeActivity.this,
						R.string.charge_failed_no_account_exist,
						Toast.LENGTH_SHORT).show();
				break;

			case HttpStatus.SC_BAD_REQUEST:
				Toast.makeText(AccountChargeActivity.this,
						R.string.charge_failed_invalid_card_number,
						Toast.LENGTH_SHORT).show();
				break;

			default:
				Toast.makeText(AccountChargeActivity.this,
						R.string.charge_failed, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
}
