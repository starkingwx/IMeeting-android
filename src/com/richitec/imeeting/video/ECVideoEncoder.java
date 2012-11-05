package com.richitec.imeeting.video;

/**
 * encode video frame and upload to RTMP server
 * 
 * combined with jni file - video_encoder.c
 * 
 * @author star
 * 
 */
public class ECVideoEncoder implements VideoLiveListener {
	private VideoLiveListener videoLivelistener;
	
	public void setVideoLivelistener(VideoLiveListener videoLivelistener) {
		this.videoLivelistener = videoLivelistener;
	}

	/**
	 * set the RTMP server url
	 * 
	 * @param rtmpUrl
	 */
	public native void setRtmpUrl(String rtmpUrl);

	/**
	 * set the name to live video
	 * 
	 * @param name
	 */
	public native void setLiveName(String name);
	
	/**
	 * get the name of living video
	 * @return
	 */
	public native String getLiveName();

	/**
	 * set group id for living video
	 * 
	 * @param groupId
	 */
	public native void setGroupId(String groupId);

	/**
	 * set the target image width to encode
	 * 
	 * @param outImgWidth
	 */
	public native void setOutImgWidth(int outImgWidth);

	/**
	 * set the target image height to encode
	 * 
	 * @param outImgHeight
	 */
	public native void setOutImgHeight(int outImgHeight);

	/**
	 * initial video encoder
	 */
	public native void setupVideoEncoder();

	/**
	 * release video encoder and related resources
	 */
	public native void releaseVideoEncoder();

	/**
	 * process raw video frame and upload to RTMP server
	 * 
	 * @param buffer
	 *            - raw video frame buffer
	 * @param width
	 *            - frame width
	 * @param height
	 *            - frame height
	 * @param rotateDegree - rotation degree
	 */
	public native void processRawFrame(byte[] buffer, int width, int height, int rotateDegree);

	static {
		System.loadLibrary("video");
	}

	@Override
	public void onVideoLiveDisconnected() {
		if (videoLivelistener != null) {
			videoLivelistener.onVideoLiveDisconnected();
		}
	}

	@Override
	public void onVideoLiveCannotEstablish() {
		if (videoLivelistener != null) {
			videoLivelistener.onVideoLiveCannotEstablish();
		}
		
	}
}
