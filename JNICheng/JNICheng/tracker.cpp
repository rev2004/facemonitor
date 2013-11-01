/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Written (W) 2013 Weihao Cheng
 * Copyright (C) 2013 Weihao Cheng
 */

#include <iostream>

#include "tracker.h"

TrackingObject::TrackingObject() {
    this->good_count = 0;
    this->corner_count = 0;
    this->cornersA = NULL;
    this->cornersB = NULL;
    this->eig_image = NULL;
    this->temp_image = NULL;
    this->pyrA = NULL;
    this->pyrB = NULL;
    this->imgA = NULL;
    this->imgB = NULL;
    this->features_error = NULL;
    this->features_found = NULL;
    
    this->mov_vector.x = 0;
    this->mov_vector.y = 0;

	loseRate = 0.5;
    
}



void TrackingObject::clear() {
    
    this->mov_vector.x = 0;
    this->mov_vector.y = 0;
    
    
    this->good_count = 0;
    this->corner_count = 0;
    
    if(cornersA) {
        delete [] cornersA;
        cornersA = NULL;
    }
    if(cornersB) {
        delete [] cornersB;
        cornersB = NULL;
    }
    if(features_error) {
        delete [] features_error;
        features_error = NULL;
    }
    if(features_found) {
        delete[] features_found;
        features_found = NULL;
    }
    
    if(eig_image) {
        cvReleaseImage(&eig_image);
        eig_image = NULL;
    }
    
    if(temp_image) {
        cvReleaseImage(&temp_image);
        temp_image = NULL;
    }
    
    if(pyrA) {
        cvReleaseImage(&pyrA);
        pyrA = NULL;
    }
    
    if(pyrB) {
        cvReleaseImage(&pyrB);
        pyrB = NULL;
    }
    
    if(imgA) {
        cvReleaseImage(&imgA);
        imgA = NULL;
    }
    
    if(imgB) {
        cvReleaseImage(&imgB);
        imgB = NULL;
    }   
}

void TrackingObject::adjustCaptureRect(CvPoint center, CvSize bound_size) {
    
    int lx = 0;
    int ly = 0;
    
    lx = capture_rect.x + center.x - capture_rect.width/2;
    
    capture_rect.x = lx > 0 ? lx : 0;
    
    
    ly = capture_rect.y + center.y - capture_rect.height/2;
    
    capture_rect.y = ly > 0 ? ly : 0;
    
    
    if(capture_rect.x + capture_rect.width > bound_size.width) {
        
        capture_rect.x = bound_size.width - capture_rect.width;
    }
    
    if(capture_rect.y + capture_rect.height > bound_size.height) {
        
        capture_rect.y = bound_size.height - capture_rect.height;
    }
}

void TrackingObject::obtainCaptureRect(CvRect track_rect, CvSize bound_size) {
    
    capture_rect.x = (int)(track_rect.x - track_rect.width*0.5f);
    if(capture_rect.x < 0) 
        capture_rect.x = 0;
    capture_rect.y = (int)(track_rect.y - track_rect.height*0.5f);
    if(capture_rect.y < 0)
        capture_rect.y = 0;
    
    
    capture_rect.width = track_rect.width*2;
    
    if( capture_rect.x + capture_rect.width >= bound_size.width) {
        capture_rect.width = bound_size.width - capture_rect.x;
    }
    
    capture_rect.height = track_rect.height*2;
    
    if( capture_rect.y + capture_rect.height >= bound_size.height) {
        capture_rect.height = bound_size.height - capture_rect.y;
    }
    
}

bool TrackingObject::startTracking(IplImage *src, CvRect t_rect, int corners, int min_d, float rate, IplImage *mask) {
    
    if(corners <= 0 || !src ) {
        return false;
    }
    clear();

	loseRate = rate;
    
    corner_count = corners;
    good_count = corners;
    
    cornersA = new CvPoint2D32f[corner_count];
    cornersB = new CvPoint2D32f[corner_count];
    
    //capture_rect = t_rect;
    
    obtainCaptureRect(t_rect, cvSize(src->width, src->height));
    
    CvSize image_size = cvSize(capture_rect.width,capture_rect.height);
    
    
    this->eig_image = cvCreateImage(image_size, IPL_DEPTH_32F, 1);
    this->temp_image = cvCreateImage(image_size, IPL_DEPTH_32F, 1);
    
    CvSize pyr_size = cvSize(image_size.width+8, image_size.height/3);
    
    this->pyrA = cvCreateImage(pyr_size, IPL_DEPTH_32F, 1);
    this->pyrB = cvCreateImage(pyr_size, IPL_DEPTH_32F, 1);
    
    this->imgA = cvCreateImage(image_size, IPL_DEPTH_8U, 1);
    this->imgB = cvCreateImage(image_size, IPL_DEPTH_8U, 1);
    
    
    IplImage *cmask = NULL;
    if(!mask) {
        
        cmask = cvCreateImage(image_size, IPL_DEPTH_8U, 1);
        cvZero(cmask);
        cvSetImageROI(cmask, cvRect(image_size.width/4, image_size.height/4, image_size.width/2, image_size.height/2));
        cvSet(cmask, cvScalar(255));
        cvResetImageROI(cmask);
    }
    else {
        cmask = cvCreateImage(cvGetSize(mask), IPL_DEPTH_8U, 1);
        cvCopy(mask, cmask);
    }
    
    cvSetImageROI(src, capture_rect);
    cvCopy(src, imgA);
    cvResetImageROI(src);
    
    
    cvGoodFeaturesToTrack(imgA, eig_image, temp_image, cornersA, &corner_count, 0.01, min_d, cmask);
    
    cvReleaseImage(&cmask);
    
    features_error = new float[corner_count];
    features_found = new char[corner_count];
    
    int g_x = 0;
    int g_y = 0;
    
    good_count = corner_count;
    
    
    for(int i=0; i<good_count; i++) {
        
        
        CvPoint pt = cvPoint(cvRound(cornersB[i].x),cvRound(cornersB[i].y));
        g_x += pt.x;
        g_y += pt.y;
        
        
    }
    
    
    
    return true;
    
}

bool TrackingObject::nextObjectRect(IplImage *src) {
    
    if(!imgB || !imgA)
        return false;
    
    cvSetImageROI(src, capture_rect);
    cvCopy(src,imgB);
    cvResetImageROI(src);
    
    //if(!show_image)
    //    show_image = cvCreateImage(cvSize(imgB->width,imgB->height), IPL_DEPTH_8U, 1);
    //cvCopy(imgB,show_image);
    
    
    cvCalcOpticalFlowPyrLK(imgA, imgB, pyrA, pyrB, cornersA, cornersB, corner_count, cvSize(20,20), 1, features_found, features_error, cvTermCriteria(CV_TERMCRIT_EPS|CV_TERMCRIT_ITER, 20, 0.3), 0);
    
    
    int g_x = 0, g_y = 0;
    
    
    for(int i=0; i<good_count; i++) {
        
        
        if(features_found[i] == 0 || features_error[i] > capture_rect.width/2) {
            
            //conersB[i].x 
            
            CvPoint2D32f t = cornersB[i];
            cornersB[i] = cornersB[good_count-1];
            cornersB[good_count-1] = t;
            
            char fft = features_found[i];
            features_found[i] = features_found[good_count-1];
            features_found[good_count-1] = fft;
            
            float fet = features_error[i];
            features_error[i] = features_error[good_count-1];
            features_error[good_count-1] = fet;
            
            good_count--;
            continue;
        }
        
        CvPoint pt = cvPoint(cvRound(cornersB[i].x),cvRound(cornersB[i].y));
        g_x += pt.x;
        g_y += pt.y;
        
        
    }
    
    if(good_count < corner_count * loseRate) {
        
        clear();
        return false;
    }
    
    CvPoint center;
    
    center.x = g_x / good_count;
    center.y = g_y / good_count;
    
    mov_vector.x = center.x - (capture_rect.width/2);
    mov_vector.y = center.y - (capture_rect.height/2);
    
    
    adjustCaptureRect(center, cvSize(src->width, src->height));
    
    
    void *t = imgA;
    imgA = imgB;
    imgB = (IplImage*)t;
    
    t = cornersA;
    cornersA = cornersB;
    cornersB = (CvPoint2D32f*)t;
    
    t = pyrA;
    pyrA = pyrB;
    pyrB = (IplImage*)t;
    
    
    //relocate the corner points to the new capture rectangle
    
    
    return true;
    
    
}

