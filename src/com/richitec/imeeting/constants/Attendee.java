package com.richitec.imeeting.constants;

public enum Attendee {
	attendee, username, nickname, online_status, video_status, telephone_status;

	public enum OnlineStatus {
		online, offline
	}

	public enum PhoneStatus {
		CallWait, Established, Failed, Terminated, TermWait
	}
	
	public enum VideoStatus {
		on, off
	}
}
