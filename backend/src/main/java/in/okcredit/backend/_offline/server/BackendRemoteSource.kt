package `in`.okcredit.backend._offline.server

import `in`.okcredit.backend._offline.common.Utils
import `in`.okcredit.backend._offline.error.CustomerErrors
import `in`.okcredit.backend._offline.server.internal.*
import `in`.okcredit.backend._offline.serverV2.internal.ApiMessagesV2
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.Version
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionRequest
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import merchant.okcredit.accounting.model.Transaction
import merchant.okcredit.accounting.model.TransactionImage
import org.joda.time.DateTime
import retrofit2.Response
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.ThreadUtils.api
import tech.okcredit.android.base.utils.ThreadUtils.database
import tech.okcredit.android.base.utils.ThreadUtils.worker
import tech.okcredit.base.network.asError
import timber.log.Timber
import javax.inject.Inject

class BackendRemoteSource @Inject constructor(
    private val apiClient: Lazy<ApiClient>,
    private val transactionMapper: Lazy<TransactionMapper>,
    private val localeManager: Lazy<LocaleManager>,
    private val reportsV2ApiClient: Lazy<ReportsV2ApiClient>,
) {
    val latestVersion: Single<Version>
        get() {
            val lang = localeManager.get().getLanguage()
            return apiClient
                .get()
                .getLatestVersion(lang = lang)
                .subscribeOn(api())
                .observeOn(worker())
                .map { res ->
                    if (res.isSuccessful) {
                        return@map res.body()
                    } else {
                        throw res.asError()
                    }
                }
        }

    fun checkMobileStatus(mobile: String): Single<Boolean> {
        return apiClient
            .get()
            .checkMobileStatus(CheckMobileStatusRequest(mobile = mobile))
            .subscribeOn(api())
            .observeOn(worker())
            .map { res ->
                res.isSuccessful
            }
    }

    fun linkDevice(deviceId: String): Completable {
        return apiClient
            .get()
            .linkDevice(deviceId)
            .subscribeOn(api())
            .observeOn(worker())
            .flatMapCompletable { response ->
                if (response.isSuccessful) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    return@flatMapCompletable Completable.error(response.asError())
                }
            }
    }

    fun listCustomers(mobile: String?, businessId: String): Single<List<Customer>> {
        return apiClient
            .get()
            .listCustomers(
                mobile = mobile,
                deleted = true,
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
            .map { response ->
                if (response.isSuccessful) {
                    return@map response.body()
                } else {
                    throw response.asError()
                }
            }
            .map { customers ->
                Utils.mapList(
                    customers,
                    ApiEntityMapper.CUSTOMER
                )
            }
    }

    fun getCustomer(customerId: String, businessId: String): Single<Customer> {
        return apiClient
            .get()
            .getCustomer(
                customerId = customerId,
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
            .map { res ->
                if (res.isSuccessful) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
            .map { a -> ApiEntityMapper.CUSTOMER.convert(a) }
    }

    fun addCustomer(
        description: String,
        mobile: String,
        reactivate: Boolean,
        profileImage: String,
        businessId: String,
    ): Single<Customer> {
        Timber.i("addCustomer reactivated 1")
        return apiClient
            .get()
            .addCustomer(
                AddCustomerRequest(
                    mobile,
                    description,
                    reactivate,
                    profileImage,
                ),
                businessId
            )
            .subscribeOn(api())
            .observeOn(worker())
            .flatMap { res ->
                if (res.isSuccessful) {
                    // success_1
                    Timber.i("addCustomer reactivated 2")
                    return@flatMap Single.just(res.body())
                        .map { a: `in`.okcredit.backend._offline.server.internal.Customer? ->
                            ApiEntityMapper.CUSTOMER.convert(a)
                        }
                } else {
                    Timber.i("addCustomer reactivated 3")
                    val error = res.asError()
                    if (error.code == 409 &&
                        "customer_exists" == error.message
                    ) {
                        // mobile already registered with another customer
                        // find that customer and throw an error
                        return@flatMap apiClient
                            .get()
                            .listCustomers(mobile, true, businessId)
                            .subscribeOn(api())
                            .observeOn(worker())
                            .map { findByMobileRes ->
                                if (findByMobileRes.isSuccessful) {
                                    val customers = findByMobileRes.body()!!
                                    if (customers.size != 1) {
                                        throw IllegalAccessException(
                                            "mobile conflict but find_by_mobile not valid"
                                        )
                                    }
                                    return@map customers[0]
                                } else {
                                    throw findByMobileRes.asError()
                                }
                            }
                            .map { a ->
                                ApiEntityMapper.CUSTOMER.convert(a)
                            }
                            .flatMap { customer ->
                                if (customer.status == 1) {
                                    return@flatMap Single.error<Customer>(
                                        CustomerErrors.MobileConflict(
                                            customer
                                        )
                                    )
                                } else {
                                    return@flatMap Single.error<Customer>(
                                        CustomerErrors.DeletedCustomer(
                                            customer
                                        )
                                    )
                                }
                            }
                    } else {
                        return@flatMap Single.error<Customer>(error)
                    }
                }
            }
    }

    fun deleteCustomer(customerId: String, businessId: String): Completable {
        return apiClient
            .get()
            .deleteCustomer(
                customerId = customerId,
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
            .flatMapCompletable { res ->
                if (res.isSuccessful) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    val error = res.asError()
                    if (error.code == ERROR_CODE_409) {
                        return@flatMapCompletable Completable.error(
                            CustomerErrors.DeletePermissionDenied(
                                error.error
                            )
                        )
                    } else {
                        return@flatMapCompletable Completable.error(error)
                    }
                }
            }
    }

    fun getTransaction(txnId: String, businessId: String): Single<Transaction?> {
        return apiClient
            .get()
            .getTransaction(txnId = txnId, businessId = businessId)
            .subscribeOn(api())
            .observeOn(worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()!!
                } else {
                    throw res.asError()
                }
            }
            .observeOn(database())
            .map {
                transactionMapper.get().convert(it)
            }
            .observeOn(worker())
    }

    fun deleteTransaction(txnId: String, businessId: String): Completable {
        return apiClient
            .get()
            .deleteTransaction(txnId = txnId, businessId = businessId)
            .subscribeOn(api())
            .observeOn(worker())
            .flatMapCompletable { res ->
                if (res.isSuccessful) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    throw res.asError()
                }
            }
    }

    fun addTransaction(
        customerId: String,
        localId: String,
        type: Int,
        amountV2: Long,
        receiptUrl: List<TransactionImage>?,
        note: String?,
        timestamp: DateTime,
        isOnboarding: Boolean,
        billDate: DateTime,
        smsSent: Boolean,
        inputType: String?,
        voiceId: String?,
        businessId: String,
    ): Single<Transaction> {
        return apiClient
            .get()
            .addTransaction2(
                AddTransactionRequest(
                    customerId = customerId,
                    requestId = localId,
                    type = type,
                    amountV2 = amountV2,
                    receiptUrl = receiptUrl,
                    note = note,
                    timestamp = timestamp,
                    isOnboarding = isOnboarding,
                    billDate = billDate,
                    smsSent = smsSent,
                    inputType = inputType,
                    voiceId = voiceId,
                ),
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
            .map { res ->
                if (res.isSuccessful) {
                    return@map transactionMapper.get().convert(res.body())
                } else {
                    throw res.asError()
                }
            }
    }

    fun updateCustomer(
        customerId: String,
        desc: String,
        address: String?,
        profileImage: String?,
        mobile: String?,
        lang: String?,
        reminderMode: String?,
        txnAlertEnabled: Boolean,
        isForTxnEnable: Boolean,
        dueinfoActivedate: DateTime?,
        updateDueCustomDate: Boolean,
        deleteDueCustomDate: Boolean,
        addTransactionPermission: Boolean,
        updateAddTransactionRestricted: Boolean,
        blockTransaction: Int,
        updateBlockTransaction: Boolean,
        businessId: String,
    ): Single<Customer> {
        return apiClient
            .get()
            .updateCustomer(
                customerId = customerId,
                UpdateCustomerRequest(
                    desc = desc,
                    mobile = mobile,
                    address = address,
                    profileImage = profileImage,
                    lang = lang,
                    reminderMode = reminderMode,
                    txnAlertEnabled = txnAlertEnabled,
                    updateTxnAlertEnabled = isForTxnEnable,
                    dueCustomDate = dueinfoActivedate,
                    updateDueCustomDate = updateDueCustomDate,
                    deleteDueCustomDate = deleteDueCustomDate,
                    addTransactionPermission = addTransactionPermission,
                    updateAddTransactionRestricted = updateAddTransactionRestricted,
                    blockTransaction = blockTransaction,
                    updateBlockTransaction = updateBlockTransaction
                ),
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
            .flatMap { res ->
                if (res.isSuccessful) {
                    // success_1
                    return@flatMap Single.just(res.body())
                        .map { a ->
                            ApiEntityMapper.CUSTOMER.convert(a)
                        }
                } else {
                    val error = res.asError()
                    if (error.code == 403) {
                        throw CustomerErrors.MobileUpdateAccessDenied()
                    } else if (error.code == 409 &&
                        "customer_exists" == error.message
                    ) {

                        // mobile already registered with another customer
                        // find that customer and throw an error
                        return@flatMap apiClient
                            .get()
                            .listCustomers(mobile, true, businessId)
                            .subscribeOn(api())
                            .observeOn(worker())
                            .map { findByMobileRes ->
                                if (findByMobileRes.isSuccessful) {
                                    val customers = findByMobileRes.body()!!
                                    if (customers.size != 1) {
                                        throw IllegalAccessException(
                                            "mobile conflict but find_by_mobile not valid"
                                        )
                                    }
                                    return@map customers[0]
                                } else {
                                    throw findByMobileRes.asError()
                                }
                            }
                            .map { a ->
                                ApiEntityMapper.CUSTOMER.convert(a)
                            }
                            .flatMap { customer: Customer? ->
                                Single.error<Customer?>(
                                    CustomerErrors.MobileConflict(
                                        customer
                                    )
                                )
                            }
                    } else {
                        return@flatMap Single.error<Customer>(error)
                    }
                }
            }
    }

    fun submitFeedback(feedback: String?, rating: Int, businessId: String): Completable {
        return apiClient
            .get()
            .submitFeedback(
                FeedbackRequest(
                    rating = rating,
                    message = feedback ?: "",
                    feedback_type = FeedbackRequest.FEEDBACK_TYPE
                ),
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
            .flatMapCompletable { res ->
                if (res.isSuccessful) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    return@flatMapCompletable Completable.error(res.asError())
                }
            }
    }

    fun getDueInfo(businessId: String): Single<List<DueInfo>> {
        return apiClient.get().getDueInfo(
            ListDuesInfoRequest(sellerId = businessId),
            businessId = businessId,
        )
            .subscribeOn(api())
            .observeOn(worker())
            .map { res ->
                if (res.isSuccessful &&
                    res.body() != null
                ) {
                    return@map res.body()!!.getDueInfo()
                } else {
                    throw res.asError()
                }
            }
    }

    fun getParticularCustomerDueInfo(
        customerId: String?,
        businessId: String,
    ): Single<DueInfo> {
        return apiClient
            .get()
            .getParticularCustomerDueInfo(
                GetDueInfoRequest(
                    merchantId = businessId,
                    customerId = customerId,
                ),
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
            .flatMap { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@flatMap Single.just(res.body()!!.getDueInfo())
                } else {
                    return@flatMap Single.error<DueInfo>(res.asError())
                }
            }
    }

    fun deleteTransactionImage(
        imageId: String,
        txnId: String,
        businessId: String,
    ): Completable {
        return apiClient
            .get()
            .deleteTransactionImage(
                imageId = imageId,
                txnId = txnId,
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
    }

    fun updateTransactionNote(
        note: String,
        txnId: String,
        businessId: String,
    ): Completable {
        return apiClient
            .get()
            .updateTransactionNote(
                txnId,
                patchTransactionRequest = ApiMessagesV2.PatchTransactionRequest(note),
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
    }

    fun createTransactionImage(
        transactionImage: TransactionImage,
        businessId: String,
    ): Completable {
        return apiClient
            .get()
            .createTransactionImage(
                CreateTransactionImageRequest(
                    merchantId = businessId,
                    image = transactionImage,
                ),
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
    }

    fun postVoiceInput(text: String?, businessId: String): Single<Response<VoiceInputResponseBody>> {
        return apiClient
            .get()
            .postVoiceData(
                voiceInputBody = VoiceInputBody(text = text),
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
    }

    fun migrate(
        mercahntId: String,
        accountId: String,
        destiantion: Int,
        businessId: String,
    ): Single<MigrateAccountResponse> {
        return apiClient
            .get()
            .migrate(
                MigrationBody(
                    merchantId = mercahntId,
                    accountId = accountId,
                    destination = destiantion
                ),
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    fun createDiscount(
        customerId: String,
        requestId: String,
        amountv2: Long,
        note: String?,
        businessId: String,
    ): Completable {
        return apiClient
            .get()
            .createDiscount(
                AddDiscountRequest(
                    requestId = requestId,
                    accountId = customerId,
                    amount = amountv2,
                    note = note,
                ),
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
    }

    fun deleteDiscount(
        txnId: String,
        businessId: String,
    ): Completable {
        return apiClient
            .get()
            .deleteDiscount(
                deleteDiscountRequest = DeleteDiscountRequest(transaction_id = txnId),
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
    }

    fun syncUpdatedAccounts(
        updatedContactList: List<ContactSync>,
        businessId: String,
    ): Completable {
        return apiClient
            .get()
            .syncUpdatedAccounts(
                syncContactRequest = SyncContactRequest(contactSyncs = updatedContactList),
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
    }

    fun getAllAccountBuyerTxnAlertConfig(businessId: String): Single<AllAccountsBuyerTxnAlertConfigResponse> {
        return apiClient
            .get()
            .getAllAccountsBuyerTxnAlertConfig(businessId = businessId)
            .subscribeOn(api())
            .observeOn(worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    fun quickAddTransaction(
        request: QuickAddTransactionRequest,
        businessId: String,
    ): Single<QuickAddTransactionResponse> {
        return apiClient.get().quickAddTransaction(request = request, businessId = businessId)
    }

    fun updateFeatureValueRequest(accountID: String, action: Int, businessId: String): Completable {
        return apiClient
            .get()
            .updateFeatureValueRequest(
                updateFeatureValueRequest = UpdateFeatureValueRequest(
                    accountId = accountID,
                    action = action,
                ),
                businessId = businessId
            )
            .subscribeOn(api())
            .observeOn(worker())
    }

    fun checkActionableStatus(
        request: CheckActionableStatusRequest,
        businessId: String,
    ): Single<CheckActionableStatusResponse> {
        return apiClient.get().checkActionableStatus(request = request, businessId = businessId)
    }

    fun updateActionableStatus(actionId: String?, businessId: String): Completable {
        return apiClient.get().updateActionableStatus(actionId = actionId, businessId = businessId)
    }

    fun generateReportUrl(
        generateReportUrlRequest: GenerateReportUrlRequest,
        businessId: String,
    ): Single<GenerateReportUrlResponse> {
        return reportsV2ApiClient.get()
            .generateReportUrl(
                generateReportUrlRequest = generateReportUrlRequest,
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
    }

    fun getReportUrl(reportId: String, businessId: String): Single<GetReportUrlResponse> {
        return reportsV2ApiClient.get()
            .getReportUrl(
                reportId = reportId,
                businessId = businessId,
            )
            .subscribeOn(api())
            .observeOn(worker())
    }

    companion object {
        private const val ERROR_CODE_409 = 409
    }
}
