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

//static void elbp_(cv::Mat &src, cv::Mat &dst, int radius, int neighbors);
void elbp_(cv::Mat &src, cv::Mat &dst, int radius, int neighbors);

void histc_( cv::Mat &src, cv::Mat &dst,int minVal, int maxVal, bool normed);


void spatial_histogram(cv::Mat &src, cv::Mat &dst, int numPatterns,
                               int grid_x, int grid_y, bool normed);

void convertIplToMat(IplImage *img, cv::Mat &mat);