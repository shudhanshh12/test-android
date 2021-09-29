package `in`.okcredit.merchant.core.common

import `in`.okcredit.shared.utils.CommonUtils

object TimestampUtils {
    fun currentTimestamp() = CommonUtils.currentDateTime().millis.toTimestamp()
}
