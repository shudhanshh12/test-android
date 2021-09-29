package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.customer.contract.BulkReminderModel
import `in`.okcredit.customer.contract.GetBannerForBulkReminder
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.store.database.BulkReminderDbInfo
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class GetBannerForBulkReminderImpl @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val ab: Lazy<AbRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetBannerForBulkReminder {

    companion object {
        private const val FIREBASE_DEFAULTED_SINCE_KEY = "bulk_reminder_v2_defaulted_since"
        private const val FEATURE_BULK_REMINDER = "bulk_reminder"
    }

    override fun execute(): Flow<BulkReminderModel> {
        val defaulterSince = firebaseRemoteConfig.get().getString(FIREBASE_DEFAULTED_SINCE_KEY)
        val defaultedSinceInDay = "-$defaulterSince day"

        return isFeatureEnabled().flatMapLatest { enabled ->
            if (enabled) {
                val businessId = getActiveBusinessId.get().execute().await()
                customerRepo.get().getDefaultersDataForBanner(defaultedSinceInDay, businessId)
                    .map { it.convertToBulkReminderModel(defaulterSince.toInt()) }
            } else {
                flowOf(
                    BulkReminderModel(
                        canShowBanner = false,
                        canShowNotificationIcon = false
                    )
                )
            }
        }
    }

    private fun isFeatureEnabled(): Flow<Boolean> {
        return ab.get().isFeatureEnabled(FEATURE_BULK_REMINDER).asFlow()
    }
}

internal fun BulkReminderDbInfo.convertToBulkReminderModel(defaulterSince: Int): BulkReminderModel =
    BulkReminderModel(
        this.totalBalanceDue < 0,
        this.totalBalanceDue,
        this.countNumberOfCustomers > 0,
        this.countNumberOfCustomers,
        defaulterSince,
        totalCustomers = this.totalCustomers
    )
