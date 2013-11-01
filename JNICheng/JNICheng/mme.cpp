/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Written (W) 2013 Weihao Cheng
 * Copyright (C) 2013 Weihao Cheng
 */

#include "mme.h"

#include <math.h>



void radiationLine(cv::Mat &src,cv::Point s_pt,cv::Point vec,cv::Point &t_pt, int thres) {
    
    
    int dx = vec.x;
    int dy = vec.y;
    
    int ax = dx >= 0 ? 1 : -1;
    int ay = dy >= 0 ? 1 : -1;
    if(abs(dy) > abs(dx)) {
        int e = -dy;
		int aind = (int)(dy >= 0 ? src.step[0] : -src.step[0]);
        if(dx < 0)
            dx = -dx;
        if(dy < 0)
            dy = -dy;
        for(int y = s_pt.y , x = s_pt.x ,index=(int)(s_pt.y*src.step[0]); ;y+=ay,index+=aind) {
            
            if(x <= 0 || x >= src.cols-1 || y <= 0 || y >= src.rows-1) {
                t_pt.x = -1;
                t_pt.y = -1;
                break;
            }

            if((unsigned char)src.data[index+x] >= thres ) {
                
                t_pt.x = x;
                t_pt.y = y;
                break;
            }
           
            if(e < 0) {
                e = e + dx + dx;
            }
            else {
                x = x + ax;
                e = e + dx + dx - dy - dy;
            }
        }
        
    }
    else {
        int e = -dx;
		int aind = (int)(dy >= 0 ? src.step[0] : -src.step[0]);
        if(dx < 0)
            dx = -dx;
        if(dy < 0)
            dy = -dy;
		for(int x = s_pt.x , y = s_pt.y,index=(int)(s_pt.y*src.step[0]); ;x+=ax) {
            
            
            if(x <= 0 || x >= src.cols-1 || y <= 0 || y >= src.rows-1) {
                t_pt.x = -1;
                t_pt.y = -1;
                break;
            }
            if((unsigned char)src.data[index+x] >= thres ) {
                
                t_pt.x = x;
                t_pt.y = y;
                break;
            }
            if(e < 0) {
                e = e + dy + dy;
            }
            else {
                y = y + ay;
                index += aind;
                e = e + dy + dy - dx - dx;
            }
        }
    }
}


int centerRadiation(cv::Mat &src, cv::Point center, cv::Point *points, int pt_count, int thres) {
    
    static int vec_x[] = { 1,  3,  2,  1,  1,  1,  0, -1, -1, -1, -2, -3, -1, -3, -2, -1, -1, -1,  0,  1,  1,  1,  2,  3 };
    static int vec_y[] = { 0,  1,  1,  1,  2,  3,  1,  3,  2,  1,  1,  1,  0, -1, -1, -1, -2, -3, -1, -3, -2, -1, -1, -1 }; 
    
	int found = 0;
    for(int i=0; i<24; i++) {
        
        
        radiationLine(src, center, cv::Point(vec_x[i],vec_y[i]), points[i], thres);
        if(points[i].x != -1 && points[i].y != -1) {
			found++;
		}
        
    }

	return found;
    
}

int rankPointDist(cv::Point *points, float *dists, int pt_count) {
    
    memset(dists, 0, sizeof(float)*pt_count);
    cv::Point center = cv::Point(0, 0);
    
    int good_count = 0;
    
    for(int i=0;i<pt_count;i++) {
        if(points[i].x == -1 || points[i].y == -1) {
            continue;
        }
        good_count++;
        center.x += points[i].x;
        center.y += points[i].y;
    }
    if(good_count <= 0) {
        return 0;
    }
    
    center.x /= good_count;
    center.y /= good_count;
    
    for(int i=0;i<pt_count;i++) {
        
        float d = 0;
        if(points[i].x == -1 || points[i].y == -1) {
            d = FLT_MAX;
        }
        else {
            
            d = sqrtf(powf(points[i].x-center.x,2)+powf(points[i].y-center.y,2));
        }
        for(int j=0;j<pt_count;j++) {
            if(d > dists[j]) {
                
                if(j > 0)
                    dists[j-1] = dists[j];
                dists[j] = d;
            }
            
        }
    }
    return good_count;
    
}

void findContourRegion(cv::Mat &src, cv::Mat &dst, int aperture_size) {
    
    
    cv::Mat dx(src.rows, src.cols, CV_16SC(1));
    cv::Mat dy(src.rows, src.cols, CV_16SC(1));
    
    
    
    
    cv::Sobel(src, dx, CV_16S, 1, 0, aperture_size, 1, 0, cv::BORDER_REPLICATE);
    cv::Sobel(src, dy, CV_16S, 0, 1, aperture_size, 1, 0, cv::BORDER_REPLICATE);
    
    
    //mcy
    
    int i=0;
    int j=0;
    
    cv::Mat temp(src.rows,src.cols,CV_32FC1);
    
    for(i = 0; i < src.rows; i++ )
    {
        const short* _dx = (short*)(dx.data + dx.step*i);
        const short* _dy = (short*)(dy.data + dy.step*i);
        float* _image = (float *)(temp.data + temp.step*i);
        for(j = 0; j < src.cols; j++)
        {
            _image[j] = (float)(abs(_dx[j]) + abs(_dy[j]));
            // maxv = maxv < _image[j] ? _image[j]: maxv;
        }
    }
    
    
    cv::convertScaleAbs(temp, dst);
    
}

bool getMouthEllipse(cv::Mat &frame, cv::RotatedRect &mellipse) {



	cv::Mat rgb[3];

	cv::split(frame,rgb);

	cv::Mat red = rgb[2];
    

   
    
    //very necessary
	cv::GaussianBlur(red,red,cv::Size(3,3),0);
	cv::GaussianBlur(red,red,cv::Size(3,3),0);

	cv::bitwise_not(red,red);

    cv::normalize(red, red,0,255,cv::NORM_MINMAX);
    

    cv::Mat laplace(red.rows,red.cols,CV_8UC1);

	findContourRegion(red,laplace,3);
    
    
    for(int y=0;y<red.rows;y++) {
        
        
		for(int x=0;x<red.cols;x++) {
            
            
			cv::Vec3b vec = frame.at<cv::Vec3b>(y,x);
            
			unsigned char r = vec[2];
			//unsigned char b = vec[1];
			unsigned char g = vec[0];
            
            
            red.at<unsigned char>(y,x) = red.at<unsigned char>(y,x)*0.7 + 76.0*(r+1)/(r+g+1);
            
            
        }
    }
    
	    
    
    
    
    const int search_width = red.cols/2;
    const int search_height = red.rows/2;
    
    const int up_offset = red.rows/10;
    const int left_offset = search_width/2;

	cv::Rect window = cv::Rect(left_offset, up_offset, search_width, search_height);

	cv::meanShift(red, window, cv::TermCriteria(CV_TERMCRIT_EPS|CV_TERMCRIT_ITER, 15, 0.05));

    
    
    cv::Point shift_center;
    shift_center.x = window.x + window.width/2;
    shift_center.y = window.y + window.height/2;
    
	cv::Rect shift_box = cvRect(0, window.y, red.cols, search_height);
    
    
   cv::Mat left_lap = laplace(cv::Rect(0,0,red.cols/2,red.rows));

    
    static int bin[256] = {0};
    memset(bin, 0, sizeof(int)*256);
    for(int y=0;y<left_lap.rows;y++) {
        
        for(int x=0;x<left_lap.cols;x++) {
            
            bin[left_lap.at<unsigned char>(y,x)]++;
        }
        
    }
    const int thres_area = laplace.rows*laplace.cols/20;
    int thres = 0;
    for(int i=255,acc=0;i>=0;i--) {
        
        acc += bin[i];
        if(acc > thres_area) {
            thres = i;
            break;
        }
    }
    if( thres < 1 ) {
        thres = 2;
    }
    

	cv::threshold(left_lap,left_lap,thres-1,100,cv::THRESH_BINARY);
    
    memset(bin, 0, sizeof(int)*256);

	cv::Mat right_lap = laplace(cv::Rect(red.cols/2,0,red.cols/2,red.rows));
    

    for(int y=0;y<right_lap.rows;y++) {
        
        for(int x=0;x<right_lap.cols;x++) {
            
            bin[right_lap.at<unsigned char>(y,x)]++;
        }
        
    }
    thres = 0;
    for(int i=255,acc=0;i>=0;i--) {
        
        acc += bin[i];
        if(acc > thres_area) {
            thres = i;
            break;
        }
    }
    if( thres < 1 ) {
        thres = 2;
    }
    
    cv::threshold(right_lap,right_lap,thres-1,100,cv::THRESH_BINARY);

	const int region_count = 5;
    
	struct RegionComp {

		cv::Point seed;
		cv::Rect rect;
		int area;
        
        RegionComp() {
            area = 0;
        }

		bool empty() {
			return area == 0;
		}
	};

	RegionComp top_regions[region_count];
    
	int found = 0;

	for(int y=shift_box.y;y<shift_box.y+shift_box.height;y++) {

		for(int x=shift_box.x;x<shift_box.x+shift_box.width;x++) {


			if(laplace.at<unsigned char>(y,x) == 100) {
				RegionComp comp;
				int area = cv::floodFill(laplace,cv::Point(x,y),101,&comp.rect);
				comp.area = area;
				comp.seed = cv::Point(x,y);

				found++;
                
                for (int i=0; i<region_count; i++) {
                    if(comp.area > top_regions[i].area) {
                        
                        if(i >= 1) {
                            top_regions[i-1] = top_regions[i];
                        }
                        top_regions[i] = comp;
                    }
                    
                }
			}
		}
	}
    

	if(found <= 0)
		return false;
    
    
    
    for(int i=0;i<region_count;i++) {
        if(top_regions[i].empty()) {
            continue;
        }

		cv::floodFill(laplace,top_regions[i].seed,200+i,NULL);
    }
    
    cv::threshold(laplace, laplace, 199, 0, CV_THRESH_TOZERO);

    
    cv::Point ret_point[24];
    int good_count = centerRadiation(laplace, shift_center, ret_point, 24, 200);
    
    
    int votes[region_count] = {0};
    
    for(int i=0;i<24;i++) {
        
        if(ret_point[i].x == -1|| ret_point[i].y == -1)
            continue;
        
        unsigned char value = laplace.at<unsigned char>(ret_point[i].y,ret_point[i].x);
        if(value >= 200) {
            votes[(value-200)]++;
        }
    }
    //sample the points to fit ellipse

	cv::vector<cv::Point2f> samp_points;

    

	for(int y=0;y<laplace.rows;y++) {
        
        for(int x=0;x<laplace.cols;x++) {
            
            
            unsigned char var = laplace.at<unsigned char>(y,x);
            if(var >= 200 && votes[(var-200)] >= 2) {
                
				samp_points.push_back(cv::Point2f(x,y));
                
            }
            else {
                laplace.at<unsigned char>(y,x) = 0;
            }
        }
    }
	int n_samp = (int)samp_points.size();

	if(n_samp <= 0)
        return false;

	cv::RotatedRect ell; 
    if(n_samp > 10) {
		ell = cv::fitEllipse(samp_points);
        mellipse = ell;
    }
    bool mouth_open = true;
	cv::Rect top_rect = top_regions[0].rect;
    if(good_count < 12) {
        mouth_open = false;
    }
    else if(good_count == 24 && top_rect.height >= window.height) {
        mouth_open = true;
        
    }
    else {
        float ret_point_dist[24];
        rankPointDist(ret_point, ret_point_dist, 24);
        if(ret_point_dist[good_count/3] < window.height/3) {
            mouth_open = false;
        }
       
        
    }
    
    
    return mouth_open;


}
