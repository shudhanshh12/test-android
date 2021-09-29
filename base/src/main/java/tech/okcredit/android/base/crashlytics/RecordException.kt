package tech.okcredit.android.base.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import tech.okcredit.base.exceptions.ExceptionUtils.Companion.isNetworkError
import tech.okcredit.base.network.ApiError

object RecordException {

    @JvmStatic
    fun recordException(exception: Exception) {
        if (exception.isNetworkError().not() && exception !is ApiError) {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }

    @JvmStatic
    fun recordException(throwable: Throwable) {
        recordException(Exception(throwable))
    }
}
