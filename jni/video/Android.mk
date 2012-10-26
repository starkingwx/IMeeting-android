LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog -lz

LOCAL_MODULE    := video
LOCAL_SRC_FILES := quicklibav.c video_encoder.c video_decoder.c

LOCAL_STATIC_LIBRARIES := avformat avdevice swscale avcodec avutil rtmp ssl crypto x264 util
include $(BUILD_SHARED_LIBRARY)
