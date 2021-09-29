package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Completable
import merchant.okcredit.supplier.contract.PutNotificationReminderAction
import javax.inject.Inject

class PutNotificationReminderActionImpl @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : PutNotificationReminderAction {
    override fun execute(notificationId: String, status: Int): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            supplierCreditRepository.get().updateNotificationReminderById(notificationId, status)
                .andThen(supplierCreditRepository.get().syncNotificationReminder(businessId))
        }
    }
}
