package com.richitec.imeeting.talkinggroup;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.richitec.commontoolkit.activityextension.NavigationActivity;
import com.richitec.commontoolkit.customui.BarButtonItem;
import com.richitec.commontoolkit.user.UserManager;
import com.richitec.commontoolkit.utils.HttpUtils;
import com.richitec.commontoolkit.utils.HttpUtils.ResponseListener;
import com.richitec.imeeting.R;
import com.richitec.imeeting.assistant.SettingActivity;
import com.richitec.imeeting.constants.SystemConstants;
import com.richitec.imeeting.constants.TalkGroup;
import com.richitec.imeeting.contactselect.ContactSelectActivity;
import com.richitec.imeeting.talkinggroup.adapter.TalkingGroupListAdapter;

public class TalkingGroupHistoryListActivity extends NavigationActivity {
	private static final int REQ_OPEN_GROUP_TALK = 0;

	private PullToRefreshListView listView;
	private TalkingGroupListAdapter listAdapter;
	private Handler handler;
	private ProgressDialog progressDialog;

	private JSONObject selectedGroupInfo;
	private boolean hasNext;
	private int offset;

	private View footerView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler(Looper.myLooper());

		// set content view
		setContentView(R.layout.talking_group_history_list_activity_layout);

		// set title text
		setTitle(R.string.myTalkingGroup_history_list_nav_title_text);

		// init setting bar button item
		BarButtonItem _settingBarBtnItem = new BarButtonItem(this);
		// set attributes
		_settingBarBtnItem.setText(R.string.setting_nav_btn_title);
		_settingBarBtnItem.setOnClickListener(new SettingBtnOnClickListener());
		// set setting bar button item as self activity left bar button
		// item
		setLeftBarButtonItem(_settingBarBtnItem);

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(
				R.menu.talking_group_history_list_activity_layout, menu);
		return true;
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
		HttpUtils.startHttpPostRequestWithSignature(getGroupListUrl, null,
				onFinishedGetGroupList, null);
	}

	private ResponseListener onFinishedGetGroupList = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					listView.onRefreshComplete();

				}
			});
			switch (status) {
			case 200:
				try {
					JSONObject data = new JSONObject(responseText);
					JSONObject pager = data.getJSONObject("pager");
					hasNext = pager.getBoolean("hasNext");
					offset = pager.getInt("offset");

					final JSONArray groups = data.getJSONArray("list");
					Log.d(SystemConstants.TAG,
							"group list size: " + groups.length());
					handler.post(new Runnable() {

						@Override
						public void run() {
							listAdapter.setGroupList(groups);
						}
					});

				} catch (JSONException e) {
					e.printStackTrace();
				}

				break;

			default:
				break;
			}

		}
	};

	private void loadMoreData() {
		Log.d(SystemConstants.TAG, "load more group list");
		String getGroupListUrl = getString(R.string.server_url)
				+ getString(R.string.conf_list_url);
		HashMap<String, String> params = new HashMap<String, String>();
		Integer nextOffset = offset + 1;
		params.put("offset", nextOffset.toString());
		HttpUtils.startHttpPostRequestWithSignature(getGroupListUrl, params,
				onFinishedLoadMoreGroupList, null);
	}

	private ResponseListener onFinishedLoadMoreGroupList = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			switch (status) {
			case 200:
				try {
					JSONObject data = new JSONObject(responseText);
					JSONObject pager = data.getJSONObject("pager");
					hasNext = pager.getBoolean("hasNext");
					offset = pager.getInt("offset");

					final JSONArray groups = data.getJSONArray("list");
					Log.d(SystemConstants.TAG,
							"group list size: " + groups.length());
					handler.post(new Runnable() {

						@Override
						public void run() {
							listView.getRefreshableView().removeFooterView(
									footerView);
							footerView = null;
							listAdapter.appendGroupList(groups);
						}
					});

				} catch (JSONException e) {
					e.printStackTrace();
				}

				break;

			default:
				break;
			}

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
		HttpUtils.startHttpPostRequestWithSignature(
				getString(R.string.server_url)
						+ getString(R.string.join_conf_url), params,
				onFinishedJoin, null);
	}

	private ResponseListener onFinishedJoin = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			switch (status) {
			case 200:
				Log.d(SystemConstants.TAG, "join ok");
				try {
					String groupId = selectedGroupInfo
							.getString(TalkGroup.conferenceId.name());
					JSONObject data = new JSONObject(responseText);

					Intent intent = new Intent(
							TalkingGroupHistoryListActivity.this,
							TalkingGroupActivity.class);
					intent.putExtra(TalkGroup.conferenceId.name(), groupId);
					intent.putExtra(TalkGroup.owner.name(),
							data.getString(TalkGroup.owner.name()));
					startActivityForResult(intent, REQ_OPEN_GROUP_TALK);
				} catch (JSONException e1) {
					e1.printStackTrace();
					Toast.makeText(TalkingGroupHistoryListActivity.this,
							R.string.error_in_join_group, Toast.LENGTH_SHORT)
							.show();
				}
				break;
			case 403:
				Toast.makeText(TalkingGroupHistoryListActivity.this,
						R.string.join_conf_forbidden, Toast.LENGTH_SHORT)
						.show();
				break;
			case 404:
				Log.d(SystemConstants.TAG,
						"conf doesn't exist, create a new one");
				String accountName = UserManager.getInstance().getUser()
						.getName();
				String attendeesJsonString = null;
				if (selectedGroupInfo != null) {
					try {
						JSONArray attendees = selectedGroupInfo
								.getJSONArray(TalkGroup.attendees.name());
						JSONArray attendeesToInvite = new JSONArray();
						for (int i = 0; i < attendees.length(); i++) {
							String name = attendees.getString(i);
							if (!accountName.equals(name)) {
								attendeesToInvite.put(name);
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
				HttpUtils.startHttpPostRequestWithSignature(
						getString(R.string.server_url)
								+ getString(R.string.create_conf_url), params,
						onFinishedCreateGroupTalk, null);
				return;
			default:
				Toast.makeText(TalkingGroupHistoryListActivity.this,
						R.string.error_in_join_group, Toast.LENGTH_SHORT)
						.show();
				break;
			}
			selectedGroupInfo = null;
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
		}
	};

	private ResponseListener onFinishedCreateGroupTalk = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			switch (status) {
			case 201:
				// create and invite ok
				try {
					JSONObject data = new JSONObject(responseText);
					String groupId = data.getString(TalkGroup.conferenceId
							.name());
					String owner = data.getString(TalkGroup.owner.name());

					Intent intent = new Intent(
							TalkingGroupHistoryListActivity.this,
							TalkingGroupActivity.class);
					intent.putExtra(TalkGroup.conferenceId.name(), groupId);
					intent.putExtra(TalkGroup.owner.name(), owner);
					startActivityForResult(intent, REQ_OPEN_GROUP_TALK);
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(TalkingGroupHistoryListActivity.this,
							R.string.error_in_create_group, Toast.LENGTH_SHORT)
							.show();
				}

				break;

			default:
				Toast.makeText(TalkingGroupHistoryListActivity.this,
						R.string.error_in_create_group, Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	private void deleteGroupTalk(String groupId) {
		progressDialog = ProgressDialog.show(this, null,
				getString(R.string.sending_request));
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TalkGroup.conferenceId.name(), groupId);
		HttpUtils.startHttpPostRequestWithSignature(
				getString(R.string.server_url)
						+ getString(R.string.hide_group_url), params,
				onFinishedDeleteGroup, null);
	}

	private ResponseListener onFinishedDeleteGroup = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			switch (status) {
			case 200:
				handler.post(new Runnable() {

					@Override
					public void run() {
						listAdapter.removeItem(selectedGroupInfo);
					}
				});
				break;
			default:
				handler.post(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(TalkingGroupHistoryListActivity.this,
								R.string.error_in_del_group, Toast.LENGTH_SHORT)
								.show();
					}

				});
				break;
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		loadData();
	}

}
