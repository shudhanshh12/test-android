package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.common.CoreModuleMapper
import `in`.okcredit.backend._offline.common.CoreModuleMapper.toCustomer
import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.error.CustomerErrors
import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.common.CoreException
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import android.util.Pair
import androidx.room.EmptyResultSetException
import com.google.common.base.Strings
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject

class UpdateCustomer @Inject constructor(
    private val server: Lazy<BackendRemoteSource>,
    private val customerRepo: Lazy<CustomerRepo>,
    private val supplierCreditApi: Lazy<SupplierCreditRepository>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val coreSdk: Lazy<CoreSdk>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
) {

    // isMobileUpdate -> helps to know whether user is updating mobile number
    // if updating mobile number , then we have to check for cyclic error
    private var isMobileUpdate = false

    fun execute(customerId: String, reminderMode: String): Completable {
        return getActiveBusinessId.get().execute()
            .flatMapCompletable { businessId: String ->
                coreSdk.get()
                    .isCoreSdkFeatureEnabled(businessId)
                    .flatMapCompletable {
                        if (it) {
                            return@flatMapCompletable coreExecute(customerId, reminderMode, businessId)
                        } else {
                            return@flatMapCompletable backendExecute(customerId, reminderMode, businessId)
                        }
                    }.andThen(
                        Completable.fromAction {
                            collectionSyncer.get().scheduleCollectionProfileForCustomer(customerId, businessId)
                        }
                    )
            }
    }

    fun executeUpdateMobile(customerId: String, mobileNumber: String): Completable {
        return getActiveBusinessId.get().execute()
            .flatMapCompletable { businessId: String ->
                coreSdk.get()
                    .isCoreSdkFeatureEnabled(businessId)
                    .flatMapCompletable {
                        if (it) {
                            return@flatMapCompletable coreUpdateMobile(customerId, mobileNumber, businessId)
                        } else {
                            return@flatMapCompletable backendUpdateMobile(customerId, mobileNumber, businessId)
                        }
                    }.andThen(
                        Completable.fromAction {
                            collectionSyncer.get().scheduleCollectionProfileForCustomer(customerId, businessId)
                        }
                    )
            }
    }

    fun executeUpdateAddress(customerId: String, mobileNumber: String): Completable {
        return getActiveBusinessId.get().execute()
            .flatMapCompletable { businessId: String ->
                coreSdk.get()
                    .isCoreSdkFeatureEnabled(businessId)
                    .flatMapCompletable {
                        if (it) {
                            return@flatMapCompletable coreUpdateAddress(customerId, mobileNumber, businessId)
                        } else {
                            return@flatMapCompletable backendUpdateMobile(customerId, mobileNumber, businessId)
                        }
                    }.andThen(
                        Completable.fromAction {
                            collectionSyncer.get().scheduleCollectionProfileForCustomer(customerId, businessId)
                        }
                    )
            }
    }

    fun execute(customerId: String, state: Customer.State): Completable {
        return getActiveBusinessId.get().execute()
            .flatMapCompletable { businessId: String ->
                coreSdk.get()
                    .isCoreSdkFeatureEnabled(businessId)
                    .flatMapCompletable {
                        if (it) {
                            return@flatMapCompletable coreExecute(customerId, state, businessId)
                        } else {
                            return@flatMapCompletable backendExecute(customerId, state, businessId)
                        }
                    }.andThen(
                        Completable.fromAction {
                            collectionSyncer.get().scheduleCollectionProfileForCustomer(customerId, businessId)
                        }
                    )
            }
    }

    fun execute(
        customerId: String,
        desc: String,
        address: String?,
        profileImage: String?,
        mobile: String?,
        lang: String?,
        reminderMode: String?,
        txnAlertEnabled: Boolean,
        isForTxnEnable: Boolean,
        isMobileUpdate: Boolean,
        dueInfo_activeDate: DateTime?,
        updateDueCustomDate: Boolean,
        deleteDueCustomDate: Boolean,
        addTransactionPermission: Boolean,
        updateAddTransactionRestricted: Boolean,
        state: Customer.State?,
        updateState: Boolean,
    ): Completable {
        return getActiveBusinessId.get().execute()
            .flatMapCompletable { businessId: String ->
                coreSdk.get()
                    .isCoreSdkFeatureEnabled(businessId)
                    .flatMapCompletable {
                        if (it) {
                            return@flatMapCompletable coreExecute(
                                customerId,
                                desc,
                                address,
                                profileImage,
                                mobile,
                                lang,
                                reminderMode,
                                txnAlertEnabled,
                                isForTxnEnable,
                                isMobileUpdate,
                                dueInfo_activeDate,
                                updateDueCustomDate,
                                deleteDueCustomDate,
                                addTransactionPermission,
                                updateAddTransactionRestricted,
                                state,
                                updateState,
                                businessId
                            )
                        } else {
                            return@flatMapCompletable backendExecute(
                                customerId,
                                desc,
                                address,
                                profileImage,
                                mobile,
                                lang,
                                reminderMode,
                                txnAlertEnabled,
                                isForTxnEnable,
                                isMobileUpdate,
                                dueInfo_activeDate,
                                updateDueCustomDate,
                                deleteDueCustomDate,
                                addTransactionPermission,
                                updateAddTransactionRestricted,
                                state,
                                updateState,
                                businessId
                            )
                        }
                    }.andThen(
                        Completable.fromAction {
                            collectionSyncer.get().scheduleCollectionProfileForCustomer(customerId, businessId)
                        }
                    )
            }
    }

    // if , merchant trying to added this mobile number as customer , but this mobile number is
    // already added as supplier , then
    // 1. if existing supplier is deleted :-> we throw DeletedCyclicAccount and take user to
    // supplier transaction screen and un-delete this supplier
    // 2. if existing supplier is not deleted:->  we throw ActiveCyclicAccount and take user to
    // supplier transaction screen
    private fun validateCyclicAccount(mobile: String?, businessId: String): Single<String> {
        return if (Strings.isNullOrEmpty(mobile) || mobile!!.length != 10) {
            Single.just("")
        } else {
            supplierCreditApi.get().getSupplierByMobile(mobile, businessId)
                .flatMap { supplier: Supplier ->
                    getActiveBusiness
                        .get()
                        .execute()
                        .firstOrError()
                        .flatMap { (_, _, mobile1) ->
                            // 1. cyclic account is not considered for
                            // merchant (this device user)
                            // 2. cyclic account is not considered if
                            // not updating mobile number
                            if (mobile1 == mobile || !isMobileUpdate) {
                                return@flatMap Single.just(mobile)
                            } else {
                                if (supplier.deleted) {
                                    return@flatMap Single.error<String>(
                                        CustomerErrors.DeletedCyclicAccount(
                                            supplier
                                        )
                                    )
                                } else {
                                    return@flatMap Single.error<String>(
                                        CustomerErrors.ActiveCyclicAccount(
                                            supplier
                                        )
                                    )
                                }
                            }
                        }
                }
                .onErrorResumeNext label@{ throwable: Throwable? ->
                    if (throwable is EmptyResultSetException) {
                        return@label Single.just(mobile)
                    } else {
                        return@label Single.error<String>(throwable)
                    }
                }
        }
    }

    private fun validateDesc(desc: String): Single<String> {
        return if (Strings.isNullOrEmpty(desc)) {
            Single.just("")
        } else {
            Single.just(desc)
        }
    }

    // Backend ==============================
    private fun backendExecute(customerId: String, reminderMode: String, businessId: String): Completable {
        return customerRepo
            .get()
            .getCustomer(customerId, businessId)
            .firstOrError()
            .flatMapCompletable { customer: Customer ->
                backendExecute(
                    customerId = customerId,
                    desc = customer.description,
                    address = customer.address,
                    profileImage = customer.profileImage,
                    mobile = customer.mobile,
                    lang = customer.lang,
                    reminderMode = reminderMode,
                    txnAlertEnabled = customer.isTxnAlertEnabled(),
                    isForTxnEnable = false,
                    isMobileUpdate = false,
                    dueInfo_activeDate = customer.dueInfo_activeDate,
                    updateDueCustomDate = false,
                    deleteDueCustomDate = false,
                    addTransactionPermission = customer.isAddTransactionPermissionDenied(),
                    updateAddTransactionRestricted = false,
                    state = customer.state,
                    updateState = false,
                    businessId = businessId
                )
            }
    }

    private fun backendUpdateMobile(customerId: String, mobileNumber: String, businessId: String): Completable {
        return customerRepo
            .get()
            .getCustomer(customerId, businessId)
            .firstOrError()
            .flatMapCompletable { customer: Customer ->
                backendExecute(
                    customerId = customerId,
                    desc = customer.description,
                    address = customer.address,
                    profileImage = customer.profileImage,
                    mobile = mobileNumber,
                    lang = customer.lang,
                    reminderMode = customer.reminderMode,
                    txnAlertEnabled = customer.isTxnAlertEnabled(),
                    isForTxnEnable = false,
                    isMobileUpdate = true,
                    dueInfo_activeDate = customer.dueInfo_activeDate,
                    updateDueCustomDate = false,
                    deleteDueCustomDate = false,
                    addTransactionPermission = customer.isAddTransactionPermissionDenied(),
                    updateAddTransactionRestricted = false,
                    state = customer.state,
                    updateState = false,
                    businessId = businessId
                )
            }
    }

    private fun backendUpdateAddress(customerId: String, address: String, businessId: String): Completable {
        return customerRepo
            .get()
            .getCustomer(customerId, businessId)
            .firstOrError()
            .flatMapCompletable { customer: Customer ->
                backendExecute(
                    customerId = customerId,
                    desc = customer.description,
                    address = address,
                    profileImage = customer.profileImage,
                    mobile = customer.mobile,
                    lang = customer.lang,
                    reminderMode = customer.reminderMode,
                    txnAlertEnabled = customer.isTxnAlertEnabled(),
                    isForTxnEnable = false,
                    isMobileUpdate = true,
                    dueInfo_activeDate = customer.dueInfo_activeDate,
                    updateDueCustomDate = false,
                    deleteDueCustomDate = false,
                    addTransactionPermission = customer.isAddTransactionPermissionDenied(),
                    updateAddTransactionRestricted = false,
                    state = customer.state,
                    updateState = false,
                    businessId = businessId
                )
            }
    }

    private fun backendExecute(customerId: String, state: Customer.State, businessId: String): Completable {
        return customerRepo
            .get()
            .getCustomer(customerId, businessId)
            .firstOrError()
            .flatMapCompletable { customer: Customer ->
                backendExecute(
                    customerId = customerId,
                    desc = customer.description,
                    address = customer.address,
                    profileImage = customer.profileImage,
                    mobile = customer.mobile,
                    lang = customer.lang,
                    reminderMode = customer.reminderMode,
                    txnAlertEnabled = customer.isTxnAlertEnabled(),
                    isForTxnEnable = false,
                    isMobileUpdate = false,
                    dueInfo_activeDate = customer.dueInfo_activeDate,
                    updateDueCustomDate = false,
                    deleteDueCustomDate = false,
                    addTransactionPermission = customer.isAddTransactionPermissionDenied(),
                    updateAddTransactionRestricted = false,
                    state = state,
                    updateState = true,
                    businessId = businessId
                )
            }
    }

    private fun backendUpdateCustomerOnDb(customer: Customer, businessId: String): Completable {
        return customerRepo.get().putCustomer(customer, businessId)
    }

    // checks whether this mobile number is existing customer
    private fun backendValidateMobile(customerId: String, mobile: String?, businessId: String): Single<String> {
        return if (Strings.isNullOrEmpty(mobile) || mobile!!.length != 10) {
            Single.just("")
        } else {
            customerRepo
                .get()
                .findCustomerByMobile(mobile, businessId)
                .flatMap { customer: Customer ->
                    if (customer.id == customerId) {
                        return@flatMap Single.just("")
                    } else {
                        return@flatMap Single.error<String>(
                            CustomerErrors.MobileConflict(customer)
                        )
                    }
                }
                .onErrorResumeNext label@{ throwable: Throwable? ->
                    if (throwable is NoSuchElementException) {
                        return@label Single.just(mobile)
                    } else {
                        return@label Single.error<String>(throwable)
                    }
                }
        }
    }

    private fun backendExecute(
        customerId: String,
        desc: String,
        address: String?,
        profileImage: String?,
        mobile: String?,
        lang: String?,
        reminderMode: String?,
        txnAlertEnabled: Boolean,
        isForTxnEnable: Boolean,
        isMobileUpdate: Boolean,
        dueInfo_activeDate: DateTime?,
        updateDueCustomDate: Boolean,
        deleteDueCustomDate: Boolean,
        addTransactionPermission: Boolean,
        updateAddTransactionRestricted: Boolean,
        state: Customer.State?,
        updateState: Boolean,
        businessId: String,
    ): Completable {
        this.isMobileUpdate = isMobileUpdate
        return Single.zip(
            backendValidateMobile(customerId, mobile, businessId),
            validateDesc(desc),
            validateCyclicAccount(mobile, businessId),
            { _mobile: String, _desc: String, t: String? -> Pair(_desc, _mobile) }
        )
            .flatMapCompletable { data: Pair<String, String> ->
                val _desc = Strings.emptyToNull(data.first)
                val _mobile = Strings.emptyToNull(data.second)
                if (_desc == null && _mobile == null && address == null && profileImage == null) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    when {
                        _desc != null -> {
                            return@flatMapCompletable server.get()
                                .updateCustomer(
                                    customerId,
                                    data.first,
                                    address,
                                    profileImage,
                                    mobile,
                                    lang,
                                    reminderMode,
                                    txnAlertEnabled,
                                    isForTxnEnable,
                                    dueInfo_activeDate,
                                    updateDueCustomDate,
                                    deleteDueCustomDate,
                                    addTransactionPermission,
                                    updateAddTransactionRestricted,
                                    state!!.value,
                                    updateState,
                                    businessId
                                )
                                .flatMapCompletable { customer: Customer ->
                                    backendUpdateCustomerOnDb(
                                        customer,
                                        businessId
                                    )
                                }
                        }
                        _mobile != null -> {
                            return@flatMapCompletable server.get()
                                .updateCustomer(
                                    customerId,
                                    desc,
                                    address,
                                    profileImage,
                                    data.second,
                                    lang,
                                    reminderMode,
                                    txnAlertEnabled,
                                    isForTxnEnable,
                                    dueInfo_activeDate,
                                    updateDueCustomDate,
                                    deleteDueCustomDate,
                                    addTransactionPermission,
                                    updateAddTransactionRestricted,
                                    state!!.value,
                                    updateState,
                                    businessId
                                )
                                .flatMapCompletable { customer: Customer ->
                                    backendUpdateCustomerOnDb(
                                        customer,
                                        businessId
                                    )
                                }
                        }
                        else -> {
                            return@flatMapCompletable server.get()
                                .updateCustomer(
                                    customerId,
                                    desc,
                                    address,
                                    profileImage,
                                    mobile,
                                    lang,
                                    reminderMode,
                                    txnAlertEnabled,
                                    isForTxnEnable,
                                    dueInfo_activeDate,
                                    updateDueCustomDate,
                                    deleteDueCustomDate,
                                    addTransactionPermission,
                                    updateAddTransactionRestricted,
                                    state!!.value,
                                    updateState,
                                    businessId
                                )
                                .flatMapCompletable { customer: Customer ->
                                    backendUpdateCustomerOnDb(
                                        customer,
                                        businessId
                                    )
                                }
                        }
                    }
                }
            }
    }

    // Core ==============================
    private fun coreExecute(customerId: String, reminderMode: String, businessId: String): Completable {
        return coreSdk.get()
            .getCustomer(customerId)
            .map(CoreModuleMapper::toCustomer)
            .firstOrError()
            .flatMapCompletable { customer: Customer ->
                coreExecute(
                    customerId = customerId,
                    desc = customer.description,
                    address = customer.address,
                    profileImage = customer.profileImage,
                    mobile = customer.mobile,
                    lang = customer.lang,
                    reminderMode = reminderMode,
                    txnAlertEnabled = customer.isTxnAlertEnabled(),
                    isForTxnEnable = false,
                    isMobileUpdate = false,
                    dueInfo_activeDate = customer.dueInfo_activeDate,
                    updateDueCustomDate = false,
                    deleteDueCustomDate = false,
                    addTransactionPermission = customer.isAddTransactionPermissionDenied(),
                    updateAddTransactionRestricted = false,
                    state = customer.state,
                    updateState = false,
                    businessId = businessId
                )
            }
    }

    private fun coreUpdateMobile(customerId: String, mobileNumber: String, businessId: String): Completable {
        return coreSdk.get().getCustomer(customerId)
            .map(CoreModuleMapper::toCustomer)
            .firstOrError()
            .flatMapCompletable { customer: Customer ->
                coreExecute(
                    customerId = customerId,
                    desc = customer.description,
                    address = customer.address,
                    profileImage = customer.profileImage,
                    mobile = mobileNumber,
                    lang = customer.lang,
                    reminderMode = customer.reminderMode,
                    txnAlertEnabled = customer.isTxnAlertEnabled(),
                    isForTxnEnable = false,
                    isMobileUpdate = true,
                    dueInfo_activeDate = customer.dueInfo_activeDate,
                    updateDueCustomDate = false,
                    deleteDueCustomDate = false,
                    addTransactionPermission = customer.isAddTransactionPermissionDenied(),
                    updateAddTransactionRestricted = false,
                    state = customer.state,
                    updateState = false,
                    businessId = businessId
                )
            }
    }

    private fun coreUpdateAddress(customerId: String, address: String, businessId: String): Completable {
        return coreSdk.get()
            .getCustomer(customerId)
            .map(CoreModuleMapper::toCustomer)
            .firstOrError()
            .flatMapCompletable { customer: Customer ->
                coreExecute(
                    customerId = customerId,
                    desc = customer.description,
                    address = address,
                    profileImage = customer.profileImage,
                    mobile = customer.mobile,
                    lang = customer.lang,
                    reminderMode = customer.reminderMode,
                    txnAlertEnabled = customer.isTxnAlertEnabled(),
                    isForTxnEnable = false,
                    isMobileUpdate = true,
                    dueInfo_activeDate = customer.dueInfo_activeDate,
                    updateDueCustomDate = false,
                    deleteDueCustomDate = false,
                    addTransactionPermission = customer.isAddTransactionPermissionDenied(),
                    updateAddTransactionRestricted = false,
                    state = customer.state,
                    updateState = false,
                    businessId = businessId
                )
            }
    }

    private fun coreExecute(customerId: String, state: Customer.State, businessId: String): Completable {
        return coreSdk.get()
            .getCustomer(customerId)
            .map(CoreModuleMapper::toCustomer)
            .firstOrError()
            .flatMapCompletable { customer: Customer ->
                coreExecute(
                    customerId = customerId,
                    desc = customer.description,
                    address = customer.address,
                    profileImage = customer.profileImage,
                    mobile = customer.mobile,
                    lang = customer.lang,
                    reminderMode = customer.reminderMode,
                    txnAlertEnabled = customer.isTxnAlertEnabled(),
                    isForTxnEnable = false,
                    isMobileUpdate = false,
                    dueInfo_activeDate = customer.dueInfo_activeDate,
                    updateDueCustomDate = false,
                    deleteDueCustomDate = false,
                    addTransactionPermission = customer.isAddTransactionPermissionDenied(),
                    updateAddTransactionRestricted = false,
                    state = state,
                    updateState = true,
                    businessId = businessId
                )
            }
    }

    // checks whether this mobile number is existing customer
    private fun coreValidateMobile(customerId: String, mobile: String?, businessId: String): Single<String> {
        return if (Strings.isNullOrEmpty(mobile) || mobile!!.length != 10) {
            Single.just("")
        } else {
            coreSdk.get()
                .getCustomerByMobile(mobile, businessId)
                .map(CoreModuleMapper::toCustomer)
                .flatMap { customer: Customer ->
                    if (customer.id == customerId) {
                        return@flatMap Single.just("")
                    } else {
                        return@flatMap Single.error<String>(
                            CustomerErrors.MobileConflict(customer)
                        )
                    }
                }
                .onErrorResumeNext label@{ throwable: Throwable? ->
                    if (throwable is NoSuchElementException) {
                        return@label Single.just(mobile)
                    } else {
                        return@label Single.error<String>(throwable)
                    }
                }
        }
    }

    private fun coreExecute(
        customerId: String,
        desc: String,
        address: String?,
        profileImage: String?,
        mobile: String?,
        lang: String?,
        reminderMode: String?,
        txnAlertEnabled: Boolean,
        isForTxnEnable: Boolean,
        isMobileUpdate: Boolean,
        dueInfo_activeDate: DateTime?,
        updateDueCustomDate: Boolean,
        deleteDueCustomDate: Boolean,
        addTransactionPermission: Boolean,
        updateAddTransactionRestricted: Boolean,
        state: Customer.State?,
        updateState: Boolean,
        businessId: String,
    ): Completable {
        this.isMobileUpdate = isMobileUpdate
        return Single.zip(
            coreValidateMobile(customerId, mobile, businessId),
            validateDesc(desc),
            validateCyclicAccount(mobile, businessId),
            { _mobile: String, _desc: String, _: String? -> Pair(_desc, _mobile) }
        )
            .flatMapCompletable { data: Pair<String, String> ->
                val _desc = Strings.emptyToNull(data.first)
                val _mobile = Strings.emptyToNull(data.second)
                if (_desc == null && _mobile == null && address == null && profileImage == null) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    var dueDate: Timestamp? = null
                    if (dueInfo_activeDate != null) {
                        dueDate = Timestamp(dueInfo_activeDate.millis)
                    }
                    when {
                        _desc != null -> {
                            return@flatMapCompletable coreSdk.get().updateCustomer(
                                customerId,
                                data.first,
                                address,
                                profileImage,
                                mobile,
                                lang,
                                reminderMode,
                                txnAlertEnabled,
                                isForTxnEnable,
                                dueDate,
                                updateDueCustomDate,
                                deleteDueCustomDate,
                                addTransactionPermission,
                                updateAddTransactionRestricted,
                                state!!.value,
                                updateState,
                                businessId
                            ).onErrorResumeNext { error: Throwable? ->
                                if (error is CoreException.MobileConflict) {
                                    val conflict: Customer = toCustomer(
                                        error
                                            .conflict
                                    )
                                    return@onErrorResumeNext Completable.error(
                                        CustomerErrors.MobileConflict(
                                            conflict
                                        )
                                    )
                                } else if (error is CoreException.MobileUpdateAccessDenied) {
                                    return@onErrorResumeNext Completable.error(
                                        CustomerErrors.MobileUpdateAccessDenied()
                                    )
                                }
                                Completable.error(error)
                            }
                        }
                        _mobile != null -> {
                            return@flatMapCompletable coreSdk.get()
                                .updateCustomer(
                                    customerId,
                                    desc,
                                    address,
                                    profileImage,
                                    data.second,
                                    lang,
                                    reminderMode,
                                    txnAlertEnabled,
                                    isForTxnEnable,
                                    dueDate,
                                    updateDueCustomDate,
                                    deleteDueCustomDate,
                                    addTransactionPermission,
                                    updateAddTransactionRestricted,
                                    state!!.value,
                                    updateState,
                                    businessId
                                )
                                .onErrorResumeNext { error: Throwable? ->
                                    if (error is CoreException.MobileConflict) {
                                        val conflict: Customer = toCustomer(error.conflict)
                                        return@onErrorResumeNext Completable.error(
                                            CustomerErrors.MobileConflict(conflict)
                                        )
                                    } else if (error is CoreException.MobileUpdateAccessDenied) {
                                        return@onErrorResumeNext Completable.error(
                                            CustomerErrors.MobileUpdateAccessDenied()
                                        )
                                    }
                                    Completable.error(error)
                                }
                        }
                        else -> {
                            return@flatMapCompletable coreSdk.get()
                                .updateCustomer(
                                    customerId,
                                    desc,
                                    address,
                                    profileImage,
                                    mobile,
                                    lang,
                                    reminderMode,
                                    txnAlertEnabled,
                                    isForTxnEnable,
                                    dueDate,
                                    updateDueCustomDate,
                                    deleteDueCustomDate,
                                    addTransactionPermission,
                                    updateAddTransactionRestricted,
                                    state!!.value,
                                    updateState,
                                    businessId
                                )
                                .onErrorResumeNext { error: Throwable? ->
                                    if (error is CoreException.MobileConflict) {
                                        val conflict: Customer = toCustomer(error.conflict)
                                        return@onErrorResumeNext Completable.error(
                                            CustomerErrors.MobileConflict(conflict)
                                        )
                                    } else if (error is CoreException.MobileUpdateAccessDenied) {
                                        return@onErrorResumeNext Completable.error(
                                            CustomerErrors.MobileUpdateAccessDenied()
                                        )
                                    }
                                    Completable.error(error)
                                }
                        }
                    }
                }
            }
    }
}
