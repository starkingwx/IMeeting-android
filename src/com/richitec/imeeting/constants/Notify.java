package com.richitec.imeeting.constants;

public enum Notify {
	notice,
	notify,
	cache,
	notice_list,
	cmd,
	action;
	
	public enum Action {
		update_status,
		update_attendee_list,
		kickout,
		invited,
		conf_destoryed
	}
}
