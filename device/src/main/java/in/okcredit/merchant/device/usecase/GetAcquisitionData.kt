package `in`.okcredit.merchant.device.usecase

import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.merchant.device.ReferrerSource
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetAcquisitionData @Inject constructor(private val deviceRepository: Lazy<DeviceRepository>) {
    fun execute(): Single<Map<String, String>> {

        return deviceRepository.get().getReferrals()
            .map { referrerList ->
                val referral = referrerList.filter { referrer ->
                    referrer.source == ReferrerSource.APPS_FLYER.value
                }[0]
                val appsFlyerValue = trim(referral.value)
                val map = appsFlyerValue.split(",")
                    .map { it.split("=") }
                    .map { it.first() to it.last().toString() }
                    .toMap()
                map
            }
    }

    private fun trim(value: String): String {
        return value.replace("{", "").replace("}", "").replace("\\s".toRegex(), "")
    }
}
