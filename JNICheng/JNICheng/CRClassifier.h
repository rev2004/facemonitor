/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Written (W) 2013 Weihao Cheng
 * Copyright (C) 2013 Weihao Cheng
 */

#ifndef __cvTest__CRClassifier__
#define __cvTest__CRClassifier__

#include <opencv2/opencv.hpp>
#include <set>
#include <vector>

class CRClassifier
{
private:
    int _num_components;
    double _threshold;
    std::vector<cv::Mat> _projections;
    std::vector<int> _labels;
    cv::Mat _eigenvectors;
    cv::Mat _eigenvalues;
    cv::Mat _mean;
    
    cv::Mat _subspace;
    cv::Mat _crc_projection;
    
public:
  
    
    // Initializes an empty Eigenfaces model.
    CRClassifier(int num_components = 0, double threshold = DBL_MAX) :
    _num_components(num_components),
    _threshold(threshold) {}
    
    // Initializes and computes an Eigenfaces model with images in src and
    // corresponding labels in labels. num_components will be kept for
    // classification.
    CRClassifier(cv::InputArrayOfArrays src, cv::InputArray labels,
               int num_components = 0, double threshold = DBL_MAX) :
    _num_components(num_components),
    _threshold(threshold) {
        train(src, labels);
    }
    
    // Computes an Eigenfaces model with images in src and corresponding labels
    // in labels.
    void train(cv::InputArrayOfArrays _src, cv::InputArray _local_labels);
    
    
    // Predicts the label and confidence for a given sample.
    void predict(cv::InputArray _src, int &label, double &dist) const;

};



#endif /* defined() */
