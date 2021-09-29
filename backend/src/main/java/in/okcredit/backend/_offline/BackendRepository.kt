package `in`.okcredit.backend._offline

import `in`.okcredit.accounting_core.contract.QuickAddCustomerModel
import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.server.internal.QuickAddTransactionResponse
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddCustomerRequestModel
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionModel
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionRequest
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.rxjava.SchedulerProvider
import javax.inject.Inject

class BackendRepository @Inject constructor(
    private val remoteSource: Lazy<BackendRemoteSource>,
    private val schedulerProvider: Lazy<SchedulerProvider>
) {

    fun quickAddTransaction(
        customerModel: QuickAddCustomerModel,
        amount: Long,
        type: Transaction.Type,
        profileImageUploadUrl: String?,
        businessId: String,
    ): Single<QuickAddTransactionResponse> {
        val customer = QuickAddCustomerRequestModel(customerModel.name, customerModel.mobile, profileImageUploadUrl)
        val transaction = QuickAddTransactionModel(amount, type.code)
        val request = QuickAddTransactionRequest(businessId, customer, transaction)
        return remoteSource.get().quickAddTransaction(request, businessId).subscribeOn(schedulerProvider.get().io())
    }
}
