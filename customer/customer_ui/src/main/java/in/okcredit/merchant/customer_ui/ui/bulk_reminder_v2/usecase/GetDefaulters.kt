package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.backend._offline.common.DbReminderProfile
import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class GetDefaulters @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    companion object {
        private const val FIREBASE_DEFAULTED_SINCE_KEY = "bulk_reminder_v2_defaulted_since"
    }

    fun execute(): Flow<CustomersListForBulkReminder> {

        val defaulterSince = firebaseRemoteConfig.get().getString(FIREBASE_DEFAULTED_SINCE_KEY)
        val defaultedSinceInDay = "-$defaulterSince day"
        val getRemindersWhichAreNotSendToday = flow { emit(getActiveBusinessId.get().execute().await()) }
            .flatMapLatest { businessId ->
                customerRepo.get()
                    .getDefaultersForPendingReminders(businessId, defaultedSinceInDay)
            }

        val getRemindersWhichAreSendToday = flow { emit(getActiveBusinessId.get().execute().await()) }
            .flatMapLatest { businessId ->
                customerRepo.get()
                    .getDefaultersForTodaysReminders(businessId, defaultedSinceInDay)
            }

        return getRemindersWhichAreNotSendToday
            .zip(getRemindersWhichAreSendToday) { remindersWhichAreNotSendToday, remindersWhichAreSendToday ->
                CustomersListForBulkReminder(
                    remindersWhichAreNotSendToday,
                    remindersWhichAreSendToday
                )
            }
    }

    data class CustomersListForBulkReminder(
        val remindersWhichAreNotSendToday: List<DbReminderProfile>,
        val remindersWhichAreSendToday: List<DbReminderProfile>,
    )
}
