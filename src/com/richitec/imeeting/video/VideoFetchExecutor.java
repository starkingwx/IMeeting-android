package com.richitec.imeeting.video;

/**
 * Executor to fetch the video from RTMP server
 * 
 * @author star
 * 
 */
public class VideoFetchExecutor extends Thread {
	private int imgWidth;
	private int imgHeight;
	private String rtmpUrl;
	private String groupId;
	private String username;

	private VideoFetchListener fetchListener;
	private boolean isCancelled;

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

	public native void readVideoFrame();

	public native void closeVideoInputStream();

	public void handleError() {

	}

	@Override
	public void run() {
		startFetchVideo();
	}

	public void cancel() {
		isCancelled = true;
	}

}
