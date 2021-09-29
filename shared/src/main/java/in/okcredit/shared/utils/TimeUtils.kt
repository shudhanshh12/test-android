package `in`.okcredit.shared.utils

import java.util.concurrent.TimeUnit

object TimeUtils {
    fun Int.toMillis() = TimeUnit.SECONDS.toMillis(this.toLong())
    fun Long.toSeconds() = TimeUnit.MILLISECONDS.toSeconds(this)
}
