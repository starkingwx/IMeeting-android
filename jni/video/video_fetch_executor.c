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

static AVFormatContext *inputFormatContext;
static AVCodecContext *videoCodecContext;
static AVCodec *videoCodec;
static int videoStream;
static AVFrame *videoFrame;
static AVFrame *videoPicture;
static struct SwsContext *img_convert_ctx;
static enum PixelFormat dst_pix_fmt = PIX_FMT_RGB0;

void close_video_input_stream() {

}

void Java_com_richitec_imeeting_video_VideoFetchExecutor_startFetchVideo(JNIEnv* env, jobject thiz) {

}

void Java_com_richitec_imeeting_video_VideoFetchExecutor_readVideoFrame(JNIEnv* env, jobject thiz) {

}

void Java_com_richitec_imeeting_video_VideoFetchExecutor_closeVideoInputStream(JNIEnv* env, jobject thiz) {
	close_video_input_stream();
}

