/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Written (W) 2013 Weihao Cheng
 * Copyright (C) 2013 Weihao Cheng
 */

#include <jni.h>
/* Header for class JNICheng */

#ifndef _Included_JNICheng
#define _Included_JNICheng
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_background_JNICheng_elbp
  (JNIEnv *, jobject, jlong, jlong, jint, jint);

JNIEXPORT void JNICALL Java_background_JNICheng_histc
  (JNIEnv *, jobject, jlong, jlong, jint, jint, jboolean);

JNIEXPORT void JNICALL Java_background_JNICheng_spatialHistogram
  (JNIEnv *, jobject, jlong, jlong, jint, jint, jint, jboolean);

JNIEXPORT jboolean JNICALL Java_background_JNICheng_getMouthEllipse
  (JNIEnv *, jobject, jlong, jdoubleArray);

JNIEXPORT void JNICALL Java_background_JNICheng_loadImage(JNIEnv *, jobject, jbyteArray, jint, jint, jlong);

JNIEXPORT void JNICALL Java_background_JNICheng_imshow
  (JNIEnv *, jobject, jbyteArray, jlong);


JNIEXPORT void JNICALL Java_background_JNICheng_namedWindow
  (JNIEnv *, jobject, jbyteArray);

JNIEXPORT void JNICALL Java_background_JNICheng_destroyWindow
  (JNIEnv *, jobject, jbyteArray);

JNIEXPORT jchar JNICALL Java_background_JNICheng_waitKey
  (JNIEnv *env, jobject obj, jint delay);


#ifdef __cplusplus
}
#endif
#endif
