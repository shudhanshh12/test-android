package tech.okcredit.android.ab.server

import androidx.annotation.Keep

@Keep
data class Profile(
    val merchant_id: String,
    val features: Map<String, Boolean>,
    val experiments: Map<String, Experiment>
)

@Keep
data class Experiment(
    val name: String,
    val status: Int,
    val variant: String,
    val vars: Map<String, String>
)

@Keep
data class GetProfileResponse(
    val profile: Profile
)

@Keep
data class AcknowledgementRequest(
    val device_id: String,
    val type: Int,
    val time: Long,
    val experiments: List<Experiment>
)
