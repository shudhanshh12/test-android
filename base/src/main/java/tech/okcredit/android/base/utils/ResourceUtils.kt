package tech.okcredit.android.base.utils

import android.content.Context
import androidx.annotation.RawRes
import tech.okcredit.android.base.extensions.readRawFie
import javax.inject.Inject

class ResourceUtils @Inject constructor(private val context: Context) {

    fun getRawResource(@RawRes rawId: Int) = context.readRawFie(rawId)
}
