package com.richitec.imeeting.video;


/**
 * fetch the video from RTMP server, and decode it
 * 
 * @author star
 * 
 */
public class ECVideoDecoder {
	private String rtmpUrl;
	private String groupId;
	private int dstImgWidth;
	private int dstImgHeight;
	private VideoFetchListener fetchListener;

	private VideoFetchExecutor executor;

	public ECVideoDecoder() {
		dstImgWidth = 144;
		dstImgHeight = 192;
	}

	public native void setupVideoDecoder();

	public void releaseVideoDecoder() {
		stopFetchVideo();
		releaseResource();
	}

	private native void releaseResource();

	public void startFetchVideo(String username) {
		executor = new VideoFetchExecutor();
		executor.setImgWidth(dstImgWidth);
		executor.setImgHeight(dstImgHeight);
		executor.setRtmpUrl(rtmpUrl);
		executor.setFetchListener(fetchListener);
		executor.setGroupId(groupId);
		executor.setUsername(username);

		executor.start();
	}

	public String getCurrentVideoUserName() {
		if (executor != null) {
			return executor.getUsername();
		} else {
			return null;
		}
	}

	public void stopFetchVideo() {
		if (executor != null) {
			executor.setFetchListener(null);
			executor.cancel();
			executor = null;
		}
	}

	public void setRtmpUrl(String rtmpUrl) {
		this.rtmpUrl = rtmpUrl;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setDstImgWidth(int dstImgWidth) {
		this.dstImgWidth = dstImgWidth;
	}

	public void setDstImgHeight(int dstImgHeight) {
		this.dstImgHeight = dstImgHeight;
	}

	public void setFetchListener(VideoFetchListener fetchListener) {
		this.fetchListener = fetchListener;
	}

	static {
		System.loadLibrary("video");
	}

	
}
