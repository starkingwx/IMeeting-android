package com.richitec.imeeting.util;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.richitec.commontoolkit.utils.HttpUtils;
import com.richitec.commontoolkit.utils.HttpUtils.HttpRequestType;
import com.richitec.commontoolkit.utils.HttpUtils.HttpResponseResult;
import com.richitec.commontoolkit.utils.HttpUtils.OnHttpRequestListener;
import com.richitec.commontoolkit.utils.VersionUtils;
import com.richitec.imeeting.R;
import com.richitec.imeeting.constants.SystemConstants;

public class AppUpdateManager {
	private Context context;
	public AppUpdateManager(Context context) {
		this.context = context;
	}
	
	public void checkVersion() {
		if (VersionUtils.checkVersion) {
			VersionUtils.localVersion = VersionUtils.versionName();
			VersionUtils.updateURL = context.getString(R.string.server_url) + context.getString(R.string.app_download_url);
			HttpUtils.getRequest(context.getString(R.string.server_url)
					+ context.getString(R.string.app_version_url), null, null,
					HttpRequestType.ASYNCHRONOUS, onFinishedGetVersion);
		}
	}

	private OnHttpRequestListener onFinishedGetVersion = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			try {
				Log.d(SystemConstants.TAG, "response text: " + responseResult.getResponseText());
				JSONObject data = new JSONObject(responseResult.getResponseText());
				String comment = data.getString("comment");
				VersionUtils.serverVerion = data.getString("version");
				if (VersionUtils.compareVersion(VersionUtils.serverVerion,
						VersionUtils.localVersion) > 0
						&& VersionUtils.updateURL != null
						&& !VersionUtils.updateURL.equals("")) {
					// prompt update dialog
					String detectNewVersion = context.getString(R.string.detect_new_version);
					detectNewVersion = String.format(detectNewVersion,
							VersionUtils.serverVerion, comment);

					new AlertDialog.Builder(context)
							.setTitle(R.string.alert_title)
							.setMessage(detectNewVersion)
							.setPositiveButton(R.string.upgrade,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											VersionUtils.checkVersion = false;
											context.startActivity(new Intent(
													Intent.ACTION_VIEW,
													Uri.parse(VersionUtils.updateURL)));
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											VersionUtils.checkVersion = false;
										}
									}).show();
				} else {
					VersionUtils.checkVersion = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {

		}
	};
}
