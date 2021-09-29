package com.camera.camerax

import android.content.Context
import java.io.File

const val ANIMATION_FAST_MILLIS = 50L
const val ANIMATION_SLOW_MILLIS = 50L

object CameraUtils {
    fun getFilePath(context: Context?): File {
        return File(
            context?.getExternalFilesDir(null), System.currentTimeMillis().toString() + "pic.jpg"
        )
    }
}
