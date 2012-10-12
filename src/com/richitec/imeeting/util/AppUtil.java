package com.richitec.imeeting.util;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.richitec.commontoolkit.addressbook.AddressBookManager;
import com.richitec.imeeting.constants.Attendee;

public class AppUtil {
	public static String getDisplayNameFromAttendee(JSONObject attendee) {
		String displayName = "";
		if (attendee != null) {
			try {
				String userName = attendee.getString(Attendee.username.name());
				String nickname = attendee.getString(Attendee.nickname.name());
				
				List<String> contactDisplayNames = AddressBookManager.getInstance().getContactsDisplayNamesByPhone(userName);
				if (contactDisplayNames.size() > 0) {
					displayName = contactDisplayNames.get(0);
				}
				if (userName.equals(displayName) && nickname != null && !nickname.equals("")) {
					displayName = nickname;
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
			String userName = attendee.get(Attendee.username.name());
			String nickname = attendee.get(Attendee.nickname.name());
			List<String> contactDisplayNames = AddressBookManager.getInstance().getContactsDisplayNamesByPhone(userName);
			if (contactDisplayNames.size() > 0) {
				displayName = contactDisplayNames.get(0);
			}
			if (userName.equals(displayName) && nickname != null && !nickname.equals("")) {
				displayName = nickname;
			}
		}
		return displayName;
	}
	
	public static String getDisplayName(String phoneNumber) {
		String displayName = phoneNumber;
		List<String> contactDisplayNames = AddressBookManager.getInstance().getContactsDisplayNamesByPhone(phoneNumber);
		if (contactDisplayNames.size() > 0) {
			displayName = contactDisplayNames.get(0);
		}
		return displayName;
	}
}
