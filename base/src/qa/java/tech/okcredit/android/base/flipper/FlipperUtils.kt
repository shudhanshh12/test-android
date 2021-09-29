package tech.okcredit.android.base.flipper

import android.content.Context
import okhttp3.Interceptor

object FlipperUtils : FlipperUtilsInterface {
    override var networkInterceptor: Interceptor? = null

    override fun initFlipper(context: Context) {
    }
}
