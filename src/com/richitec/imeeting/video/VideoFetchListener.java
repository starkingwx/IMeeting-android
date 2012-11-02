package com.richitec.imeeting.video;

import android.graphics.Bitmap;

public interface VideoFetchListener {
	public void onFetchNewImage(Bitmap image);
	public void onFetchFailed();
	public void onVideoFetchBeginToPrepare(String username);
	public void onVideoFetchPrepared();
	public void onFetchEnd();
}
