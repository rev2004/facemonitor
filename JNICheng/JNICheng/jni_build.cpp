/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Written (W) 2013 Weihao Cheng
 * Copyright (C) 2013 Weihao Cheng
 */
#include "jni_build.h"

void elbp_(cv::Mat &src, cv::Mat &dst, int radius, int neighbors) {
    
    
    dst.create(src.rows-2*radius, src.cols-2*radius, CV_32SC1);
    
    dst.setTo(0);
    for(int n=0; n<neighbors; n++) {
        
        float x = static_cast<float>(-radius * sin(2.0*CV_PI*n/static_cast<float>(neighbors)));
        float y = static_cast<float>(radius * cos(2.0*CV_PI*n/static_cast<float>(neighbors)));
        
        int fx = static_cast<int>(floor(x));
        int fy = static_cast<int>(floor(y));
        int cx = static_cast<int>(ceil(x));
        int cy = static_cast<int>(ceil(y));
        
        float ty = y - fy;
        float tx = x - fx;
        
        float w1 = (1 - tx) * (1 - ty);
        float w2 =      tx  * (1 - ty);
        float w3 = (1 - tx) *      ty;
        float w4 =      tx  *      ty;
        
        for(int i=radius; i < src.rows-radius;i++) {
            for(int j=radius;j < src.cols-radius;j++) {
                
                float t = static_cast<float>(w1*src.at<unsigned char>(i+fy,j+fx) + w2*src.at<unsigned char>(i+fy,j+cx) + w3*src.at<unsigned char>(i+cy,j+fx) + w4*src.at<unsigned char>(i+cy,j+cx));
                
                dst.at<int>(i-radius,j-radius) += ((t > src.at<unsigned char>(i,j)) || (std::abs(t-src.at<unsigned char>(i,j)) < std::numeric_limits<float>::epsilon())) << n;
            }
        }
    }
    return;
}

void histc_(cv::Mat &src, cv::Mat &dst,int minVal, int maxVal, bool normed)
{
    
    int histSize = maxVal-minVal+1;
    
    float range[] = { static_cast<float>(minVal), static_cast<float>(maxVal+1) };
    const float* histRange = { range };
    
    cv::calcHist(&src, 1, 0, cv::Mat(), dst, 1, &histSize, &histRange, true, false);
    
    if(normed) {
        dst /= (int)src.total();
    }
    return;
}


void spatial_histogram(cv::Mat &src, cv::Mat &dst, int numPatterns,
                               int grid_x, int grid_y, bool normed)
{
    
    int width = src.cols/grid_x;
    int height = src.rows/grid_y;
    
    //cv::Mat result = cv::Mat::zeros(grid_x * grid_y, numPatterns, CV_32FC1);
    
    dst.create(grid_x*grid_y, numPatterns, CV_32FC1);
	dst.setTo(0);
     
    if(src.empty()) {
     //   dst.setTo(0);
        dst = dst.reshape(1, 1);
        return;
    }
    
    int resultRowIdx = 0;
    
    for(int i = 0; i < grid_y; i++) {
        for(int j = 0; j < grid_x; j++) {
            cv::Mat src_cell = cv::Mat(src, cv::Range(i*height,(i+1)*height), cv::Range(j*width,(j+1)*width));
            cv::Mat cell_hist;
            histc_(cv::Mat_<float>(src_cell), cell_hist, 0, (numPatterns-1), true);
            cv::Mat result_row = dst.row(resultRowIdx);
            cell_hist.reshape(1,1).convertTo(result_row, CV_32FC1);
            
            resultRowIdx++;
        }
    }
    
    dst = dst.reshape(1,1);
    
    return;
}

void convertIplToMat(IplImage *img, cv::Mat &mat) {

	mat = cv::Mat(img);

}



