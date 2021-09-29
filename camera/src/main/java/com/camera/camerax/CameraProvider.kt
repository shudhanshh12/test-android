package com.camera.camerax

import androidx.camera.core.CameraSelector
import androidx.fragment.app.FragmentManager

object CameraProvider {
    private var cameraXFrag: CameraXFragment? = null
    fun getInstance(): CameraProvider? {
        cameraXFrag = CameraXFragment.getInstance()
        return this
    }

    fun beginCamera(supportFragmentManager: FragmentManager, containerId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(containerId, cameraXFrag!!)
            .commitAllowingStateLoss()
    }

    fun onFlashClicked(onFlashClicked: Boolean) {
        cameraXFrag?.onFlashClicked(onFlashClicked)
    }

    fun setCanCaptureImage(canCaptureImage: Boolean) {
        cameraXFrag?.canCaptureImage = canCaptureImage
    }

    fun flipCamera() {
        cameraXFrag?.flipCamera()
    }

    fun isBackLens(): Boolean {
        return cameraXFrag?.lensFacing == CameraSelector.LENS_FACING_BACK
    }

    fun isFlashOFF(): Boolean? {
        return cameraXFrag?.isFlashEnable
    }
}
