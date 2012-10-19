/*
 * video_encoder.c
 *
 *  Created on: 2012-10-19
 *      Author: star
 */
#include <string.h>
#include <jni.h>
#include "../common.h"
#include "quicklibav.h"
#include "libswscale/swscale.h"

static char rtmp_url[200];
static char live_name[20];
static char group_id[20];
static int out_img_width = 144;
static int out_img_height = 192;

static QuickVideoOutput *qvo = NULL;
static AVFrame *raw_picture = NULL;
static AVFrame *tmp_picture = NULL;
static struct SwsContext *img_convert_ctx = NULL;
static enum PixelFormat src_pix_fmt;
static int is_video_encode_ready = 0;  // boolean flag

void Java_com_richitec_imeeting_video_ECVideoEncoder_setRtmpUrl(JNIEnv* env, jobject thiz, jstring rtmpUrl) {
	const char *str = (*env)->GetStringUTFChars(env, rtmpUrl, 0);
	memset(rtmp_url, 0, 200);
	strcpy(rtmp_url, str);
	(*env)->ReleaseStringUTFChars(env, rtmpUrl, str);
}


