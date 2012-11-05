package com.richitec.imeeting.video;

import java.util.ArrayList;
import java.util.List;

import com.richitec.imeeting.constants.SystemConstants;

import android.util.Log;

/**
 * encode video frame and upload to RTMP server
 * 
 * combined with jni file - video_encoder.c
 * 
 * @author star
 * 
 */
public class ECVideoEncoder implements VideoLiveListener {
	private List<VideoLiveListener> listeners;

	public ECVideoEncoder() {
		listeners = new ArrayList<VideoLiveListener>();
	}

	public void addVideoLivelistener(VideoLiveListener videoLivelistener) {
		listeners.add(videoLivelistener);
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
	 * 
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
	 * @param rotateDegree
	 *            - rotation degree
	 */
	public native void processRawFrame(byte[] buffer, int width, int height,
			int rotateDegree);

	static {
		System.loadLibrary("video");
	}

	@Override
	public void onVideoLiveDisconnected() {
		for (VideoLiveListener lis : listeners) {
			lis.onVideoLiveDisconnected();
		}
	}

	@Override
	public void onVideoLiveCannotEstablish() {
		for (VideoLiveListener lis : listeners) {
			lis.onVideoLiveCannotEstablish();
		}

	}

	@Override
	public void onVideoLiveEstablish() {
		for (VideoLiveListener lis : listeners) {
			Log.d(SystemConstants.TAG, "send msg to listener: " + lis.toString());
			lis.onVideoLiveEstablish();
		}
	}
}
