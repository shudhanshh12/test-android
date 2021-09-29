package `in`.okcredit.merchant.device.usecase

import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.merchant.device.Referrer
import `in`.okcredit.merchant.device.ReferrerSource
import android.net.Uri
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class IsCollectionCampaign @Inject constructor(
    private val deviceRepository: Lazy<DeviceRepository>
) {

    companion object {
        const val INDEX = 0
        const val UTM_CAMPAIGN = "utm_campaign"
        const val QUESTION_MARK = "?"
        const val PAYMENT_INSTALL_LINK_UTM_CAMPAIGN = "customer_collection"
    }

    fun execute(): Single<Boolean> {
        return getPlayStoreReferrer()
            .map {
                val utmParams = it.value
                val utmCampaign = Uri.parse("$QUESTION_MARK$utmParams").getQueryParameter(
                    UTM_CAMPAIGN
                )
                utmCampaign == PAYMENT_INSTALL_LINK_UTM_CAMPAIGN
            }
    }

    private fun getPlayStoreReferrer(): Single<Referrer?> {
        return deviceRepository.get().getReferrals()
            .map { referrerList ->
                referrerList.filter { referrer -> referrer.source == ReferrerSource.PLAY_STORE.value }[INDEX]
            }
    }
}
