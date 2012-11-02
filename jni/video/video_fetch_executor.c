/*
 * video_fetch_executor.c
 *
 *  Created on: 2012-10-26
 *      Author: star
 */

#include <jni.h>
#include "../common.h"
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include <android/bitmap.h>

static enum PixelFormat dst_pix_fmt = PIX_FMT_RGB24;

int openVideoInputStream(JNIEnv* env, jobject thiz, const char *playPath) {
	D("open video input stream");
	AVFormatContext *inputFormatContext = NULL;
	AVCodecContext *videoCodecContext = NULL;
	AVCodec *videoCodec = NULL;
	int videoStream = -1;

	int err = avformat_open_input(&inputFormatContext, playPath, NULL, NULL);
	if (err < 0) {
		D("ffmpeg: unable to open input");
		return -1;
	}
	D("avformat_open_input ok");

	err = avformat_find_stream_info(inputFormatContext, NULL);
	if (err < 0) {
		D("ffmpeg: unable to find stream info");
		return -1;
	}
	D("avformat_find_stream_info ok");

	av_dump_format(inputFormatContext, 0, playPath, 0);
	D("av_dump_format ok");

	videoStream = -1;

	int index;
	for (index = 0; index < inputFormatContext->nb_streams; index++) {
		if (inputFormatContext->streams[index]->codec->codec_type
				== AVMEDIA_TYPE_VIDEO && videoStream < 0) {
			videoStream = index;
		}
	}
	D("video stream: %d", videoStream);

	if (videoStream == -1) {
		D("ffmpeg: unable to find video stream");
		return -1;
	}

	videoCodecContext = inputFormatContext->streams[videoStream]->codec;
	if (!videoCodecContext) {
		D("ffmpeg: no video codec context found");
		return -1;
	}
	// find the decoder for video stream
	videoCodec = avcodec_find_decoder(videoCodecContext->codec_id);
	if (!videoCodec) {
		D(
				"ffmpeg: unable to find the decoder(%d) for video stream", videoCodecContext->codec_id);
		return -1;
	}

	// open video stream codec
	if (avcodec_open2(videoCodecContext, videoCodec, NULL) < 0) {
		D("ffmpeg: unable to open video codec");
		return -1;
	}

	// save the pointers' value in object members
	set_int_field(env, thiz, "pInputFormatContext", inputFormatContext);
	set_int_field(env, thiz, "pVideoCodecContext", videoCodecContext);
	set_int_field(env, thiz, "videoStream", videoStream);

	return 0;
}

void fillRGBIntBuffer(AVFrame *frame, int width, int height, int *outRGBData) {
	uint8_t *frameLine;
	uint8_t r, g, b;

	int yy;
	for (yy = 0; yy < height; yy++) {
		frameLine = (uint8_t *) frame->data[0] + (yy * frame->linesize[0]);

		int xx;
		for (xx = 0; xx < width; xx++) {
			int in_offset = xx * 3;

			r = frameLine[in_offset];
			g = frameLine[in_offset + 1];
			b = frameLine[in_offset + 2];

			outRGBData[yy * width + xx] = 0xff000000 | r << 16 | g << 8 | b;
		}
	}
}

void readVideoFrame(JNIEnv* env, jobject thiz) {
	D("### read video frame start");
	jclass thisClass = (*env)->GetObjectClass(env, thiz);

	AVFormatContext *inputFormatContext = get_int_field(env, thiz,
			"pInputFormatContext");
	AVCodecContext *videoCodecContext = get_int_field(env, thiz,
			"pVideoCodecContext");
	jint videoStream = get_int_field(env, thiz, "videoStream");
	jint imgWidth = get_int_field(env, thiz, "imgWidth");
	jint imgHeight = get_int_field(env, thiz, "imgHeight");

	if (!inputFormatContext) {
		return;
	}

	AVPacket packet;

	// allocate a video frame to store the decoded image
	AVFrame *videoFrame = avcodec_alloc_frame();
	if (!videoFrame) {
		D("cannot allocate video frame");
		call_void_method(env, thiz, "handleError");
		return;
	}

	AVFrame *videoPicture = alloc_picture(dst_pix_fmt, imgWidth, imgHeight);
	if (!videoPicture) {
		D("failed to alloc video picture");
		if (videoFrame) {
			av_free(videoFrame);
		}
		call_void_method(env, thiz, "handleError");
		return;
	}

	int gotPicture;
	struct SwsContext *img_convert_ctx = NULL;
	jsize pic_data_len = imgWidth * imgHeight;
	int *rgb_data = (int*) malloc(pic_data_len * sizeof(int));
	jintArray pic_data = (*env)->NewIntArray(env, pic_data_len);

	while (av_read_frame(inputFormatContext, &packet) >= 0) {
		jfieldID cancelFid = (*env)->GetFieldID(env, thisClass, "cancel", "Z");
		jboolean cancel = (*env)->GetBooleanField(env, thiz, cancelFid);
		if (cancel) {
			D("video fetch executor is cancelled");
			break;
		}
		D("read video frame");

		// check if the packet is from video stream
		if (packet.stream_index == videoStream) {
			// decode video frame
			avcodec_decode_video2(videoCodecContext, videoFrame, &gotPicture,
					&packet);
			if (gotPicture) {
				D("got video frame");

				img_convert_ctx = sws_getCachedContext(img_convert_ctx,
						videoCodecContext->width, videoCodecContext->height,
						videoCodecContext->pix_fmt, imgWidth, imgHeight,
						dst_pix_fmt, SWS_BILINEAR, NULL, NULL, NULL);

				// convert YUV420 to RGB
				sws_scale(img_convert_ctx, (const uint8_t*)videoFrame->data,
						videoFrame->linesize, 0, videoCodecContext->height,
						videoPicture->data, videoPicture->linesize);

				fillRGBIntBuffer(videoPicture, imgWidth, imgHeight, rgb_data);

				jmethodID processVideoPictureMid = (*env)->GetMethodID(env,
						thisClass, "processVideoPicture", "([III)V");
				if (processVideoPictureMid != NULL) {
					(*env)->SetIntArrayRegion(env, pic_data, 0, pic_data_len,
							rgb_data);

					(*env)->CallVoidMethod(env, thiz, processVideoPictureMid,
							pic_data, imgWidth, imgHeight);
				}
			}
		}

	}

	if (rgb_data) {
		free(rgb_data);
		rgb_data = NULL;
	}

	if (videoFrame) {
		av_free(videoFrame);
	}
	if (videoPicture) {
		if (videoPicture->data[0]) {
			av_free(videoPicture->data[0]);
		}
		av_free(videoPicture);
	}
}

void close_video_input_stream(JNIEnv* env, jobject thiz) {
	AVFormatContext *inputFormatContext = get_int_field(env, thiz,
			"pInputFormatContext");
	AVCodecContext *videoCodecContext = get_int_field(env, thiz,
			"pVideoCodecContext");

	if (videoCodecContext) {
		avcodec_close(videoCodecContext);
		videoCodecContext = NULL;
		set_int_field(env, thiz, "pVideoCodecContext", videoCodecContext);
	}
	if (inputFormatContext) {
		avformat_close_input(&inputFormatContext);
		inputFormatContext = NULL;
		set_int_field(env, thiz, "pInputFormatContext", inputFormatContext);
	}
	D("video input stream closed");
}

void Java_com_richitec_imeeting_video_VideoFetchExecutor_startFetchVideo(
		JNIEnv* env, jobject thiz) {
	jclass thisClass = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, thisClass, "username",
			"Ljava/lang/String;");
	jstring username = (*env)->GetObjectField(env, thiz, fid);

	jmethodID onBeginToPreparedMid = (*env)->GetMethodID(env, thisClass,
			"onVideoFetchBeginToPrepare", "(Ljava/lang/String;)V");
	if (onBeginToPreparedMid != NULL) {
		(*env)->CallVoidMethod(env, thiz, onBeginToPreparedMid, username);
	}

	jstring accountName = get_string_field(env, thiz, "accountName");
	jstring groupId = get_string_field(env, thiz, "groupId");
	jstring rtmpUrl = get_string_field(env, thiz, "rtmpUrl");
	char playPath[300];
	memset(playPath, 0, sizeof playPath);

	const char *user_name = (*env)->GetStringUTFChars(env, username, 0);
	const char *account_name = (*env)->GetStringUTFChars(env, accountName, 0);
	const char *group_id = (*env)->GetStringUTFChars(env, groupId, 0);
	const char *rtmp_url = (*env)->GetStringUTFChars(env, rtmpUrl, 0);

	sprintf(playPath, "%s/%s/%s live=1 conn=S:%s", rtmp_url, group_id,
			user_name, account_name);
	D("video play path: %s", playPath);

	(*env)->ReleaseStringUTFChars(env, username, user_name);
	(*env)->ReleaseStringUTFChars(env, accountName, account_name);
	(*env)->ReleaseStringUTFChars(env, groupId, group_id);
	(*env)->ReleaseStringUTFChars(env, rtmpUrl, rtmp_url);

	int ret = openVideoInputStream(env, thiz, playPath);
	if (ret < 0) {
		D("video input stream open failed");
		call_void_method(env, thiz, "handleError");
		return;
	}

	jmethodID onVideoFetchPreparedMid = (*env)->GetMethodID(env, thisClass,
			"onVideoFetchPrepared", "()V");
	if (onVideoFetchPreparedMid != NULL) {
		(*env)->CallVoidMethod(env, thiz, onVideoFetchPreparedMid);
	}

	readVideoFrame(env, thiz);

	jmethodID onFetchEndMid = (*env)->GetMethodID(env, thisClass, "onFetchEnd",
			"()V");
	if (onFetchEndMid != NULL) {
		(*env)->CallVoidMethod(env, thiz, onFetchEndMid);
	}

	close_video_input_stream(env, thiz);
}

void Java_com_richitec_imeeting_video_VideoFetchExecutor_closeVideoInputStream(
		JNIEnv* env, jobject thiz) {
	close_video_input_stream(env, thiz);
}

