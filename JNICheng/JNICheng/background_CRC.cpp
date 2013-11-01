/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Written (W) 2013 Weihao Cheng
 * Copyright (C) 2013 Weihao Cheng
 */

#include "background_CRC.h"
#include "CRClassifier.h"

JNIEXPORT jlong JNICALL Java_background_CRClassifier_crClassifier
  (JNIEnv *env, jobject obj, jint comp) {

	  CRClassifier *crc = new CRClassifier(comp);
	  return (jlong)crc;
}


JNIEXPORT void JNICALL Java_background_CRClassifier_train
  (JNIEnv *env, jobject obj, jlong addr, jlong sam, jlong lab) {

	  CRClassifier *crc = (CRClassifier*)addr;
	  cv::Mat &samples = *((cv::Mat*)sam);
	  cv::Mat &labels = *((cv::Mat*)lab);

	  crc->train(samples, labels);

}



JNIEXPORT void JNICALL Java_background_CRClassifier_predict
  (JNIEnv *env, jobject obj, jlong addr, jlong addrsrc, jintArray cls, jdoubleArray dist) {

	  CRClassifier *crc = (CRClassifier*)addr;

	  cv::Mat &test = *((cv::Mat*)addrsrc);
	  int *classes = (int*)env->GetIntArrayElements(cls,0);
	  double *dists = (double*)env->GetDoubleArrayElements(dist,0);

	  crc->predict(test,classes[0],dists[0]);


}