package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class IsEditTransactionAmountEnabled @Inject constructor(
    private val coreSdk: CoreSdk,
    private val transactionRepo: TransactionRepo,
    private val getCustomer: GetCustomer,
    private val showEditAmountABExperiment: ShowEditAmountABExperiment,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(transactionId: String): Single<Boolean> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            Observable.zip(
                coreSdk.isCoreSdkFeatureEnabled(businessId).toObservable(),
                showEditAmountABExperiment.execute(),
                BiFunction { isCoreSdkFeatureEnabled, isEditAmountExpEnabled ->
                    return@BiFunction if (isCoreSdkFeatureEnabled && isEditAmountExpEnabled) {
                        isEditableTransaction(transactionId, businessId).blockingFirst()
                    } else {
                        false
                    }
                }
            ).firstOrError()
        }
    }

    private fun isEditableTransaction(transactionId: String, businessId: String): Observable<Boolean> {
        return transactionRepo.getTransaction(transactionId, businessId)
            .flatMap { transaction ->
                if (isEditableTransaction(transaction)) {
                    checkCustomerNotBlocked(transaction)
                } else {
                    Observable.just(false)
                }
            }
    }

    private fun checkCustomerNotBlocked(transaction: merchant.okcredit.accounting.model.Transaction): Observable<Boolean> {
        return getCustomer.execute(transaction.customerId)
            .flatMap {
                if (it.status == Customer.State.ACTIVE.value && it.isBlockedByCustomer().not()) {
                    Observable.just(true)
                } else {
                    Observable.just(false)
                }
            }
    }

    private fun isEditableTransaction(transaction: merchant.okcredit.accounting.model.Transaction) =
        transaction.collectionId.isNullOrBlank() && transaction.isDeleted.not() && transaction.isCreatedByCustomer.not()
}
