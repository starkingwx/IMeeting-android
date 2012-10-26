/*
 * yuv_util.h
 *
 *  Created on: 2012-10-22
 *      Author: star
 */

#ifndef YUV_UTIL_H_
#define YUV_UTIL_H_

#include <jni.h>

typedef enum {
	ROTATE_0 = 0,
	ROTATE_90 = 90,
	ROTATE_270 = 270
} ROTATE_DEGREE;

/*
 * rotate YUV420SP(NV21) image 90 degree clock wise
 * in param:
 * 	yuvInbuffer - input YUV420SP image buffer
 * 	inWidth - input image width
 * 	inHeight - input image height
 * out param:
 *  outWidth - out image width
 *  outHeight - out image height
 * return:
 *  rotated YUV420SP buffer
 */
unsigned char* rotateYUV420SPDegree90(jbyte *yuvInBuffer, jint inWidth, jint inHeight, jint *outWidth, jint *outHeight);

/*
 * rotate YUV420SP(NV21) image 270 degree clock wise
 * in param:
 * 	yuvInbuffer - input YUV420SP image buffer
 * 	inWidth - input image width
 * 	inHeight - input image height
 * out param:
 *  outWidth - out image width
 *  outHeight - out image height
 * return:
 *  rotated YUV420SP buffer
 */
unsigned char* rotateYUV420SPDegree270(jbyte *yuvInBuffer, jint inWidth, jint inHeight, jint *outWidth, jint *outHeight);

/*
 * rotate YUV420SP(NV21) image according to the degree
 * in param:
 * 	yuvInbuffer - input YUV420SP image buffer
 * 	inWidth - input image width
 * 	inHeight - input image height
 * 	degree - rotation degree
 * out param:
 *  outWidth - out image width
 *  outHeight - out image height
 * return:
 *  rotated YUV420SP buffer
 */
unsigned char *rotateYUV420SP(jbyte *yuvInBuffer, jint inWidth, jint inHeight, ROTATE_DEGREE degree, jint *outWidth, jint *outHeight);
#endif /* YUV_UTIL_H_ */
