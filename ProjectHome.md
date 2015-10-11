Team P-unhard, Weihao Cheng

Requirements:  Windows 7 64bit with JRE1.7

The project consists of two parts, the Java interface and native dynamic library. Java interface part named _FaceMonitor_ is built by Eclipse Juno with JDK 1.7. The native dynamic library part named _JNICheng_ is built by Microsoft Visual Studio 2010 with OpenCV 2.4.5. Most of the core algorithms are written by C++ and integrated into _JNICheng_.

This face recognition system has two main functions: Face Login and Video Monitoring.

Face Login provides Register and Login to users. Register is considered to be the training procedure, which  grabs user's face images as training sample via Webcam and records the user profile as label. Login is considered to be the classification procedure, which grabs user's face image as test sample and then predicts its label. **Vivo detection** is performed after passing verification and before login. Users are required to open their mouth to unlock for preventing photo login case.

Video Monitoring uses similar recognition principles with Face Login. The difference is the materials are videos. A function called **Playback** is applied. It allows user to watch the entire recognition result playing with video progress.

The face recognition algorithm uses Local Binary Patterns as feature vector and NN classifier.  Collaborative Representing based Classification algorithm is also comprised in the project, but the image need to be aligned as condition.
