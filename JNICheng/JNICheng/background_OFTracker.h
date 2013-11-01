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
/* Header for class OFTracker */

#ifndef _Included_OFTracker
#define _Included_OFTracker
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     
 * Method:    startTracking
 * Signature: (JJII)V
 */
JNIEXPORT jlong JNICALL Java_background_OFTracker_createOFTracker
  (JNIEnv *, jobject);

/*
 * Class:     
 * Method:    startTracking
 * Signature: (JJII)V
 */
JNIEXPORT jboolean JNICALL Java_background_OFTracker_startTracking
  (JNIEnv *, jobject, jlong, jlong, jintArray, jint, jint, jfloat);

/*
 * Class:     
 * Method:    nextObjectRect
 * Signature: (JJIIZ)V
 */
JNIEXPORT jboolean JNICALL Java_background_OFTracker_nextObjectRect
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     
 * Method:    getMovVector
 * Signature: (JJIIIZ)V
 */
JNIEXPORT void JNICALL Java_background_OFTracker_getMovVector
  (JNIEnv *, jobject, jlong, jintArray);

JNIEXPORT void JNICALL Java_background_OFTracker_release
  (JNIEnv *, jobject, jlong);


#ifdef __cplusplus
}
#endif
#endif
