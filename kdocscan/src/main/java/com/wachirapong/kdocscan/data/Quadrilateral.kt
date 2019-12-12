package com.wachirapong.kdocscan.data

import org.opencv.core.MatOfPoint
import org.opencv.core.Point

data class Quadrilateral(var contour: MatOfPoint?, var points: ArrayList<Point>?)