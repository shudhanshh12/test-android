package tech.okcredit.okstream.usecase

import android.content.Context
import dagger.Lazy
import merchant.android.okstream.sdk.OkStreamSdk
import javax.inject.Inject

class OkStreamDisconnect @Inject constructor(
    private val okStreamSdk: Lazy<OkStreamSdk>,
) {
    fun execute(context: Context) {
        okStreamSdk.get().disconnect(context)
    }
}
