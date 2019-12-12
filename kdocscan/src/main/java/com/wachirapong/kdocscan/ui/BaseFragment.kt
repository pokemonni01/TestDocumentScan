package com.wachirapong.kdocscan.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

abstract class BaseFragment: Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadOpenCV()
    }

    protected fun loadOpenCV() {
        if (!OpenCVLoader.initDebug()) {
            //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, getActivity().getApplicationContext(), mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    protected abstract fun onOpenCVConnected()
    protected abstract fun onOpenCVConnectionFailed()
    protected abstract fun onAfterViewCreated()

    private val mLoaderCallback = object : BaseLoaderCallback(activity) {
        override fun onManagerConnected(status: Int) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                onOpenCVConnected()
            } else {
                onOpenCVConnectionFailed()
            }
        }
    }
}