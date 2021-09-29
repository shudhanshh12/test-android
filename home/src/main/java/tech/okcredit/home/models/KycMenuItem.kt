package tech.okcredit.home.models

import `in`.okcredit.collection.contract.KycRisk
import `in`.okcredit.collection.contract.KycStatus

sealed class KycMenuItem {
    object Unavailable : KycMenuItem()

    data class Available(val kycStatus: KycStatus, val kycRisk: KycRisk) : KycMenuItem()
}
