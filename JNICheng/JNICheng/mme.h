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

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/video/video.hpp>

void radiationLine(cv::Mat &src,cv::Point s_pt,cv::Point vec,cv::Point &t_pt, int thres);
int centerRadiation(cv::Mat &src, cv::Point center, cv::Point *points, int pt_count, int thres);
void findContourRegion(cv::Mat &src, cv::Mat &dst, int aperture_size);
int rankPointDist(cv::Point *points, float *dists, int pt_count);
bool getMouthEllipse(cv::Mat &frame, cv::RotatedRect &mellipse);
