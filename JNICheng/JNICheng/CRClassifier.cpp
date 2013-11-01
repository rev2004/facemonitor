/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Written (W) 2013 Weihao Cheng
 * Copyright (C) 2013 Weihao Cheng
 */

#include "CRClassifier.h"




static cv::Mat asRowMatrix(cv::InputArrayOfArrays src, int rtype, double alpha=1, double beta=0) {
    // make sure the input data is a vector of matrices or vector of vector
    if(src.kind() != cv::_InputArray::STD_VECTOR_MAT && src.kind() != cv::_InputArray::STD_VECTOR_VECTOR) {
        std::string error_message = "The data is expected as InputArray::STD_VECTOR_MAT (a std::vector<Mat>) or _InputArray::STD_VECTOR_VECTOR (a std::vector< vector<...> >).";
        CV_Error(CV_StsBadArg, error_message);
    }
    // number of samples
    size_t n = src.total();
    // return empty matrix if no matrices given
    
    if(n == 0)
        return cv::Mat();
    // dimensionality of (reshaped) samples
    size_t d = src.getMat(0).total();
    // create data matrix
    cv::Mat data((int)n, (int)d, rtype);
    // now copy data
    for(unsigned int i = 0; i < n; i++) {
        // make sure data can be reshaped, throw exception if not!
        if(src.getMat(i).total() != d) {
            std::string error_message = cv::format("Wrong number of elements in matrix #%d! Expected %d was %d.", i, d, src.getMat(i).total());
            CV_Error(CV_StsBadArg, error_message);
        }
        // get a hold of the current row
        cv::Mat xi = data.row(i);
        // make reshape happy by cloning for non-continuous matrices
        if(src.getMat(i).isContinuous()) {
            src.getMat(i).reshape(1, 1).convertTo(xi, rtype, alpha, beta);
        } else {
            src.getMat(i).clone().reshape(1, 1).convertTo(xi, rtype, alpha, beta);
        }
    }
    return data;
}


// Removes duplicate elements in a given vector.
template<typename _Tp>
inline std::vector<_Tp> remove_dups(const std::vector<_Tp>& src) {
    typedef typename std::set<_Tp>::const_iterator constSetIterator;
    typedef typename std::vector<_Tp>::const_iterator constVecIterator;
    std::set<_Tp> set_elems;
    for (constVecIterator it = src.begin(); it != src.end(); ++it)
        set_elems.insert(*it);
    std::vector<_Tp> elems;
    for (constSetIterator it = set_elems.begin(); it != set_elems.end(); ++it)
        elems.push_back(*it);
    return elems;
}




void CRClassifier::train(cv::InputArrayOfArrays _src, cv::InputArray _local_labels) {
    if(_src.total() == 0) {
        return;
    } else if(_local_labels.getMat().type() != CV_32SC1) {
        return;
    }
    // make sure data has correct size
    if(_src.total() > 1) {
        for(int i = 1; i < static_cast<int>(_src.total()); i++) {
            if(_src.getMat(i-1).total() != _src.getMat(i).total()) {
                return;
            }
        }
    }
    // get labels
    cv::Mat labels = _local_labels.getMat();
    // observations in row
    cv::Mat data = asRowMatrix(_src, CV_64FC1);
    
    // number of samples
    int n = data.rows;
    // assert there are as much samples as labels
    if(static_cast<int>(labels.total()) != n) {
        return;
    }
    // clear existing model data
    _labels.clear();
    _projections.clear();
    
    // clip number of components to be valid
    // The number of components won't larger than data's rows or cols
    if((_num_components <= 0) || (_num_components > n))
        _num_components = n;
    
    // perform the PCA
    cv::PCA pca(data, cv::Mat(), CV_PCA_DATA_AS_ROW, _num_components);
    // copy the PCA results
    _mean = pca.mean.reshape(1,1); // store the mean vector
    _eigenvalues = pca.eigenvalues.clone(); // eigenvalues by row
    cv::transpose(pca.eigenvectors, _eigenvectors); // eigenvectors by column
    // store labels for prediction
    _labels = labels.clone();

    
    cv::Mat subspace = cv::subspaceProject(_eigenvectors, _mean, data);
    
    for(int i=0;i<subspace.rows;i++) {
        
        cv::Mat r = subspace.row(i);
        double sum = sqrt(cv::sum(r.mul(r))[0]);
        
        r = r / sum;
        
    }
    
    // n * k
    cv::Mat tsp = subspace;
    
    // k * n
    subspace = subspace.t();
       
    _crc_projection = (tsp * subspace + cv::Mat::eye(subspace.cols, subspace.cols, CV_64FC1)*0.001).inv() * tsp;
    
    //n*k
    _subspace = subspace.clone();
}

void CRClassifier::predict(cv::InputArray _src, int &minClass, double &minDist) const {
    // get data
    

    cv::Mat src = _src.getMat();
    // make sure the user is passing correct data
    if(_subspace.empty()) {
        // throw error if no data (or simply return -1?)
        printf("\nNo data");
        return;
    } else if(_eigenvectors.rows != static_cast<int>(src.total())) {
        // check data alignment just for clearer exception messages
        printf("\nWrong eigenvectors");
        return;
    }
    
    
    // project into PCA subspace
    cv::Mat y = cv::subspaceProject(_eigenvectors, _mean, src.reshape(1,1));
    
    double sy = sqrt(cv::sum(y.mul(y))[0]);
    
    y = y / sy;
    
    cv::Mat coef;
    
    cv::Mat ty;
    cv::transpose(y, ty);
    

    coef = _crc_projection * ty;

    int prev_l = -1;
    int start_i;
    

    double min_err = DBL_MAX;
    int min_class = -1;
    
    int i,e;
    for(i=0,e=(int)_labels.size();i<=e;i++) {
        
        int l = i < e ? _labels.at(i) : prev_l+1;
        if( l != prev_l ) {
            
            
            
            if( prev_l >= 0 ) {
                
                //std::cout<<prev_l<<std::endl;
        
                cv::Mat coef_c = coef(cv::Range(start_i,i-1),cv::Range(0,coef.cols));
                cv::Mat subspace_c = _subspace(cv::Range(0,_subspace.rows),cv::Range(start_i,i-1));
                
                cv::Mat r = subspace_c * coef_c;
                
                double sum = cv::sum(coef_c.mul(coef_c))[0];
                
                
                double err = pow(cv::norm(ty-r, cv::NORM_L2),2)/sum;
                
                if( err < min_err ) {
                    
                    min_err = err;
                    min_class = prev_l;
                }
            }
            start_i = i;
            
        }
        prev_l = l;
        
    }

    minClass = min_class;
    minDist = min_err;
}





