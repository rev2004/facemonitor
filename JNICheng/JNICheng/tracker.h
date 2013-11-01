/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Written (W) 2013 Weihao Cheng
 * Copyright (C) 2013 Weihao Cheng
 */

#ifndef FaceTracer_tracker_h
#define FaceTracer_tracker_h

#include <opencv/cv.h>
#include <opencv/cxcore.h>
#include <opencv/highgui.h>


class TrackingObject {
    
public:
    int corner_count;
    int good_count;
    CvPoint2D32f *cornersA;
    CvPoint mov_vector;
    
private:
    
    CvPoint2D32f *cornersB;
    IplImage *eig_image;
    IplImage *temp_image;
    IplImage *pyrA;
    IplImage *pyrB;
    IplImage *imgA;
    IplImage *imgB;
    CvRect capture_rect;
    char *features_found;
    float *features_error;
	float loseRate;
    
public:
    TrackingObject();
    bool startTracking(IplImage *src, CvRect t_rect, int corners, int min_d, float rate, IplImage *mask=NULL);
    bool nextObjectRect(IplImage *src);
    void clear();
    void obtainCaptureRect(CvRect track_rect, CvSize bound_size);
    void adjustCaptureRect(CvPoint center, CvSize bound_size);

    
};


#endif
