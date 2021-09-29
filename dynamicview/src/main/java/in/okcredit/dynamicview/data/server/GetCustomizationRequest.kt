package `in`.okcredit.dynamicview.data.server

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetCustomizationRequest(
    @Json(name = "version_code")
    val versionCode: String,
    @Json(name = "lang")
    val language: String
)
