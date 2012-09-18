package com.richitec.imeeting.util;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.richitec.imeeting.constants.Attendee;

public class AppUtil {
	public static String getDisplayNameFromAttendee(JSONObject attendee) {
		String displayName = "";
		if (attendee != null) {
			try {
				displayName = attendee.getString(Attendee.nickname.name());
				if (displayName.equals("")) {
					displayName = attendee.getString(Attendee.username.name());
				}
			} catch (JSONException e) {
				e.printStackTrace();
				try {
					displayName = attendee.getString(Attendee.username.name());
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
			
		}
		
		return displayName;
	}
	
	public static String getDisplayNameFromAttendee(Map<String, String> attendee) {
		String displayName = "";
		if (attendee != null) {
			displayName = attendee.get(Attendee.nickname.name());
			if (displayName == null || displayName.equals("")) {
				displayName = attendee.get(Attendee.username.name());
			}
		}
		return displayName;
	}
}
