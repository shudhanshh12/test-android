package tech.okcredit.base.exceptions

import com.google.firebase.crashlytics.FirebaseCrashlytics
import tech.okcredit.base.network.asNetworkError

@Deprecated(message = "Please Use RecordException")
class ExceptionUtils {

    companion object {

        private const val ERROR_TYPE_RXJAVA_UNHANDLE = "RxJava UnHandle Error"

        fun logException(exception: Exception) {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }

        fun logException(msg: String, exception: Exception) {
            if (exception.isNetworkError().not()) {
                logException(Exception(msg, exception))
            }
        }

        fun log(msg: String) {
            FirebaseCrashlytics.getInstance().log(msg)
        }

        fun setUserIdentifier(id: String) {
            FirebaseCrashlytics.getInstance().setUserId(id)
        }

        fun logUsecaseError(exception: Exception) {
            if (exception.isNetworkError().not() && exception.cause?.isNetworkError()?.not() != false) {
                logException(exception)
            }
        }

        fun logRxUnHandleError(exception: Exception) {
            if (exception.isNetworkError().not()) {
                logException(Exception(ERROR_TYPE_RXJAVA_UNHANDLE, exception))
            }
        }

        fun Throwable.isNetworkError(): Boolean {
            return (this.asNetworkError() != null || this.cause.asNetworkError() != null)
        }

        fun logException(msg: String, throwable: Throwable) {
            logException(msg, java.lang.Exception(throwable))
        }
    }
}
