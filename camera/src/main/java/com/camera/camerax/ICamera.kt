package com.camera.camerax

import com.camera.models.models.Picture

interface ICamera {

    fun goBack()

    fun onCameraCapturedImage(picture: Picture)
}
