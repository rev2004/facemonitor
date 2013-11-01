/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Written (W) 2013 Weihao Cheng
 * Copyright (C) 2013 Weihao Cheng
 */

#include <opencv2\opencv.hpp>
#include "background_MyVideoCapture.h"



JNIEXPORT void JNICALL Java_background_MyVideoCapture_releaseVideoCapture
  (JNIEnv * env, jobject obj, jlong videoCaptureAddress)
{
	cv::VideoCapture * videoCapture = (cv::VideoCapture *)videoCaptureAddress;
	videoCapture->release();
	delete videoCapture;
}

JNIEXPORT jlong JNICALL Java_background_MyVideoCapture_myVideoCapture__
  (JNIEnv * env, jobject obj)
{
	cv::VideoCapture * videoCapture =new cv::VideoCapture();
	return (jlong)videoCapture;
}


JNIEXPORT jlong JNICALL Java_background_MyVideoCapture_myVideoCapture___3B
  (JNIEnv * env, jobject obj, jbyteArray filename)
{

	char* path= (char*)env->GetPrimitiveArrayCritical(filename, 0);
	
	cv::VideoCapture * videoCapture =new cv::VideoCapture(path);
	return (jlong)videoCapture;
}


JNIEXPORT jlong JNICALL Java_background_MyVideoCapture_myVideoCapture__I
  (JNIEnv * env, jobject obj, jint device)
{
	cv::VideoCapture * videoCapture =new cv::VideoCapture(device);
	return (jlong)videoCapture;
}


JNIEXPORT jboolean JNICALL Java_background_MyVideoCapture_read
  (JNIEnv * env, jobject obj, jlong videoCaptureAddress, jlong imageAddress)
{
	cv::VideoCapture * videoCapture = (cv::VideoCapture *)videoCaptureAddress;
        
	cv::Mat& image  = *(cv::Mat*)imageAddress;
	bool _retval_ = videoCapture->read( image );

    return _retval_;
}


JNIEXPORT jboolean JNICALL Java_background_MyVideoCapture_set
  (JNIEnv * env, jobject obj, jlong videoCaptureAddress, jint propId, jdouble value)
{
	cv::VideoCapture * videoCapture = (cv::VideoCapture *)videoCaptureAddress;
	return videoCapture->set(propId,value);
}



JNIEXPORT jdouble JNICALL Java_background_MyVideoCapture_get
  (JNIEnv * env, jobject obj, jlong videoCaptureAddress, jint propId)
{
	cv::VideoCapture * videoCapture = (cv::VideoCapture *)videoCaptureAddress;
	return videoCapture->get(propId);
}

JNIEXPORT jlong JNICALL Java_background_MyVideoCapture_myFileVideoCapture
  (JNIEnv * env, jobject obj, jbyteArray filename, jint arrayLen)
{
	char* name= (char*)env->GetPrimitiveArrayCritical(filename, 0);

	char path[256];
	strcpy(path,name);
	path[arrayLen] = 0;
	
	cv::VideoCapture * videoCapture =new cv::VideoCapture(path);
	return (jlong)videoCapture;
}

















