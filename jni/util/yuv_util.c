/*
 * util.c
 *
 *  Created on: 2012-10-22
 *      Author: star
 */

#include "yuv_util.h"
#include "../common.h"

unsigned char* rotateYUV420SPDegree90(jbyte *yuvInBuffer, jint inWidth,
		jint inHeight, jint* outWidth, jint* outHeight) {
//	D("rotate 90 degree");
	jint size = inWidth * inHeight;
	jint offset = size;
	*outWidth = inHeight;
	*outHeight = inWidth;

	int ow = (*outWidth);
	int oh = (*outHeight);

//	D(
//			"rotate: out width: %d, out height: %d, inWidth: %d, inHeight: %d", ow, oh, inWidth, inHeight);

	unsigned char *outYuv = (unsigned char *) malloc(
			sizeof(unsigned char) * (3 * size) / 2);

	if (!outYuv) {
		D("rotateYUV420SPDegree90 - out YUV buffer alloc failed");
		return NULL;
	}
	// rotate Y (luma)
	int i = 0, j = 0;
	for (i = 0; i < oh; i++) {
		for (j = 0; j < ow; j++) {
//			D("rotating Y - oY[%d, %d] = iY[%d, %d]", i, j, inHeight - 1 - j, i);
			outYuv[i * ow + j] = yuvInBuffer[(inHeight - 1 - j) * inWidth + i];
		}
	}
//	D("rotate Y end - i: %d, j: %d", i, j);

// rotate C (chrominance)
	jbyte *outC = outYuv + offset;
	jbyte *inC = yuvInBuffer + offset;

	jint inCWidth = inWidth;
	jint inCHeight = inHeight / 2;
	jint outCWidth = ow;
	jint outCHeight = oh / 2;

//	D(
//			"rotate C: out C width: %d, out C height: %d, inCWidth: %d, inCHeight: %d", outCWidth, outCHeight, inCWidth, inCHeight);
	int count = 0;
	for (i = 0; i < outCHeight; i++) {
		for (j = 0; j < outCWidth; j += 2) {
			int inI = inCHeight - 1 - (j / 2);
			int inJ = 2 * i;
//			D("loop - i: %d, j: %d", i, j);
			outC[i * outCWidth + j] = inC[inI * inCWidth + inJ]; // rotate U component
			outC[i * outCWidth + j + 1] = inC[inI * inCWidth + inJ + 1]; // rotate V component
		}
	}
//	D("rotate C end - i: %d, j: %d", i, j);
	return outYuv;
}

unsigned char* rotateYUV420SPDegree270(jbyte *yuvInBuffer, jint inWidth,
		jint inHeight, jint* outWidth, jint* outHeight) {
//	D("rotate 270 degree");
	int size = inWidth * inHeight;
	int offset = size;
	*outWidth = inHeight;
	*outHeight = inWidth;

	int ow = (*outWidth);
	int oh = (*outHeight);

//	printf("rotate: out width: %d, out height: %d, inWidth: %d, inHeight: %d\n",
//			ow, oh, inWidth, inHeight);

	unsigned char *outYuv = (unsigned char *) malloc(
			sizeof(unsigned char) * ((3 * size) / 2));

	if (!outYuv) {
		printf("rotateYUV420SPDegree270 - out YUV buffer alloc failed");
		return 0;
	}
	// rotate Y (luma)
	int i = 0, j = 0;
	for (i = 0; i < oh; i++) {
		for (j = 0; j < ow; j++) {
//			D("rotating Y - oY[%d, %d] = iY[%d, %d]", i, j, inHeight - 1 - j, i);
			outYuv[i * ow + j] = yuvInBuffer[j * inWidth + inWidth - 1 - i];
		}
	}
//	printf("rotate Y end - i: %d, j: %d\n", i, j);

	// rotate C (chrominance)
	unsigned char *outC = outYuv + offset;
	unsigned char *inC = yuvInBuffer + offset;

	int inCWidth = inWidth;
	int inCHeight = inHeight / 2;
	int outCWidth = ow;
	int outCHeight = oh / 2;

//	printf(
//			"rotate C: out C width: %d, out C height: %d, inCWidth: %d, inCHeight: %d\n",
//			outCWidth, outCHeight, inCWidth, inCHeight);
	for (i = 0; i < outCHeight; i++) {
		for (j = 0; j < outCWidth; j += 2) {
//			printf("loop - i: %d, j: %d\n", i, j);
			int inI = (int) (j * 0.5);
			int inJ = inCWidth - 2 * i - 2;
//			printf("rotating U - oC[%d, %d] = iC[%d, %d]\n", i, j, inI, inJ);
			outC[i * outCWidth + j] = inC[inI * inCWidth + inJ]; // rotate U component
//			printf("rotating V - oC[%d, %d] = iC[%d, %d]\n", i, j + 1, inI, inJ + 1);
			outC[i * outCWidth + j + 1] = inC[inI * inCWidth + inJ + 1]; // rotate V component
		}
	}
//	printf("rotate C end - i: %d, j: %d\n", i, j);
	return outYuv;
}

unsigned char *rotateYUV420SP(jbyte *yuvInBuffer, jint inWidth, jint inHeight,
		ROTATE_DEGREE degree, jint *outWidth, jint *outHeight) {
	unsigned char *outYuv = NULL;
//	D("rotateYUV420SP - rotate degree: %d", degree);
	switch (degree) {
	case ROTATE_90:
		outYuv = rotateYUV420SPDegree90(yuvInBuffer, inWidth, inHeight,
				outWidth, outHeight);
		break;
	case ROTATE_270:
		outYuv = rotateYUV420SPDegree270(yuvInBuffer, inWidth, inHeight,
				outWidth, outHeight);
		break;
	}
	return outYuv;
}


