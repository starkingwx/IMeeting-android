package com.richitec.imeeting.video;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.richitec.imeeting.constants.SystemConstants;

/**
 * Manage the camera resource, take charge of uploading and fetch live video
 * 
 * @author star
 * 
 */
public class VideoManager implements Camera.PreviewCallback,
		SurfaceHolder.Callback, VideoLiveListener {
	// rotation degree
	private static final int Rotation_0 = 0;
	private static final int Rotation_90 = 90;
	private static final int Rotation_270 = 270;

	enum CameraPosition {
		FrontCamera(1), BackCamera(0);

		private int code;

		private CameraPosition(int val) {
			this.code = val;
		}

		public int code() {
			return code;
		}
	}

	private CameraPosition currentCameraPostion;

	private Camera camera;
	private SurfaceView previewSurface;
	private ViewGroup previewSurfaceParent;
	private ECVideoEncoder videoEncoder;
	private ECVideoDecoder videoDecoder;

	private boolean videoLiving;
	private Activity activity;

	private int imageRotationDegree;

	private boolean resInit;

	public VideoManager(Activity context) {
		this.activity = context;

		currentCameraPostion = CameraPosition.FrontCamera;
		previewSurface = new SurfaceView(context);
		previewSurface.getHolder().addCallback(this);
		previewSurface.getHolder().setType(
				SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		videoEncoder = new ECVideoEncoder();
		videoDecoder = new ECVideoDecoder();
		videoEncoder.addVideoLivelistener(this);
		videoLiving = false;
		imageRotationDegree = Rotation_0;
		resInit = false;
	}

	public ECVideoDecoder getVideoDecoder() {
		return videoDecoder;
	}

	public void setRtmpUrl(String rtmpUrl) {
		videoEncoder.setRtmpUrl(rtmpUrl);
		videoDecoder.setRtmpUrl(rtmpUrl);
	}

	public void setLiveName(String liveName) {
		videoEncoder.setLiveName(liveName);
	}

	public String getLiveName() {
		return videoEncoder.getLiveName();
	}

	public void setGroupId(String groupId) {
		videoEncoder.setGroupId(groupId);
		videoDecoder.setGroupId(groupId);
	}

	public void setImgWidth(int imgWidth) {
		videoEncoder.setOutImgWidth(imgWidth);
		videoDecoder.setDstImgWidth(imgWidth);
	}

	public void setImgHeight(int imgHeight) {
		videoEncoder.setOutImgHeight(imgHeight);
		videoDecoder.setDstImgHeight(imgHeight);
	}

	public void setVideoFetchListener(VideoFetchListener listener) {
		videoDecoder.setFetchListener(listener);
	}

	public void setVideoLiveListener(VideoLiveListener listener) {
		videoEncoder.addVideoLivelistener(listener);
	}

	private void releaseCamera() {
		if (camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
	}

	public synchronized boolean isVideoLiving() {
		return videoLiving;
	}

	public synchronized void setVideoLiving(boolean living) {
		this.videoLiving = living;
	}

	public void attachVideoPreview(ViewGroup parentView) {
		previewSurfaceParent = parentView;
		if (previewSurfaceParent != null) {
			previewSurface.setLayoutParams(previewSurfaceParent
					.getLayoutParams());
			previewSurfaceParent.addView(previewSurface);
		}
	}

	public void detachVideoPreview() {
		if (previewSurfaceParent != null) {
			previewSurfaceParent.removeView(previewSurface);
		}
	}

	public void hideVideoPreview() {
		if (previewSurfaceParent != null) {
			previewSurfaceParent.setVisibility(View.INVISIBLE);
		}
	}

	public void showVideoPreview() {
		if (previewSurfaceParent != null) {
			previewSurfaceParent.setVisibility(View.VISIBLE);
		}
	}

	public void initResources() {
		if (!resInit) {
			videoDecoder.setupVideoDecoder();
			resInit = true;
		}
	}

	public void releaseResources() {
		if (resInit) {
			videoDecoder.releaseVideoDecoder();
			resInit = false;
		}
	}

	/**
	 * start to live video
	 * 
	 * @throws Exception
	 */
	public void startVideoLive() throws Exception {
		setVideoLiving(true);
		startVideoCapture();

		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		if (camera != null) {
			videoEncoder.setupVideoEncoder();
		}
		// }
		// }).start();

	}

	private void startVideoCapture() throws Exception {
		camera = getCamera(currentCameraPostion);
//		if (camera != null) {
			camera.setPreviewDisplay(previewSurface.getHolder());
			camera.setPreviewCallback(this);
			// camera.startPreview();
			Log.d(SystemConstants.TAG, "start to live video");
//		} else {
//			throw new Exception("camera not found!");
//		}
	}

	public void stopVideoLive() {
		releaseCamera();
		videoEncoder.releaseVideoEncoder();
		setVideoLiving(false);
	}

	public void switchCamera() {
		releaseCamera();
		if (currentCameraPostion == CameraPosition.FrontCamera) {
			currentCameraPostion = CameraPosition.BackCamera;
		} else {
			currentCameraPostion = CameraPosition.FrontCamera;
		}
		try {
			startVideoCapture();
			camera.startPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TargetApi(9)
	private Camera getCamera(CameraPosition position) throws Exception {
		Camera camera = null;
		int sdk = android.os.Build.VERSION.SDK_INT;
		Log.d(SystemConstants.TAG, "version sdk: " + sdk);
//		try {
			if (position == CameraPosition.FrontCamera) {
				if (sdk >= 9) {
					// android version is equal to or larger than API 9
					// we can use the following code
					int cameraNum = Camera.getNumberOfCameras();
					if (cameraNum > 1) {
						CameraInfo cinfo = new CameraInfo();
						for (int i = 0; i < cameraNum; i++) {
							Camera.getCameraInfo(i, cinfo);
							if (cinfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
								camera = Camera.open(i);
								setupCamera(camera, i);
								currentCameraPostion = CameraPosition.FrontCamera;
								imageRotationDegree = Rotation_270;
								Log.d(SystemConstants.TAG,
										"open front camera ok!!!");
								break;
							}
						}
					} else {
						camera = getCamera(CameraPosition.BackCamera);
					}
				} else {
					// there is no way to get front camera below version 2.3
					// so return back camera
					camera = getCamera(CameraPosition.BackCamera);
				}
			} else {
				// get back camera
				camera = Camera.open(); // attempt to get back Camera instance
				setupCamera(camera, 0);
				currentCameraPostion = CameraPosition.BackCamera;
				imageRotationDegree = Rotation_90;
				Log.d(SystemConstants.TAG, "open back camera ok!!!");
			}
//		} catch (Exception e) {
//			Log.d(SystemConstants.TAG, "open camera failed");
//			e.printStackTrace();
//		}
		return camera;
	}

	@TargetApi(8)
	private void setupCamera(Camera camera, int cameraId) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk >= 8) {
			camera.setDisplayOrientation(90);
		}
		// else if (sdk >= 9) {
		// setCameraDisplayOrientation(cameraId, camera);
		// }
		Parameters p = camera.getParameters();
		p.setPreviewSize(320, 240);
		p.setPreviewFormat(ImageFormat.NV21);
		camera.setParameters(p);
	}

	@TargetApi(9)
	private void setCameraDisplayOrientation(int cameraId, Camera camera) {
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}

		Log.d(SystemConstants.TAG, String.format(
				"rotation: %d, facing: %d, info orientation: %d, result: %d",
				rotation, info.facing, info.orientation, result));
		camera.setDisplayOrientation(result);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// Log.d(SystemConstants.TAG, "onPreviewFrame");
		Size size = camera.getParameters().getPreviewSize();
		if (size != null) {
			int w = size.width;
			int h = size.height;

			videoEncoder.processRawFrame(data, w, h, imageRotationDegree);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.d(SystemConstants.TAG, "surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		Log.d(SystemConstants.TAG, "surfaceCreated");
		if (camera != null) {
			try {
				camera.setPreviewDisplay(previewSurface.getHolder());
				camera.setPreviewCallback(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
			camera.startPreview();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.d(SystemConstants.TAG, "surfaceDestroyed");
		if (camera != null) {
			camera.stopPreview();
		}
	}

	public void startVideoFetch(String userName) {
		videoDecoder.startFetchVideo(userName);
	}

	public void stopVideoFetch() {
		videoDecoder.stopFetchVideo();
	}

	@Override
	public void onVideoLiveDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoLiveCannotEstablish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoLiveEstablish() {
		Log.d(SystemConstants.TAG, "VideoManager - onVideoLiveEstablish");
		if (camera != null) {
			Log.d(SystemConstants.TAG, "VideoManager - startPreview");
			camera.startPreview();
		}
	}

}
