package `in`.okcredit.merchant.core.common

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Timestamp(
    val epoch: Long
) {
    val seconds: Long
        get() = if (epoch.toString().length == 13) (epoch / 1000) else epoch
}

fun Long.toTimestamp(): Timestamp {
    var millis = this
    if (millis.toString().length == 10) millis *= 1000
    return Timestamp(millis)
}

fun String.toTimestamp(): Timestamp {
    try {
        var millis = this.toLong()
        if (this.length == 10) millis *= 1000
        return Timestamp(millis)
    } catch (e: Exception) {
        throw CoreException.IllegalArgumentException
    }
}
