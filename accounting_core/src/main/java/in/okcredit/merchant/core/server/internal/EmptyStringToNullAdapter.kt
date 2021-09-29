package `in`.okcredit.merchant.core.server.internal

import com.google.common.base.Strings
import com.squareup.moshi.FromJson

object EmptyStringToNullAdapter {
    @FromJson
    fun fromJson(data: String?) = Strings.emptyToNull(data)
}
