package `in`.okcredit.onboarding.marketing

import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.onboarding.contract.marketing.AppsFlyerHelper
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import dagger.Lazy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.awaitFirstOrNull
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.utils.DateTimeUtils.currentDateTime
import javax.inject.Inject

class AppsFlyerHelperImpl @Inject constructor(
    private val marketingRepository: Lazy<MarketingRepository>,
    private val onboardingPreferencesImpl: Lazy<OnboardingPreferencesImpl>,
    private val deviceRepository: Lazy<DeviceRepository>,
) : AppsFlyerHelper {

    var aaid: String? = null
        private set
    var mediaSource: String? = null
        private set
    var campaign: String? = null
        private set

    override suspend fun setAuthSuccess(isAuthSignup: Boolean) = withContext(Dispatchers.IO) {
        onboardingPreferencesImpl.get().setMarketingIsSignUp(isAuthSignup)
        onboardingPreferencesImpl.get().setMarketingAuthTime(currentDateTime().millis)

        if (aaid != null && mediaSource != null && campaign != null) {
            reportMarketingData()
        }
    }

    override suspend fun setPreProcessedAppsflyerData(appsFlyerData: Map<String, Any?>) = withContext(Dispatchers.IO) {
        aaid = deviceRepository.get().getDevice().awaitFirstOrNull()?.aaid ?: ""
        mediaSource = (appsFlyerData["media_source"] as? String) ?: ""
        campaign = (appsFlyerData["c"] as? String) ?: ""

        with(onboardingPreferencesImpl.get()) {
            if (containsMarketingIsSignUp() && containsMarketingAuthTime()) {
                reportMarketingData()
            }
        }
    }

    private suspend fun reportMarketingData() {
        try {
            val isSignup = onboardingPreferencesImpl.get().getMarketingIsSignUp()
            val authTime = onboardingPreferencesImpl.get().getMarketingAuthTime()

            marketingRepository.get().reportMarketingData(aaid!!, isSignup, authTime, mediaSource!!, campaign!!)

            // Only clean login data, as appsflyer data is not fetched again on logout + login
            onboardingPreferencesImpl.get().removeMarketingIsSignUp()
            onboardingPreferencesImpl.get().removeMarketingAuthTime()
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            // do nothing
        }
    }

    override fun toAppsFlyerData(data: Map<String, Any?>): MutableMap<String, String>? {
        return data
            .takeIf { data[IS_FIRST_LAUNCH] as Boolean }
            ?.mapNotNull { (key, value) ->
                mappedAppsFlyerKeys[key]
                    ?.let { it to (value?.toString() ?: "") }
            }?.toMap(HashMap())
    }

    override fun getInstallRefererData(data: String): MutableMap<String, String> {
        // data = af_tranid=wNn4FT2VNEgzzzWlhpU77w&pid=facebook_inttTransfer&c=Campaign_MoneyTransfer&advertising_id=503995db-44e9-4280-b361-00260cde26d2&af_web_id=48cd6304-1f82-4a55-9d73-9ab9f013582d-c
        return data.split("&")
            .map { it.split("=") }
            .map { it.first() to it.last().toString() }
            .mapNotNull { (key, value) ->
                mappedGoogleKeys[key]?.let { it to value }
            }.toMap(HashMap())
    }

    @Suppress("SpellCheckingInspection")
    private val mappedAppsFlyerKeys = hashMapOf(
        "media_source" to "ad_partner_name",
        "adgroup_id" to "adgroup_id",
        "adset" to "ad_set",
        "campaign_id" to "campaign_id",
        "advertising_id" to "advertising_id",
        "adset_id" to "adset_id",
        "c" to "campaign",
        "adgroup" to "adgroup",
        "af_siteid" to "af_siteid",
        "af_sub1" to "af_sub1",
        "af_sub2" to "af_sub2",
        "af_sub3" to "af_sub3",
        "af_sub4" to "af_sub4",
        "af_sub5" to "af_sub5",
    )

    @Suppress("SpellCheckingInspection")
    private val mappedGoogleKeys = mapOf(
        "pid" to "ad_partner_name",
        "c" to "campaign",
        "advertising_id" to "advertising_id",
        "af_tranid" to "af_tranid",
        "af_web_id" to "af_web_id",
    )

    companion object {
        private const val IS_FIRST_LAUNCH = "is_first_launch"
    }
}
