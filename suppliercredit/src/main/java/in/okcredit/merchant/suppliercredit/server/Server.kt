package `in`.okcredit.merchant.suppliercredit.server

import `in`.okcredit.merchant.suppliercredit.AccountMetaInfo
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierLocalSource
import `in`.okcredit.merchant.suppliercredit.SupplierRemoteSource
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.merchant.suppliercredit.server.internal.ApiClient
import `in`.okcredit.merchant.suppliercredit.server.internal.ApiEntityMapper
import `in`.okcredit.merchant.suppliercredit.server.internal.ApiMessages
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import `in`.okcredit.merchant.suppliercredit.utils.Utils
import `in`.okcredit.shared.utils.AbFeatures
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import org.joda.time.DateTime
import retrofit2.Response
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.ThreadUtils.api
import tech.okcredit.android.base.utils.ThreadUtils.worker
import tech.okcredit.base.network.asError
import timber.log.Timber
import javax.inject.Inject

class ServerImpl @Inject constructor(
    private val apiClient: Lazy<ApiClient>,
    private val ab: Lazy<AbRepository>,
    private val store: Lazy<SupplierLocalSource>, // Added for fixing un-sync txns issue on 2.11.2. Remove Soon
) : SupplierRemoteSource {

    override fun addSupplier(name: String, mobile: String?, profileImage: String?, businessId: String): Single<Supplier> {
        return apiClient.get().addSupplier(ApiMessages.SupplierRequest(name, mobile, profileImage), businessId)
            .doOnEvent { t1, t2 -> Timber.i("SupplierThread addSupplier 2= ${Thread.currentThread()}") }
            .subscribeOn(api())
            .map { response ->
                if (response.isSuccessful) {
                    return@map response.body()
                } else {
                    val error = response.asError()

                    if (error.code == 409 && error.message == "cyclic_accounts_not_permitted") {
                        throw SupplierCreditServerErrors.ActiveCyclicAccount(null)
                    } else if (error.code == 400 && error.message == "invalid_mobile") {
                        throw SupplierCreditServerErrors.InvalidMobile()
                    } else {
                        throw error
                    }
                }
            }.map { ApiEntityMapper.SUPPLIER.convert(it) }
    }

    override fun updateSuppler(
        supplier: Supplier,
        txnAlertChanged: Boolean,
        state: Int,
        updateState: Boolean,
        businessId: String
    ): Completable {

        val sup = ApiMessages.Supplier(
            id = supplier.id,
            name = supplier.name,
            mobile = supplier.mobile,
            address = supplier.address,
            profile_image = supplier.profileImage,
            txn_alert_enabled = supplier.txnAlertEnabled,
            lang = supplier.lang,
            registered = supplier.registered,
            deleted = supplier.deleted,
            create_time = supplier.createTime,
            txn_start_time = supplier.txnStartTime,
            balance = supplier.balance,
            add_transaction_restricted = supplier.addTransactionRestricted,
            state = supplier.state,
            blocked_by_supplier = supplier.blockedBySupplier,
            restrict_contact_sync = supplier.restrictContactSync
        )

        return apiClient.get().updateSupplier(
            sup.id,
            ApiMessages.UpdateSupplierRequest(sup, txnAlertChanged, state, updateState),
            businessId
        )
            .doOnEvent { t1, t2 -> Timber.i("SupplierThread updateSuppler = ${Thread.currentThread()}") }
            .subscribeOn(api())
            .flatMapCompletable {
                if (it.isSuccessful) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    return@flatMapCompletable Completable.error(it.asError())
                }
            }
    }

    override fun deleteSupplier(supplierId: String, businessId: String): Completable {
        return apiClient.get()
            .deleteSupplier(supplierId, businessId)
            .doOnEvent { t1, t2 -> Timber.i("SupplierThread deleteSupplier = ${Thread.currentThread()}") }
            .subscribeOn(api())
            .flatMapCompletable {
                if (it.isSuccessful) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    return@flatMapCompletable Completable.error(it.asError())
                }
            }
    }

    override fun getSupplier(supplierId: String, businessId: String): Single<Supplier> {
        return apiClient.get().getSupplier(supplierId, businessId)
            .doOnEvent { t1, t2 -> Timber.i("SupplierThread getSupplier = ${Thread.currentThread()}") }
            .subscribeOn(api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful) {
                    return@map it.body()
                } else {
                    throw it.asError()
                }
            }
            .map { ApiEntityMapper.SUPPLIER.convert(it) }
    }

    override fun getSuppliers(businessId: String): Single<List<Supplier>> {
        return apiClient.get().getSuppliers(businessId)
            .subscribeOn(api())
            .map {
                if (it.isSuccessful) {
                    return@map it.body()?.suppliers
                } else {
                    throw it.asError()
                }
            }
            .map {
                Utils.mapList(it, ApiEntityMapper.SUPPLIER)
            }
    }

    override fun addTransaction(transaction: Transaction, businessId: String): Single<Transaction> {
        val txn = ApiMessages.Transaction(
            id = transaction.id,
            supplier_id = transaction.supplierId,
            collection_id = transaction.collectionId,
            payment = transaction.payment,
            amount = transaction.amount,
            note = transaction.note,
            receipt_url = transaction.receiptUrl,
            bill_date = transaction.billDate,
            create_time = transaction.createTime,
            created_by_supplier = transaction.createdBySupplier,
            deleted = transaction.deleted,
            delete_time = transaction.deleteTime,
            deleted_by_supplier = transaction.deletedBySupplier,
            update_time = transaction.updateTime,
            transaction_state = -1,
            tx_category = 0
        )

        // Added for fixing un-sync txns issue on 2.11.2. Remove Soon
        return store.get().getSupplier(transaction.supplierId, businessId).firstOrError().flatMap {
            apiClient.get().addTransaction(
                transaction.supplierId,
                ApiMessages.AddTransactionRequest(transaction.id, txn, it.mobile),
                businessId
            )
        }
            .doOnEvent { t1, t2 -> Timber.i("SupplierThread addTransaction = ${Thread.currentThread()}") }
            .subscribeOn(api())
            .map { response ->
                if (response.isSuccessful) {
                    return@map response.body()
                } else {
                    val error = response.asError()

                    if (error.code == 400 && error.message == "invalid_txn_amount") {
                        throw SupplierCreditServerErrors.InvalidAmount()
                    } else if (error.code == 400 && error.message == "invalid_txn_req_id") {
                        throw SupplierCreditServerErrors.InvalidTransaction()
                    } else if (error.code == 409 && error.message == "txn_req_id_exists") {
                        throw SupplierCreditServerErrors.InvalidTransaction()
                    } else {
                        throw error
                    }
                }
            }.map { ApiEntityMapper.TRANSACTION.convert(it) }
    }

    override fun getTransactionOfSupplier(supplierId: String, businessId: String): Single<List<Transaction>> {
        return apiClient.get().getTransactionsOfSupplier(supplierId, businessId)
            .doOnEvent { t1, t2 -> Timber.i("SupplierThread getTransactionOfSupplier = ${Thread.currentThread()}") }
            .subscribeOn(api())
            .map {
                if (it.isSuccessful) {
                    return@map it.body()?.transactions
                } else {
                    throw it.asError()
                }
            }
            .map { Utils.mapList(it, ApiEntityMapper.TRANSACTION) }
    }

    override fun getTransactions(startTime: DateTime?, businessId: String): Single<List<Transaction>> {
        return apiClient.get().getTransactions(startTime, businessId = businessId)
            .doOnEvent { t1, t2 -> Timber.i("SupplierThread getTransactionOfSupplier = ${Thread.currentThread()}") }
            .subscribeOn(api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful) {
                    return@map it.body()?.transactions
                } else {
                    throw it.asError()
                }
            }
            .map { Utils.mapList(it, ApiEntityMapper.TRANSACTION) }
    }

    override fun getSCEnabledCustomerIds(businessId: String): Single<AccountMetaInfo> {
        return ab.get().isFeatureEnabled(AbFeatures.SINGLE_LIST).firstOrError().flatMap { it ->
            val requestBody = ApiMessages.FeatureFindRequest("supplier_credit")
            val endpoint: Single<Response<ApiMessages.FeatureFindResponse>>
            if (it) {
                endpoint = apiClient.get().getSingleListSupplierEnabledCustomerIds(businessId)
            } else {
                endpoint = apiClient.get().getSupplierEnabledCustomerIds(requestBody, businessId)
            }

            endpoint
                .subscribeOn(api())
                .observeOn(ThreadUtils.worker())
                .map {
                    if (it.isSuccessful) {
                        return@map it.body()
                    } else {
                        throw it.asError()
                    }
                }
                .map {
                    ApiEntityMapper.ACCOUNT_META_INFO.convert(it)
                }
        }
    }

    override fun getTransaction(txnId: String, businessId: String): Single<Transaction> {
        return apiClient.get().getTransaction(txnId, businessId)
            .subscribeOn(api())
            .map {
                if (it.isSuccessful) {
                    return@map it.body()
                } else {
                    throw it.asError()
                }
            }
            .map { ApiEntityMapper.TRANSACTION.convert(it) }
    }

    override fun deleteTransaction(txnId: String, businessId: String): Completable {
        return apiClient.get()
            .deleteTransaction(txnId, businessId)
            .subscribeOn(api())
            .flatMapCompletable {
                if (it.isSuccessful) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    return@flatMapCompletable Completable.error(it.asError())
                }
            }
    }

    override fun getNotificationReminder(
        startTime: Long,
        businessId: String,
    ): Single<ApiMessages.NotificationRemindersResponse> {
        return apiClient.get().getNotificationReminders(startTime, businessId)
            .subscribeOn(api())
            .map {
                if (it.isSuccessful) {
                    return@map requireNotNull(it.body())
                } else {
                    throw it.asError()
                }
            }
    }

    override fun createNetworkReminder(accountId: String, businessId: String): Single<Boolean> {
        return apiClient.get().createNotificationReminder(
            ApiMessages.CreateNotificationReminderRequest(accountId = accountId),
            businessId
        )
            .subscribeOn(api())
            .observeOn(worker())
            .map {
                if (it.isSuccessful) {
                    return@map requireNotNull(it.body()).success
                } else {
                    throw it.asError()
                }
            }
    }

    override fun updateNotificationReminder(request: ApiMessages.UpdateNotificationReminder, businessId: String): Single<List<ApiMessages.UpdateNotificationReminderAction>> {
        return apiClient.get().updateNotificationReminders(request, businessId)
            .subscribeOn(api())
            .observeOn(worker())
            .map {
                if (it.isSuccessful) {
                    Timber.d("updateNotificationReminder response : ${requireNotNull(requireNotNull(it.body()).updateNotificationReminderAction)}")
                    return@map requireNotNull(it.body()).updateNotificationReminderAction
                } else {
                    throw it.asError()
                }
            }
    }
}
