/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Written (W) 2013 Weihao Cheng
 * Copyright (C) 2013 Weihao Cheng
 */

#pragma once

#include <jni.h>

#ifndef _Included_background_CRC
#define _Included_background_CRC
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_background_CRClassifier_crClassifier
  (JNIEnv *, jobject);


JNIEXPORT void JNICALL Java_background_CRClassifier_train
  (JNIEnv *, jobject, jlong, jlong);



JNIEXPORT void JNICALL Java_background_CRClassifier_predict
  (JNIEnv *, jobject, jlong, jlong, jintArray, jdoubleArray);




#ifdef __cplusplus
}
#endif
#endif

