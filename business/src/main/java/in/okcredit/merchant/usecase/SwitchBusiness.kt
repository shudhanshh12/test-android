package `in`.okcredit.merchant.usecase

import `in`.okcredit.analytics.SuperProperties
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.contract.SetActiveBusinessId
import `in`.okcredit.voice_first.contract.ResetDraftTransactions
import android.app.Activity
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import tech.okcredit.account_chat_contract.SignOutFirebaseAndRemoveChatListener
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.app_contract.LegacyNavigator
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SwitchBusiness @Inject constructor(
    private val setActiveBusinessId: Lazy<SetActiveBusinessId>,
    private val tracker: Lazy<Tracker>,
    private val legacyNavigator: Lazy<LegacyNavigator>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val signOutFirebaseAndRemoveChatListener: Lazy<SignOutFirebaseAndRemoveChatListener>,
    private val resetDraftTransactions: Lazy<ResetDraftTransactions>,
) {
    suspend fun execute(businessId: String, businessName: String, weakActivity: WeakReference<Activity>? = null) {
        setActiveBusinessId(businessId, businessName)
        cleanUpCacheAndMisc()
        navigateToHomeScreen(weakActivity)
    }

    private suspend fun setActiveBusinessId(businessId: String, businessName: String) {
        setActiveBusinessId.get().execute(businessId).await()
        tracker.get().setIdentity(businessId, false)
        tracker.get().setUserProperties(businessId, businessName)
        tracker.get().setSuperProperties(SuperProperties.MERCHANT_ID, businessId)
    }

    private suspend fun cleanUpCacheAndMisc() {
        signOutFirebaseAndRemoveChatListener.get().execute()
            .timeout(3, TimeUnit.SECONDS).onErrorComplete().await()
        resetDraftTransactions.get().execute()
    }

    private suspend fun navigateToHomeScreen(weakActivity: WeakReference<Activity>?) {
        weakActivity?.get()?.let { activity ->
            withContext(dispatcherProvider.get().main()) {
                legacyNavigator.get().goToHome(activity)
                activity.finishAffinity()
            }
        }
    }
}
