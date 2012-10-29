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

static enum PixelFormat dst_pix_fmt = PIX_FMT_RGB0;

int openVideoInputStream(JNIEnv* env, jobject thiz, const char *playPath) {
	AVFormatContext *inputFormatContext;
	AVCodecContext *videoCodecContext;
	AVCodec *videoCodec;
	int videoStream;

	int err = avformat_open_input(&inputFormatContext, playPath, NULL, NULL);
	if (err < 0) {
		D("ffmpeg: unable to open input");
		return -1;
	}

	err = avformat_find_stream_info(inputFormatContext, NULL);
	if (err < 0) {
		D("ffmpeg: unable to find stream info");
		return -1;
	}

	av_dump_format(inputFormatContext, 0, playPath, 0);

	videoStream = -1;

	int index;
	for (index = 0; index < inputFormatContext->nb_streams; index++) {
		if (inputFormatContext->streams[index]->codec->codec_type
				== AVMEDIA_TYPE_VIDEO && videoStream < 0) {
			videoStream = index;
		}
	}

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

void readVideoFrame(JNIEnv* env, jobject thiz) {

}

void close_video_input_stream() {

}

void Java_com_richitec_imeeting_video_VideoFetchExecutor_startFetchVideo(
		JNIEnv* env, jobject thiz) {
	jclass thisClass = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, thisClass, "username",
			"Ljava/lang/String;");
	jstring username = (*env)->GetObjectField(env, thiz, fid);

	jmethodID mid = (*env)->GetMethodID(env, thisClass, "onVideoFetchBeginToPrepare", "(Ljava/lang/String;)V");
	if (mid != NULL) {
		D("call void method - mid is not null");
		(*env)->CallVoidMethod(env, thiz, mid, username);
	}

}

void Java_com_richitec_imeeting_video_VideoFetchExecutor_closeVideoInputStream(
		JNIEnv* env, jobject thiz) {
	close_video_input_stream();
}

