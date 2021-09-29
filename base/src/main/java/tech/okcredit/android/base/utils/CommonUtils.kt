package tech.okcredit.android.base.utils

import android.content.res.Resources
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.lang.Exception
import java.net.URLDecoder

/****************************************************************
 * Log utils
 ****************************************************************/

fun Throwable.getStringStackTrace(): String {
    return try {
        val writer: Writer = StringWriter()
        this.printStackTrace(PrintWriter(writer))
        writer.toString()
    } catch (e: Exception) {
        ""
    }
}

fun getDecodedUrl(encodedUrl: String): String? {
    var url = ""
    try {
        url = URLDecoder.decode(encodedUrl, "UTF-8")
    } catch (e: Exception) { }

    return url
}

fun Int.convertToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

infix fun <T1, T2, T3> Pair<T1, T2>.to(t3: T3): Triple<T1, T2, T3> {
    return Triple(this.first, this.second, t3)
}
