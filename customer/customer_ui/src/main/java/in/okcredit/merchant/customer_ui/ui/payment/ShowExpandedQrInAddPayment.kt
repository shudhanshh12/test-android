package `in`.okcredit.merchant.customer_ui.ui.payment

import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import javax.inject.Inject

class ShowExpandedQrInAddPayment @Inject constructor(
    private val customerRepositoryImpl: Lazy<CustomerRepositoryImpl>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
) {

    suspend fun execute(): Boolean {
        val maxCount = firebaseRemoteConfig.get().getLong(FRC_MAX_EXPANDED_QR_SHOWN_COUNT)
        val current = customerRepositoryImpl.get().getExpandedQrShownCount()
        if (current < maxCount) {
            customerRepositoryImpl.get().setExpandedQrShownCount(current + 1)
            return true
        }

        return false
    }

    companion object {
        const val FRC_MAX_EXPANDED_QR_SHOWN_COUNT = "max_expanded_qr_shown_count"
    }
}
