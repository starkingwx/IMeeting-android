/*
 * util.h
 *
 *  Created on: 2012-10-22
 *      Author: star
 */

#ifndef UTIL_H_
#define UTIL_H_

#include <jni.h>

/*
 * rotate YUV420SP(NV21) image 90 degree
 */
unsigned char* rotateYUV420SPDegree90(jbyte *yuvInBuffer, jint inWidth, jint inHeight, jint *outWidth, jint *outHeight);

#endif /* UTIL_H_ */
