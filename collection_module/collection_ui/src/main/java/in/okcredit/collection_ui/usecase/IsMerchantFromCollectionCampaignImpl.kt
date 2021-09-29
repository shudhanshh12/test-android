package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.IsCollectionCampaignMerchant
import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.merchant.device.Referrer
import `in`.okcredit.merchant.device.ReferrerSource
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import android.net.Uri
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import io.reactivex.functions.Function3
import kotlinx.coroutines.rx2.asObservable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.app_contract.AppConstants
import javax.inject.Inject

@Reusable
class IsMerchantFromCollectionCampaignImpl @Inject constructor(
    private val deviceRepository: Lazy<DeviceRepository>,
    private val preferences: Lazy<DefaultPreferences>,
    private val ab: AbRepository,
) : IsCollectionCampaignMerchant {

    companion object {
        const val INDEX = 0
        const val UTM_CAMPAIGN = "utm_campaign"
        const val QUESTION_MARK = "?"
        const val CUSTOMER_COLLECTION = "customer_collection"
    }

    override fun execute(): Observable<Boolean> {

        return Observable.zip(
            ab.isFeatureEnabled(CUSTOMER_COLLECTION).onErrorReturnItem(false),
            preferences.get().getBoolean(OnboardingPreferences.KEY_NEW_USER, Scope.Individual).asObservable(),
            getPlayStoreReferrer(),
            Function3<Boolean, Boolean, Referrer?, Boolean> { isCustomerCollection, isNewUser, referrer ->
                val utmCampaign = Uri.parse("$QUESTION_MARK${referrer.value}").getQueryParameter(
                    UTM_CAMPAIGN
                )
                return@Function3 (isCustomerCollection || isNewUser) &&
                    utmCampaign == AppConstants.PAYMENT_INSTALL_LINK_UTM_CAMPAIGN
            }
        )
    }

    private fun getPlayStoreReferrer(): Observable<Referrer> {
        return deviceRepository.get().getReferrals()
            .flatMapObservable { referrerList ->
                val resReferrerList =
                    referrerList.filter { referrer -> referrer.source == ReferrerSource.PLAY_STORE.value }
                if (resReferrerList.isEmpty())
                    Observable.empty()
                else Observable.just(resReferrerList[INDEX])
            }
    }
}
