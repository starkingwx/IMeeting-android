package com.richitec.imeeting.talkinggroup;

import java.util.HashMap;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.richitec.commontoolkit.customcomponent.BarButtonItem.BarButtonItemStyle;
import com.richitec.commontoolkit.user.UserManager;
import com.richitec.commontoolkit.utils.HttpUtils;
import com.richitec.commontoolkit.utils.MyToast;
import com.richitec.commontoolkit.utils.VersionUtils;
import com.richitec.commontoolkit.utils.HttpUtils.HttpRequestType;
import com.richitec.commontoolkit.utils.HttpUtils.HttpResponseResult;
import com.richitec.commontoolkit.utils.HttpUtils.OnHttpRequestListener;
import com.richitec.commontoolkit.utils.HttpUtils.PostRequestFormat;
import com.richitec.imeeting.R;
import com.richitec.imeeting.account.AccountSettingActivity;
import com.richitec.imeeting.assistant.SettingActivity;
import com.richitec.imeeting.constants.Attendee;
import com.richitec.imeeting.constants.SystemConstants;
import com.richitec.imeeting.constants.TalkGroup;
import com.richitec.imeeting.contactselect.ContactSelectActivity;
import com.richitec.imeeting.customcomponent.IMeetingBarButtonItem;
import com.richitec.imeeting.customcomponent.IMeetingNavigationActivity;
import com.richitec.imeeting.talkinggroup.adapter.TalkingGroupListAdapter;
import com.richitec.imeeting.util.AppUpdateManager;

public class TalkingGroupHistoryListActivity extends IMeetingNavigationActivity {
	private static final int REQ_OPEN_GROUP_TALK = 0;

	private PullToRefreshListView listView;
	private TalkingGroupListAdapter listAdapter;
	private ProgressDialog progressDialog;

	private JSONObject selectedGroupInfo;
	private boolean hasNext;
	private int offset;

	private View footerView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set content view
		setContentView(R.layout.talking_group_history_list_activity_layout);

		// set title text
		setTitle(R.string.myTalkingGroup_history_list_nav_title_text);

		// init setting bar button item
		setLeftBarButtonItem(new IMeetingBarButtonItem(this,
				BarButtonItemStyle.RIGHT_GO, R.string.setting_nav_btn_title,
				new SettingBtnOnClickListener()));

		// set my group history listView adapter
		listView = (PullToRefreshListView) findViewById(R.id.myGroup_history_listView);
		listAdapter = new TalkingGroupListAdapter(this);
		listView.getRefreshableView().setAdapter(listAdapter);
		listView.getRefreshableView().setOnItemClickListener(selectedListener);
		listView.getRefreshableView().setOnItemLongClickListener(
				longPressedListener);
		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				loadData();
			}
		});

		listView.setOnLastItemVisibleListener(lastItemVisibleListener);

		// bind new talking group button on click listener
		((Button) findViewById(R.id.newTalkingGroup_btn))
				.setOnClickListener(new NewTalkingGroupBtnOnClickListener());

		listView.setRefreshing();
		loadData();

		AppUpdateManager aum = new AppUpdateManager(this);
		aum.checkVersion();
	}

	// setting button on click listener
	class SettingBtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// go to setting activity
			pushActivity(SettingActivity.class);
		}

	}

	// new talking group button
	class NewTalkingGroupBtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// go to talking group attendee select activity
			pushActivity(ContactSelectActivity.class);
		}

	}

	private OnLastItemVisibleListener lastItemVisibleListener = new OnLastItemVisibleListener() {

		@Override
		public void onLastItemVisible() {
			Log.d(SystemConstants.TAG, "last item reached");
			if (hasNext) {
				showLoadMoreItemFooter();
				loadMoreData();
			} else {
				showNoMoreItemFooter();
			}
		}
	};

	private void showNoMoreItemFooter() {
		footerView = LayoutInflater.from(this).inflate(
				R.layout.no_more_item_layout, null);
		listView.getRefreshableView().addFooterView(footerView);
	}

	private void showLoadMoreItemFooter() {
		footerView = LayoutInflater.from(this).inflate(
				R.layout.load_more_item_layout, null);
		listView.getRefreshableView().addFooterView(footerView);
	}

	private void loadData() {
		Log.d(SystemConstants.TAG, "load group list");
		String getGroupListUrl = getString(R.string.server_url)
				+ getString(R.string.conf_list_url);
		HttpUtils.postSignatureRequest(getGroupListUrl,
				PostRequestFormat.URLENCODED, null, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedGetGroupList);
	}

	private OnHttpRequestListener onFinishedGetGroupList = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			listView.onRefreshComplete();
			try {
				JSONObject data = new JSONObject(
						responseResult.getResponseText());
				JSONObject pager = data.getJSONObject("pager");
				hasNext = pager.getBoolean("hasNext");
				offset = pager.getInt("offset");

				JSONArray groups = data.getJSONArray("list");
				Log.d(SystemConstants.TAG,
						"group list size: " + groups.length());
				listAdapter.setGroupList(groups);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			listView.onRefreshComplete();

		}
	};

	private void loadMoreData() {
		Log.d(SystemConstants.TAG, "load more group list");
		String getGroupListUrl = getString(R.string.server_url)
				+ getString(R.string.conf_list_url);
		HashMap<String, String> params = new HashMap<String, String>();
		Integer nextOffset = offset + 1;
		params.put("offset", nextOffset.toString());
		HttpUtils.postSignatureRequest(getGroupListUrl,
				PostRequestFormat.URLENCODED, params, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedLoadMoreGroupList1);
	}

	private OnHttpRequestListener onFinishedLoadMoreGroupList1 = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			try {
				JSONObject data = new JSONObject(
						responseResult.getResponseText());
				JSONObject pager = data.getJSONObject("pager");
				hasNext = pager.getBoolean("hasNext");
				offset = pager.getInt("offset");

				JSONArray groups = data.getJSONArray("list");
				Log.d(SystemConstants.TAG,
						"group list size: " + groups.length());
				listView.getRefreshableView().removeFooterView(footerView);
				footerView = null;
				listAdapter.appendGroupList(groups);

			} catch (Exception e) {
				e.printStackTrace();
				listView.getRefreshableView().removeFooterView(footerView);
				footerView = null;
			}
		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			listView.getRefreshableView().removeFooterView(footerView);
			footerView = null;

		}
	};

	private OnItemClickListener selectedListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position <= listAdapter.getCount() && position > 0) {

				selectedGroupInfo = (JSONObject) listAdapter
						.getItem(position - 1);
				try {
					String groupId = selectedGroupInfo
							.getString(TalkGroup.conferenceId.name());
					Log.d(SystemConstants.TAG, "current groupid: " + groupId);
					joinGroupTalk(groupId);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

	};

	private OnItemLongClickListener longPressedListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			if (position <= listAdapter.getCount() && position > 0) {
				selectedGroupInfo = (JSONObject) listAdapter
						.getItem(position - 1);
				new AlertDialog.Builder(TalkingGroupHistoryListActivity.this)
						.setTitle(R.string.select_operation)
						.setItems(R.array.group_long_press_menu,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										switch (which) {
										case 0:
											try {
												String groupId = selectedGroupInfo
														.getString(TalkGroup.conferenceId
																.name());
												deleteGroupTalk(groupId);
											} catch (JSONException e) {
												e.printStackTrace();
											}
											break;

										default:
											break;
										}
									}
								}).show();
			}
			return true;
		}
	};

	private void joinGroupTalk(String groupId) {
		progressDialog = ProgressDialog.show(this, null,
				getString(R.string.sending_request));
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TalkGroup.conferenceId.name(), groupId);
		HttpUtils.postSignatureRequest(getString(R.string.server_url)
				+ getString(R.string.join_conf_url),
				PostRequestFormat.URLENCODED, params, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedJoin);
	}

	private OnHttpRequestListener onFinishedJoin = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			dismissProgressDlg();
			Log.d(SystemConstants.TAG, "join ok");
			try {
				String groupId = selectedGroupInfo
						.getString(TalkGroup.conferenceId.name());
				JSONObject data = new JSONObject(
						responseResult.getResponseText());

				Intent intent = new Intent(
						TalkingGroupHistoryListActivity.this,
						TalkingGroupActivity.class);
				intent.putExtra(TalkGroup.conferenceId.name(), groupId);
				intent.putExtra(TalkGroup.owner.name(),
						data.getString(TalkGroup.owner.name()));
				startActivityForResult(intent, REQ_OPEN_GROUP_TALK);
			} catch (Exception e1) {
				e1.printStackTrace();
				MyToast.show(TalkingGroupHistoryListActivity.this,
						R.string.error_in_join_group, Toast.LENGTH_SHORT);
			}
		}

		@Override
		public void onForbidden(HttpResponseResult responseResult) {
			dismissProgressDlg();
			MyToast.show(TalkingGroupHistoryListActivity.this,
					R.string.join_conf_forbidden, Toast.LENGTH_SHORT);
		}

		@Override
		public void onNotFound(HttpResponseResult responseResult) {
			Log.d(SystemConstants.TAG, "conf doesn't exist, create a new one");
			String accountName = UserManager.getInstance().getUser().getName();
			String attendeesJsonString = null;
			if (selectedGroupInfo != null) {
				try {
					JSONArray attendees = selectedGroupInfo
							.getJSONArray(TalkGroup.attendees.name());
					JSONArray attendeesToInvite = new JSONArray();
					for (int i = 0; i < attendees.length(); i++) {
						JSONObject attendee = attendees.getJSONObject(i);
						String userName = attendee.getString(Attendee.username
								.name());
						if (!accountName.equals(userName)) {
							attendeesToInvite.put(userName);
						}
					}
					attendeesJsonString = attendeesToInvite.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				selectedGroupInfo = null;
			}
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(TalkGroup.attendees.name(), attendeesJsonString);
			HttpUtils.postSignatureRequest(getString(R.string.server_url)
					+ getString(R.string.create_conf_url),
					PostRequestFormat.URLENCODED, params, null,
					HttpRequestType.ASYNCHRONOUS, onFinishedCreateGroupTalk);
		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			dismissProgressDlg();
			MyToast.show(TalkingGroupHistoryListActivity.this,
					R.string.error_in_join_group, Toast.LENGTH_SHORT);
		}

	};

	private OnHttpRequestListener onFinishedCreateGroupTalk = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			dismissProgressDlg();
			try {
				JSONObject data = new JSONObject(
						responseResult.getResponseText());
				String groupId = data.getString(TalkGroup.conferenceId.name());
				String owner = data.getString(TalkGroup.owner.name());

				Intent intent = new Intent(
						TalkingGroupHistoryListActivity.this,
						TalkingGroupActivity.class);
				intent.putExtra(TalkGroup.conferenceId.name(), groupId);
				intent.putExtra(TalkGroup.owner.name(), owner);
				startActivityForResult(intent, REQ_OPEN_GROUP_TALK);
			} catch (Exception e) {
				e.printStackTrace();
				MyToast.show(TalkingGroupHistoryListActivity.this,
						R.string.error_in_create_group, Toast.LENGTH_SHORT);
			}
		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			dismissProgressDlg();
			switch (responseResult.getStatusCode()) {
			case 402:
				MyToast.show(TalkingGroupHistoryListActivity.this,
						R.string.payment_required, Toast.LENGTH_SHORT);
				break;

			default:
				MyToast.show(TalkingGroupHistoryListActivity.this,
						R.string.error_in_create_group, Toast.LENGTH_SHORT);
				break;
			}

		}
	};

	private void deleteGroupTalk(String groupId) {
		progressDialog = ProgressDialog.show(this, null,
				getString(R.string.sending_request));
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TalkGroup.conferenceId.name(), groupId);
		HttpUtils.postSignatureRequest(getString(R.string.server_url)
				+ getString(R.string.hide_group_url),
				PostRequestFormat.URLENCODED, params, null,
				HttpRequestType.ASYNCHRONOUS, onFinishedDeleteGroup1);
	}

	private OnHttpRequestListener onFinishedDeleteGroup1 = new OnHttpRequestListener() {

		@Override
		public void onFinished(HttpResponseResult responseResult) {
			dismissProgressDlg();
			listAdapter.removeItem(selectedGroupInfo);
		}

		@Override
		public void onFailed(HttpResponseResult responseResult) {
			dismissProgressDlg();
			MyToast.show(TalkingGroupHistoryListActivity.this,
					R.string.error_in_del_group, Toast.LENGTH_SHORT);
		}
	};

	private void dismissProgressDlg() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		loadData();
	}

	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.alert_title)
				.setMessage(R.string.exit_app)
				.setNegativeButton(R.string.cancel, null)
				.setPositiveButton(R.string.exit,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								System.exit(0);
							}
						}).show();
	}
}
