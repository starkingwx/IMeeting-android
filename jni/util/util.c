/*
 * util.c
 *
 *  Created on: 2012-10-22
 *      Author: star
 */

#include "util.h"
#include "../common.h"

unsigned char* rotateYUV420SPDegree90(jbyte *yuvInBuffer, jint inWidth, jint inHeight,
		jint* outWidth, jint* outHeight) {
	jint size = inWidth * inHeight;
	jint offset = size;
	*outWidth = inHeight;
	*outHeight = inWidth;

	int ow = (*outWidth);
	int oh = (*outHeight);

	D("rotate: out width: %d, out height: %d, inWidth: %d, inHeight: %d", ow, oh, inWidth, inHeight);

	unsigned char *outYuv = (unsigned char *)malloc(sizeof(unsigned char) * (3 * size) / 2);

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
	D("rotate Y end - i: %d, j: %d", i, j);

	// rotate C (chrominance)
	jbyte *outC = outYuv + offset;
	jbyte *inC = yuvInBuffer + offset;

	jint inCWidth = inWidth;
	jint inCHeight = inHeight / 2;
	jint outCWidth = ow;
	jint outCHeight = oh / 2;

	D("rotate C: out C width: %d, out C height: %d, inCWidth: %d, inCHeight: %d", outCWidth, outCHeight, inCWidth, inCHeight);
	int count = 0;
	for (i = 0; i < outCHeight; i++) {
		for (j = 0; j < outCWidth; j += 2) {
			count++;
//			D("loop - i: %d, j: %d", i, j);
//			D("rotating U - oC[%d, %d] = iC[%d, %d]", i, 2 * j, inCHeight - 1 - j, 2 * i);
			outC[i * outCWidth + j] = inC[(inCHeight - 1 - (int)(j * 0.5)) * inCWidth + 2 * i]; // rotate U component
//			D("rotating V - oC[%d, %d] = iC[%d, %d]", i, 2 * j + 1, inCHeight - 1 - j, 2 * i + 1);
			outC[i * outCWidth + j + 1] = inC[(inCHeight - 1 - (int)(j * 0.5)) * inCWidth + 2 * i + 1]; // rotate V component
		}
	}
	D("rotate C end - count: %d, i: %d, j: %d", count, i, j);
	return outYuv;
}
