package com.wachirapong.kdocscan.ui.scanner


import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraX
import com.wachirapong.kdocscan.R
import com.wachirapong.kdocscan.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_scanner.*

class ScannerFragment : BaseFragment(), ScannerContract.View {

    companion object {
        fun initInstance() = ScannerFragment()
    }

    private val presenter = ScannerPresenter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.provideView(this)
        initView()
    }

    override fun onOpenCVConnected() {

    }

    override fun onOpenCVConnectionFailed() {

    }

    override fun onAfterViewCreated() {

    }

    override fun showAutoScan() {
        tvAutoScan?.text = getString(R.string.auto_scan)
    }

    override fun showManualScan() {
        tvAutoScan?.text = getString(R.string.manual_scan)
    }

    override fun onFlashTurnOn() {
        ivFlash?.setImageResource(R.drawable.ic_flash_off)
    }

    override fun onFlashTurnOff() {
        ivFlash?.setImageResource(R.drawable.ic_flash_on)
    }

    private fun initView() {
        context?.let {
            val isHasFlash = it.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
            if (isHasFlash) {
                ivFlash?.visibility = View.VISIBLE
            } else {
                ivFlash?.visibility = View.GONE
            }
        }
        tvCancel?.setOnClickListener { activity?.onBackPressed() }
        tvAutoScan?.setOnClickListener { presenter.toggleAutoScan() }
        ivFlash?.setOnClickListener { presenter.toggleFlash() }
    }
}
