package com.richitec.imeeting.talkinggroup.statusfilter;

import java.util.Map;

import android.util.Log;

import com.richitec.imeeting.constants.Attendee;
import com.richitec.imeeting.constants.SystemConstants;

public class OwnerModeStatusFilter implements IStatusFilter {

	@Override
	public Map<String, String> filterStatus(Map<String, String> newAttendee,
			Map<String, String> oldAttendee) {
		Log.d(SystemConstants.TAG, "filter status");
		Map<String, String> filtered = newAttendee;
		String newPhoneStatus = newAttendee.get(Attendee.telephone_status
				.name());
		String oldPhoneStatus = oldAttendee.get(Attendee.telephone_status
				.name());
		Log.d(SystemConstants.TAG, "new attendee: " + newAttendee.toString()
				+ " old attendee: " + oldAttendee);
		if (newPhoneStatus != null && oldPhoneStatus != null) {
			String checkedPhoneStatus = checkNewPhoneStatus(newPhoneStatus,
					oldPhoneStatus);
			filtered.put(Attendee.telephone_status.name(), checkedPhoneStatus);
		}
		Log.d(SystemConstants.TAG, "filtered attendee: " + filtered.toString());
		return filtered;
	}

	private String checkNewPhoneStatus(String newPhoneStatus,
			String oldPhoneStatus) {
		String checkedStatus = oldPhoneStatus;
		if (Attendee.PhoneStatus.Terminated.name().equals(oldPhoneStatus)) {
			if (Attendee.PhoneStatus.CallWait.name().equals(newPhoneStatus)
					|| Attendee.PhoneStatus.Established.name().equals(
							newPhoneStatus)
					|| Attendee.PhoneStatus.Terminated.name().equals(
							newPhoneStatus)) {
				checkedStatus = newPhoneStatus;
			} else {
				alert(newPhoneStatus, oldPhoneStatus);
			}
		} else if (Attendee.PhoneStatus.CallWait.name().equals(oldPhoneStatus)) {
			if (Attendee.PhoneStatus.Established.name().equals(newPhoneStatus)
					|| Attendee.PhoneStatus.Terminated.name().equals(
							newPhoneStatus)
					|| Attendee.PhoneStatus.Failed.name()
							.equals(newPhoneStatus)
					|| Attendee.PhoneStatus.CallWait.name().equals(
							newPhoneStatus)) {
				checkedStatus = newPhoneStatus;
			} else {
				alert(newPhoneStatus, oldPhoneStatus);
			}
		} else if (Attendee.PhoneStatus.Established.name().equals(
				oldPhoneStatus)) {
			if (Attendee.PhoneStatus.Terminated.name().equals(newPhoneStatus)
					|| Attendee.PhoneStatus.Established.name().equals(
							newPhoneStatus)) {
				checkedStatus = newPhoneStatus;
			} else {
				alert(newPhoneStatus, oldPhoneStatus);
			}
		} else if (Attendee.PhoneStatus.Failed.name().equals(oldPhoneStatus)) {
			if (Attendee.PhoneStatus.CallWait.name().equals(newPhoneStatus)
					|| Attendee.PhoneStatus.Established.name().equals(
							newPhoneStatus)
					|| Attendee.PhoneStatus.Failed.name()
							.equals(newPhoneStatus)) {
				checkedStatus = newPhoneStatus;
			} else {
				alert(newPhoneStatus, oldPhoneStatus);
			}
		}

		return checkedStatus;
	}

	private void alert(String newPhoneStatus, String oldPhoneStatus) {
		Log.d(SystemConstants.TAG, "invalid status transformation from "
				+ oldPhoneStatus + " to " + newPhoneStatus);
	}
}
