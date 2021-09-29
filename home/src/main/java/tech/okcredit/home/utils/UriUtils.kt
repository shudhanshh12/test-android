package tech.okcredit.home.utils

import android.net.Uri

object UriUtils {
    internal fun String.replaceLastSegmentWithValue(value: String): String? {
        val deeplinkUri = Uri.parse(this)
        return deeplinkUri.lastPathSegment?.let {
            deeplinkUri.toString().replace(it, value)
        }
    }
}
