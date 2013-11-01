/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Written (W) 2013 Weihao Cheng
 * Copyright (C) 2013 Weihao Cheng
 */

#include <opencv2/opencv.hpp>
#include "background_JNICheng.h"
#include "jni_build.h"
#include "mme.h"


JNIEXPORT void JNICALL Java_background_JNICheng_elbp
  (JNIEnv *env, jobject obj, jlong srcAddress, jlong destAddress,jint radius, jint neighbors)
{
	cv::Mat& src  = *(cv::Mat*)srcAddress;
	cv::Mat& dst = *(cv::Mat*)destAddress;
	elbp_(src,dst,radius,neighbors);
}

/*
 * Class:     JNITest
 * Method:    histc_
 * Signature: (Lorg/opencv/core/Mat;IIZ)Lorg/opencv/core/Mat;
 */
JNIEXPORT void JNICALL Java_background_JNICheng_histc
  (JNIEnv * env, jobject obj, jlong srcAddress,jlong destAddress, jint minVal, jint maxVal, jboolean normed)
{
	cv::Mat& src  = *(cv::Mat*)srcAddress;
	cv::Mat& dst = *(cv::Mat*)destAddress;
    
    histc_(src,dst,minVal,maxVal,normed);
}

/*
 * Class:     JNITest
 * Method:    spatial_histogram
 * Signature: (Lorg/opencv/core/Mat;III)Lorg/opencv/core/Mat;
 */
JNIEXPORT void JNICALL Java_background_JNICheng_spatialHistogram
  (JNIEnv * env, jobject obj, jlong srcAddress,jlong destAddress, jint numPatterns, jint grid_x, jint grid_y, jboolean normed)
{
	cv::Mat& src  = *(cv::Mat*)srcAddress;
	cv::Mat& dst = *(cv::Mat*)destAddress;

	spatial_histogram(src,dst,numPatterns,grid_x,grid_y,normed);
}

JNIEXPORT jboolean JNICALL Java_background_JNICheng_getMouthEllipse
  (JNIEnv *env, jobject obj, jlong addrMat, jdoubleArray arrayEll) {

	  cv::Mat &mat = *(cv::Mat*)addrMat;
	 
	  double *ell =  (double*)env->GetPrimitiveArrayCritical(arrayEll,0);

	  cv::RotatedRect rect;
	  
	  bool ret = getMouthEllipse(mat,rect);
	  ell[0] = rect.center.x;
	  ell[1] = rect.center.y;
	  ell[2] = rect.size.width;
	  ell[3] = rect.size.height;
	  ell[4] = rect.angle;

	  return (jboolean)ret;

}

JNIEXPORT void JNICALL Java_background_JNICheng_loadImage
	(JNIEnv *env, jobject obj, jbyteArray arrayName, jint arrayLen, jint flags, jlong addrMat) {
	char *name = (char*)env->GetPrimitiveArrayCritical(arrayName,0);
	  cv::Mat &mat = *(cv::Mat*)addrMat;

	 
	  char path[256];
	  strcpy(path,name);
	  path[arrayLen] = 0;

	  IplImage *ipl = cvLoadImage(path,flags);

	  cv::Mat read(ipl);
	  read.copyTo(mat);
}

JNIEXPORT void JNICALL Java_background_JNICheng_imshow
  (JNIEnv *env, jobject obj, jbyteArray arrayName, jlong addrMat) {

	  char *name = (char*)env->GetPrimitiveArrayCritical(arrayName,0);
	  cv::Mat &mat = *(cv::Mat*)addrMat;

	  cv::imshow(name,mat);

}


JNIEXPORT void JNICALL Java_background_JNICheng_namedWindow
  (JNIEnv *env, jobject obj, jbyteArray arrayName) {

	  char *name = (char*)env->GetPrimitiveArrayCritical(arrayName,0);
	  cv::namedWindow(name);
	

}

JNIEXPORT void JNICALL Java_background_JNICheng_destroyWindow
  (JNIEnv *env, jobject obj, jbyteArray arrayName) {

	  char *name = (char*)env->GetPrimitiveArrayCritical(arrayName,0);
	  cv::destroyWindow(name);
	

}

JNIEXPORT jchar JNICALL Java_background_JNICheng_waitKey
  (JNIEnv *env, jobject obj, jint delay) {

	  return (jchar)cv::waitKey(delay);

}

