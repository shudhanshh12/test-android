package `in`.okcredit.individual.contract.model

import org.joda.time.DateTime

data class Individual(
    val id: String,
    val createTime: DateTime?,
    val mobile: String,
    val email: String?,
    val registerTime: DateTime?,
    val lang: String?,
    val displayName: String?,
    val profileImage: String?,
    val addressText: String?,
    val longitude: Double?,
    val latitude: Double?,
    val about: String?,
)
