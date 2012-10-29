package com.richitec.imeeting.video;

import android.graphics.Bitmap;

/**
 * Executor to fetch the video from RTMP server
 * 
 * @author star
 * 
 */
public class VideoFetchExecutor extends Thread implements VideoFetchListener {
	private int imgWidth;
	private int imgHeight;
	private String rtmpUrl;
	private String groupId;
	private String username;

	private VideoFetchListener fetchListener;
	private boolean isCancelled;

	// native members
	private int pInputFormatContext;
	private int pVideoCodecContext;
	private int videoStream;
	private int pVideoFrame;
	private int pVideoPicture;
	private int pImgConvertCtx;

	public VideoFetchExecutor() {
		isCancelled = false;
	}

	public void setFetchListener(VideoFetchListener fetchListener) {
		this.fetchListener = fetchListener;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setImgWidth(int imgWidth) {
		this.imgWidth = imgWidth;
	}

	public void setImgHeight(int imgHeight) {
		this.imgHeight = imgHeight;
	}

	public void setRtmpUrl(String rtmpUrl) {
		this.rtmpUrl = rtmpUrl;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public native void startFetchVideo();

	public native void closeVideoInputStream();

	public void handleError() {
		closeVideoInputStream();
		onFetchEnd();
	}

	@Override
	public void run() {
		startFetchVideo();
	}

	public void cancel() {
		isCancelled = true;
	}

	@Override
	public void onFetchNewImage(Bitmap image) {
		if (fetchListener != null) {
			fetchListener.onFetchNewImage(image);
		}
	}

	@Override
	public void onFetchFailed() {
		if (fetchListener != null) {
			fetchListener.onFetchFailed();
		}
	}

	@Override
	public void onVideoFetchBeginToPrepare(String username) {
		if (fetchListener != null) {
			fetchListener.onVideoFetchBeginToPrepare(username);
		}
	}

	@Override
	public void onVideoFetchPrepared() {
		if (fetchListener != null) {
			fetchListener.onVideoFetchPrepared();
		}
	}

	@Override
	public void onFetchEnd() {
		if (fetchListener != null) {
			fetchListener.onFetchEnd();
		}
	}

	static {
		System.loadLibrary("video");
	}
}
