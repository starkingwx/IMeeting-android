package com.richitec.imeeting.talkinggroup.statusfilter;

import java.util.Map;

public class AttendeeModeStatusFilter implements IStatusFilter {

	@Override
	public Map<String, String> filterStatus(Map<String, String> newAttendee,
			Map<String, String> oldAttendee) {
		return newAttendee;
	}

}
