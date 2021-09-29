package tech.okcredit.android.base.flipper

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import okhttp3.Interceptor
import timber.log.Timber

object FlipperUtils : FlipperUtilsInterface {
    var isFlipperInstantiated: Boolean = false
    override var networkInterceptor: Interceptor? = null

    @Synchronized
    override fun initFlipper(context: Context) {
        if (FlipperUtils.shouldEnableFlipper(context) && !isFlipperInstantiated) {
            Timber.v("<<<<Flipper initializing")
            isFlipperInstantiated = true

            // init
            SoLoader.init(context, false)
            val client = AndroidFlipperClient.getInstance(context)

            // add layout inspector plugin
            client.addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))

            // add network inspector plugin
            val networkPlugin = NetworkFlipperPlugin()
            networkInterceptor = FlipperOkhttpInterceptor(networkPlugin)
            client.addPlugin(networkPlugin)

            // start flipper client
            client.start()
            Timber.v("<<<<Flipper initialized")
        }
    }
}
