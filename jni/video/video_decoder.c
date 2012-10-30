/*
 * video_decoder.c
 *
 *  Created on: 2012-10-26
 *      Author: star
 */

#include <jni.h>
#include "../common.h"
#include "libavformat/avformat.h"

void Java_com_richitec_imeeting_video_ECVideoDecoder_setupVideoDecoder(
		JNIEnv* env, jobject thiz) {
	D("setup video decoder");
	av_register_all();
	avformat_network_init();
}

void Java_com_richitec_imeeting_video_ECVideoDecoder_releaseResource(
		JNIEnv* env, jobject thiz) {
	avformat_network_deinit();
}
