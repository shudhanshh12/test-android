package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Completable
import merchant.okcredit.supplier.contract.IsNetworkReminderEnabled
import merchant.okcredit.supplier.contract.PutNotificationReminder
import timber.log.Timber
import javax.inject.Inject

class PutNotificationReminderImpl @Inject constructor(
    private val isNetworkReminderEnabled: Lazy<IsNetworkReminderEnabled>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : PutNotificationReminder {

    companion object {
        const val TAG = "PutNotificationReminder"
    }

    override fun execute(accountId: String): Completable {
        return isNetworkReminderEnabled.get().execute()
            .flatMapCompletable { enabled ->
                if (enabled) {
                    getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                        supplierCreditRepository.get().createNotificationReminder(accountId, businessId)
                            .flatMapCompletable {
                                Timber.i("$TAG notification created -> $it")
                                Completable.complete()
                            }
                    }
                } else {
                    Timber.i("$TAG notification reminder not enabled")
                    Completable.complete()
                }
            }
    }
}
