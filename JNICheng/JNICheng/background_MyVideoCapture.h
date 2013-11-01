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
/* Header for class background_MyVideoCapture */

#ifndef _Included_background_MyVideoCapture
#define _Included_background_MyVideoCapture
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     background_MyVideoCapture
 * Method:    myVideoCapture
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_background_MyVideoCapture_myVideoCapture__
  (JNIEnv *, jobject);

/*
 * Class:     background_MyVideoCapture
 * Method:    myVideoCapture
 * Signature: ([B)J
 */
JNIEXPORT jlong JNICALL Java_background_MyVideoCapture_myVideoCapture___3B
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     background_MyVideoCapture
 * Method:    myVideoCapture
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_background_MyVideoCapture_myVideoCapture__I
  (JNIEnv *, jobject, jint);

/*
 * Class:     background_MyVideoCapture
 * Method:    read
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_background_MyVideoCapture_read
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     background_MyVideoCapture
 * Method:    set
 * Signature: (JID)Z
 */
JNIEXPORT jboolean JNICALL Java_background_MyVideoCapture_set
  (JNIEnv *, jobject, jlong, jint, jdouble);

/*
 * Class:     background_MyVideoCapture
 * Method:    get
 * Signature: (JI)D
 */
JNIEXPORT jdouble JNICALL Java_background_MyVideoCapture_get
  (JNIEnv *, jobject, jlong, jint);

JNIEXPORT void JNICALL Java_background_MyVideoCapture_releaseVideoCapture
  (JNIEnv *, jobject, jlong);

JNIEXPORT jlong JNICALL Java_background_MyVideoCapture_myFileVideoCapture
  (JNIEnv *, jobject, jbyteArray, jint);

#ifdef __cplusplus
}
#endif
#endif
