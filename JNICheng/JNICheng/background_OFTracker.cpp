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
#include "background_OFTracker.h"
#include "tracker.h"


JNIEXPORT jlong JNICALL Java_background_OFTracker_createOFTracker
  (JNIEnv *env , jobject obj) {

	  TrackingObject *tracker = new TrackingObject();
	  return (jlong)tracker;

}

/*
 * Class:     
 * Method:    startTracking
 * Signature: (JJII)V
 */
JNIEXPORT jboolean JNICALL Java_background_OFTracker_startTracking
  (JNIEnv *env, jobject obj, jlong addrObj, jlong addrMat, jintArray rectarray, jint corners, jint min_d, jfloat rate) {

	  TrackingObject *tracker = (TrackingObject*)addrObj;

	  cv::Mat& mat = *(cv::Mat*)addrMat;
	  IplImage ipl(mat);

	  int* rect= (int*)env->GetPrimitiveArrayCritical(rectarray, 0);
	  CvRect t_rect;
	  t_rect.x = rect[0];
	  t_rect.y = rect[1];
	  t_rect.width = rect[2];
	  t_rect.height = rect[3];
	  bool ret = tracker->startTracking(&ipl,t_rect,corners,min_d,rate);
	  return (jboolean)ret;
}

/*
 * Class:     
 * Method:    nextObjectRect
 * Signature: (JJIIZ)V
 */
JNIEXPORT jboolean JNICALL Java_background_OFTracker_nextObjectRect
  (JNIEnv *env, jobject obj, jlong addrObj, jlong addrMat) {

	   TrackingObject *tracker = (TrackingObject*)addrObj;

	  cv::Mat& mat = *(cv::Mat*)addrMat;
	  IplImage ipl(mat);
	  bool ret = tracker->nextObjectRect(&ipl);
	  return (jboolean)ret;
}

/*
 * Class:     
 * Method:    getMovVector
 * Signature: (JJIIIZ)V
 */
JNIEXPORT void JNICALL Java_background_OFTracker_getMovVector
  (JNIEnv *env, jobject obj, jlong addrObj, jintArray vecarray) {

	    TrackingObject *tracker = (TrackingObject*)addrObj;

		int* vec= (int*)env->GetPrimitiveArrayCritical(vecarray, 0);

		vec[0] = tracker->mov_vector.x;
		vec[1] = tracker->mov_vector.y;
}


JNIEXPORT void JNICALL Java_background_OFTracker_release
  (JNIEnv *env, jobject obj, jlong addrObj) {

	  TrackingObject *tracker = (TrackingObject*)addrObj;
	  delete tracker;

}
