package `in`.okcredit.merchant.device.server

import androidx.annotation.Keep

interface ApiMessages {

    @Keep
    data class Device(
        val name: String,
        val version_code: Int,
        val api_level: Int,
        val fcm_token: String?,
        val aa_id: String?,
        val referrers: List<Referrer?>
    )

    @Keep
    data class CreateOrUpdateDeviceRequest(
        val device: Device
    )

    @Keep
    data class Referrer(
        val source: String,
        val referrer: String
    )
}
