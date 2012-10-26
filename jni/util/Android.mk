LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog

LOCAL_MODULE    := util
LOCAL_SRC_FILES := yuv_util.c

include $(BUILD_STATIC_LIBRARY)
