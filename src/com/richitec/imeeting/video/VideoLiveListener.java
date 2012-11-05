package com.richitec.imeeting.video;

public interface VideoLiveListener {
	public void onVideoLiveDisconnected();
	public void onVideoLiveCannotEstablish();
	public void onVideoLiveEstablish();
}
