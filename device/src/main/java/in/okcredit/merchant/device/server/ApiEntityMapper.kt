package `in`.okcredit.merchant.device.server

import `in`.okcredit.merchant.device.Device
import `in`.okcredit.merchant.device.Referrer

fun Device.toApiModel(): ApiMessages.Device {
    return ApiMessages.Device(
        name = id,
        version_code = versionCode,
        api_level = apiLevel,
        fcm_token = fcmToken,
        aa_id = aaid,
        referrers = referrers.toApiModel()
    )
}

fun List<Referrer>.toApiModel(): List<ApiMessages.Referrer> {
    return this.map {
        ApiMessages.Referrer(it.source, it.value)
    }
}
