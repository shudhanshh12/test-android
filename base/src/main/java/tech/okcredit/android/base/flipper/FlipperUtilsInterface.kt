package tech.okcredit.android.base.flipper

import android.content.Context
import okhttp3.Interceptor

interface FlipperUtilsInterface {
    var networkInterceptor: Interceptor?

    fun initFlipper(context: Context)
}
