package com.richitec.imeeting.talkinggroup.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.richitec.imeeting.R;
import com.richitec.imeeting.constants.Attendee;
import com.richitec.imeeting.util.AppUtil;

public class VideoWatchListAdapter extends BaseAdapter implements
		MemberUpdateListener {
	private List<Map<String, String>> members;
	private LayoutInflater inflater;

	public VideoWatchListAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		members = new ArrayList<Map<String, String>>();

		// for (int i = 0; i < 30; i++) {
		// Map<String, String> member = new HashMap<String, String>();
		// member.put(Attendee.username.name(), String.valueOf(i));
		// members.add(member);
		// }
	}

	@Override
	public void update(List<Map<String, String>> members) {
		this.members = members;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return members.size();
	}

	@Override
	public Object getItem(int position) {
		return members.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(
					R.layout.video_watch_member_item_layout, null);
			viewHolder.memberAvatarIV = (ImageView) convertView
					.findViewById(R.id.video_watch_member_avatar);
			viewHolder.memberNameTV = (TextView) convertView
					.findViewById(R.id.video_watch_member_name);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Map<String, String> member = (Map<String, String>) getItem(position);
		String displayName = AppUtil.getDisplayNameFromAttendee(member);

		String userName = member.get(Attendee.username.name());
		Bitmap avatar = AppUtil.getAvatar(userName);
		if (avatar != null) {
			viewHolder.memberAvatarIV.setImageBitmap(avatar);
		} else {
			viewHolder.memberAvatarIV
					.setImageResource(R.drawable.default_avatar);
		}
		viewHolder.memberNameTV.setText(displayName);
		return convertView;
	}

	final class ViewHolder {
		public ImageView memberAvatarIV;
		public TextView memberNameTV;
	}
}
