package tech.okcredit.base.network.utils

import tech.okcredit.android.base.error.check
import tech.okcredit.base.network.NetworkError

object NetworkHelper {
    fun isNetworkError(throwable: Throwable): Boolean = throwable.check<NetworkError>()
}
