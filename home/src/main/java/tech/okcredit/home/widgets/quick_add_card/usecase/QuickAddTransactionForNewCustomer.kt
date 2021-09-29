package tech.okcredit.home.widgets.quick_add_card.usecase

import `in`.okcredit.accounting_core.contract.QuickAddCustomerModel
import `in`.okcredit.backend._offline.BackendRepository
import `in`.okcredit.backend._offline.common.CoreModuleMapper
import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.database.DueInfoRepo
import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend._offline.database.internal.DbEntities
import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.backend._offline.server.internal.ApiEntityMapper
import `in`.okcredit.backend._offline.serverV2.internal.toDomainModel
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages
import `in`.okcredit.merchant.core.server.toCustomer
import `in`.okcredit.merchant.core.server.toTransaction
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

class QuickAddTransactionForNewCustomer @Inject constructor(
    private val coreSdk: Lazy<CoreSdk>,
    private val backendRepository: Lazy<BackendRepository>,
    private val customerRepo: Lazy<CustomerRepo>,
    private val dueInfoRepo: Lazy<DueInfoRepo>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val uploadFile: Lazy<IUploadFile>,
    private val transactionRepo: Lazy<TransactionRepo>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(
        customerModel: QuickAddCustomerModel,
        amount: Long,
        type: Transaction.Type,
    ): Observable<Result<Unit>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                isCoreSdkEnabled(businessId).flatMap { isEnabled ->
                    if (isEnabled) {
                        coreQuickAddTransaction(customerModel, amount, type, businessId)
                    } else {
                        backendQuickAddTransaction(customerModel, amount, type, businessId)
                    }
                }.toObservable()
            }
        )
    }

    private fun isCoreSdkEnabled(businessId: String) = coreSdk.get().isCoreSdkFeatureEnabled(businessId)

    private fun coreQuickAddTransaction(
        customerModel: QuickAddCustomerModel,
        amount: Long,
        type: Transaction.Type,
        businessId: String,
    ): Single<Unit> {
        val profileImageUploadUrl = getProfileImageUploadUrl(customerModel)
        return coreSdk.get()
            .quickAddTransaction(customerModel, amount, type, profileImageUploadUrl, businessId)
            .flatMap { response ->
                val customerDatabaseEntity = CoreModuleMapper.toCustomer(response.apiCustomer.toCustomer())
                val transactionDatabaseEntity = response.transaction.toTransaction()
                coreAddCustomer(response.apiCustomer, businessId)
                    .andThen(uploadProfileImage(customerModel, profileImageUploadUrl))
                    .andThen(insertDueInfo(customerDatabaseEntity, businessId))
                    .andThen(syncSupplierEnabledCustomerIds(businessId))
                    .andThen(coreAddTransaction(transactionDatabaseEntity, businessId))
                    .andThen(deleteCollectionShareInfo(transactionDatabaseEntity.customerId))
                    .andThen(Single.just(Unit))
            }
    }

    private fun coreAddCustomer(apiCustomer: CoreApiMessages.ApiCustomer, businessId: String) =
        coreSdk.get().putCustomer(apiCustomer.toCustomer(), businessId)

    private fun coreAddTransaction(transaction: Transaction, businessId: String) = coreSdk.get().putTransaction(transaction, businessId)

    private fun backendQuickAddTransaction(
        customerModel: QuickAddCustomerModel,
        amount: Long,
        type: Transaction.Type,
        businessId: String,
    ): Single<Unit> {
        val profileImageUploadUrl = getProfileImageUploadUrl(customerModel)
        return backendRepository.get()
            .quickAddTransaction(customerModel, amount, type, profileImageUploadUrl, businessId)
            .flatMap { response ->
                val customerDatabaseEntity = ApiEntityMapper.CUSTOMER.convert(response.customer)!!
                val transactionDatabaseEntity = response.transaction.toDomainModel(businessId)
                backendAddCustomer(customerDatabaseEntity, businessId)
                    .andThen(uploadProfileImage(customerModel, profileImageUploadUrl))
                    .andThen(insertDueInfo(customerDatabaseEntity, businessId))
                    .andThen(syncSupplierEnabledCustomerIds(businessId))
                    .andThen(backendAddTransaction(transactionDatabaseEntity))
                    .andThen(deleteCollectionShareInfo(transactionDatabaseEntity.customerId))
                    .andThen(Single.just(Unit))
            }
    }

    private fun backendAddCustomer(customer: Customer, businessId: String) = customerRepo.get().putCustomer(customer, businessId)

    private fun backendAddTransaction(transaction: DbEntities.Transaction) =
        transactionRepo.get().putTransactionV2(transaction)

    private fun uploadProfileImage(customerModel: QuickAddCustomerModel, profileImageUploadUrl: String?): Completable {
        return if (customerModel.profileImage.isNullOrBlank() || profileImageUploadUrl.isNullOrBlank()) {
            Completable.complete()
        } else {
            uploadFile.get().schedule(IUploadFile.CONTACT_PHOTO, profileImageUploadUrl, customerModel.profileImage!!)
        }
    }

    private fun insertDueInfo(customer: Customer, businessId: String) = dueInfoRepo.get()
        .insertDueInfo(DueInfo(customer.id, false, null, false), businessId)

    private fun syncSupplierEnabledCustomerIds(businessId: String) =
        supplierCreditRepository.get().scheduleSyncSupplierEnabledCustomerIds(businessId)

    private fun deleteCollectionShareInfo(customerId: String) =
        collectionRepository.get().deleteCollectionShareInfoOfCustomer(customerId)

    private fun getProfileImageUploadUrl(customerModel: QuickAddCustomerModel) =
        if (customerModel.profileImage.isNullOrBlank()) {
            null
        } else {
            IUploadFile.AWS_RECEIPT_BASE_URL + "/" + UUID.randomUUID() + ".jpg"
        }
}
