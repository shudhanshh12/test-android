package `in`.okcredit.backend._offline.database

import `in`.okcredit.backend._offline.common.CoreModuleMapper
import `in`.okcredit.backend._offline.common.CoreModuleMapper.toCustomer
import `in`.okcredit.backend._offline.common.DbReminderProfile
import `in`.okcredit.backend._offline.common.Utils
import `in`.okcredit.backend._offline.common.toBackendReminderProfileList
import `in`.okcredit.backend._offline.database.internal.CustomerDao
import `in`.okcredit.backend._offline.database.internal.DbEntities
import `in`.okcredit.backend._offline.database.internal.DbEntityMapper
import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.server.internal.AllAccountsBuyerTxnAlertConfigResponse
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.model.bulk_reminder.LastReminderSendTime
import `in`.okcredit.merchant.core.model.bulk_reminder.convertBackendToLastReminderSendTime
import `in`.okcredit.merchant.core.model.bulk_reminder.toLastReminderSendTime
import `in`.okcredit.merchant.core.store.database.BulkReminderDbInfo
import androidx.room.EmptyResultSetException
import com.google.firebase.perf.FirebasePerformance
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.Traces
import timber.log.Timber
import javax.inject.Inject
import `in`.okcredit.merchant.core.common.Timestamp as CoreTimestamp
import `in`.okcredit.merchant.core.model.Customer as CoreCustomer

@AppScope
@Suppress("FunctionName")
class CustomerRepo @Inject constructor(
    private val customerDao: Lazy<CustomerDao>,
    private val coreSdk: Lazy<CoreSdk>,
    private val dueInfoRepo: Lazy<DueInfoRepo>,
    private val remoteSource: Lazy<BackendRemoteSource>,
) {
    companion object {
        // in-memory cache
        private var backend_listCustomersCache: HashMap<String, BehaviorSubject<List<Customer>>> = HashMap()
        private var backend_listActiveCustomersCache: HashMap<String, BehaviorSubject<List<Customer>>> = HashMap()
        private var core_listCustomersCache: HashMap<String, BehaviorSubject<List<Customer>>> = HashMap()
        private var core_listActiveCustomersCache: HashMap<String, BehaviorSubject<List<Customer>>> = HashMap()
        private var backend_listAccountFeaturesCache: HashMap<String, BehaviorSubject<MutableMap<String, Boolean>>> =
            HashMap()
    }

    fun findCustomerByMobile(mobile: String?, businessId: String): Single<Customer> {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMap {
                if (it) {
                    coreFindCustomerByMobile(mobile, businessId)
                } else {
                    backendFindCustomerByMobile(mobile, businessId)
                }
            }
    }

    // Saving Customer List In Memory
    fun listCustomers(businessId: String): Observable<List<Customer>> {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    coreListcustomers(businessId)
                } else {
                    backendListCustomers(businessId)
                }
            }
    }

    fun getCustomersWithBalanceDue(businessId: String) = coreSdk.get().isCoreSdkFeatureEnabled(businessId)
        .flatMapObservable {
            if (it) {
                coreCustomersWithBalanceDue(businessId)
            } else {
                backendCustomersWithBalanceDue(businessId)
            }
        }

    private fun backendCustomersWithBalanceDue(businessId: String): Observable<List<Customer>> {
        return customerDao.get().getCustomersWithBalanceDue(businessId)
            .distinctUntilChanged()
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { Utils.mapList(it, DbEntityMapper.CustomerWithTransactionView(businessId)) }
    }

    private fun coreCustomersWithBalanceDue(businessId: String) =
        coreGetCustomerObservable(coreSdk.get().getCustomersWithBalanceDue(businessId), businessId)

    // Saving Customer List In Memory
    fun getDefaultersList(businessId: String): Observable<List<Customer>> {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    coreDefaulters(businessId)
                } else {
                    backendDefaulters(businessId)
                }
            }
    }

    private fun backendDefaulters(businessId: String): Observable<List<Customer>> {
        return customerDao.get().getDefaulters(businessId)
            .distinctUntilChanged()
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { Utils.mapList(it, DbEntityMapper.CustomerWithTransactionView(businessId)) }
            .toObservable()
    }

    private fun coreDefaulters(businessId: String) =
        coreGetCustomerObservable(coreSdk.get().getDefaulters(businessId), businessId)

    // Saving Customer List In Memory
    fun listCustomersByLastPayment(businessId: String): Observable<List<Customer>> {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    coreListcustomersbylastpayment(businessId)
                } else {
                    backendListCustomersByLastPayment(businessId)
                }
            }
    }

    // Saving List Customer List In Memory
    fun listActiveCustomers(businessId: String): Observable<List<Customer>> {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    coreListActiveCustomers(businessId)
                } else {
                    backendListActiveCustomers(businessId)
                }
            }
    }

    fun listActiveCustomersIds(businessId: String): Observable<List<String>> {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    core_listActiveCustomersIds(businessId)
                } else {
                    backend_listActiveCustomersIds(businessId)
                }
            }
    }

    // Saving List Customer List In Memory
    fun getLiveSalesCustomerId(businessId: String): Single<String> {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMap {
                if (it) {
                    coreGetLiveSalesCustomerId(businessId)
                } else {
                    backendGetLiveSalesCustomerId(businessId)
                }
            }
    }

    private fun backendGetLiveSalesCustomerId(businessId: String): Single<String> {
        return customerDao.get().getLiveSalesCustomerId(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    private fun coreGetLiveSalesCustomerId(businessId: String): SingleSource<out String>? {
        return coreSdk.get().getLiveSalesCustomerId(businessId)
    }

    fun getCustomersCount(businessId: String): Observable<Int> {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    coreGetCustomersCount(businessId)
                } else {
                    backendGetCustomersCount(businessId)
                }
            }
    }

    fun getActiveCustomerCount(businessId: String): Observable<Long> {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    coreGetActiveCustomersCount(businessId)
                } else {
                    backendGetActiveCustomersCount(businessId)
                }
            }
    }

    // Returning customer object from customer list cache
    fun getCustomer(customerId: String?, businessId: String): Observable<Customer> {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    coreGetCustomer(customerId, businessId)
                } else {
                    backendGetCustomer(customerId, businessId)
                }
            }
    }

    fun putCustomer(customer: Customer, businessId: String): Completable {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapCompletable {
                if (it) {
                    corePutCustomer(customer, businessId)
                } else {
                    backendPutCustomer(customer, businessId)
                }
            }
    }

    suspend fun getIsBlocked(businessId: String, customerId: String): Boolean {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId).await()
            .let { enabled ->
                if (enabled) {
                    coreIsblocked(businessId, customerId)
                } else {
                    backendIsblocked(businessId, customerId)
                }
            }
    }

    suspend fun getIsAddTransactionRestricted(businessId: String, customerId: String): Boolean {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId).await()
            .let { enabled ->
                if (enabled) {
                    core_isAddTransactionRestricted(businessId, customerId)
                } else {
                    backend_isAddTransactionRestricted(businessId, customerId)
                }
            }
    }

    fun markActivityAsSeen(customerId: String?, businessId: String): Completable {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapCompletable {
                if (it) {
                    core_markActivityAsSeen(customerId)
                } else {
                    backend_markActivityAsSeen(customerId)
                }
            }
    }

    // DANGEROUS: replaces existing customers of same id
    fun resetCustomerList(customers: List<Customer>, businessId: String): Completable {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapCompletable {
                if (it) {
                    coreResetCustomerList(customers, businessId)
                } else {
                    backendResetCustomerList(customers, businessId)
                }
            }
    }

    fun deleteCustomer(accountId: String): Completable {
        return Completable
            .fromAction { customerDao.get().deleteCustomer(accountId) }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun updateDescription(name: String, accountId: String, businessId: String): Completable {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapCompletable {
                if (it) {
                    core_updateDescription(name, accountId)
                } else {
                    backend_updateDescription(name, accountId)
                }
            }
    }
    // Backend =====================

    private fun backendFindCustomerByMobile(mobile: String?, businessId: String): Single<Customer> {
        return customerDao.get().findCustomerByMobile(businessId, mobile)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { customer -> DbEntityMapper.CustomerWithTransactionView(businessId).convert(customer)!! }
            .onErrorResumeNext {
                RecordException.recordException(it)
                if (it is EmptyResultSetException) {
                    Single.error<Customer>(NoSuchElementException())
                } else {
                    Single.error<Customer>(RuntimeException(it))
                }
            }
    }

    // Saving Customer List In Memory
    private fun backendListCustomers(businessId: String): Observable<List<Customer>> {
        val value = System.currentTimeMillis() % 1000
        Timber.d("<<<<CustomerRepo listCustomers: Started :%s", value.toString())
        if (backend_listCustomersCache.containsKey(businessId).not()) {
            backend_listCustomersCache[businessId] = BehaviorSubject.create()
            customerDao.get().listCustomers(businessId)
                .distinctUntilChanged()
                .subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
                .map { Utils.mapList(it, DbEntityMapper.CustomerWithTransactionView(businessId)) }
                .subscribe {
                    Timber.d("<<<<CustomerRepo listCustomers: Ended :%s", value.toString())
                    backend_listCustomersCache[businessId]!!.onNext(it)
                }
        }
        return backend_listCustomersCache[businessId]!!.observeOn(ThreadUtils.worker())
    }

    private fun backend_listActiveCustomersIds(businessId: String): Observable<List<String>> {
        return customerDao.get().listActiveCustomersIds(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    // Saving Customer List In Memory
    private fun backendListCustomersByLastPayment(businessId: String): Observable<List<Customer>> {
        if (backend_listCustomersCache.containsKey(businessId).not()) {
            backend_listCustomersCache[businessId] = BehaviorSubject.create()
            customerDao.get().listCustomersByLastPayment(businessId)
                .distinctUntilChanged()
                .subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
                .map { Utils.mapList(it, DbEntityMapper.CustomerWithTransactionView(businessId)) }
                .subscribe {
                    backend_listCustomersCache[businessId]!!.onNext(it)
                }
        }
        return backend_listCustomersCache[businessId]!!.observeOn(ThreadUtils.worker())
    }

    // Saving List Customer List In Memory
    private fun backendListActiveCustomers(businessId: String): Observable<List<Customer>> {
        val value = System.currentTimeMillis() % 1000
        Timber.d("<<<<CustomerRepo %s listActiveCustomers: Started", value.toString())
        if (backend_listActiveCustomersCache.containsKey(businessId).not()
        ) {
            val traceGetCustomerList = FirebasePerformance.getInstance().newTrace(Traces.Trace_GetActiveCustomerList)
            traceGetCustomerList.start()
            var isFirstTraceeEnd = false
            Timber.d("<<<<CustomerRepo %s listActiveCustomers: init", value.toString())
            backend_listActiveCustomersCache[businessId] = BehaviorSubject.create()
            customerDao.get().listActiveCustomers(businessId)
                .distinctUntilChanged()
                .subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
                .map { Utils.mapList(it, DbEntityMapper.CustomerWithTransactionView(businessId)) }
                .subscribe { customers: List<Customer> ->
                    if (isFirstTraceeEnd.not()) {
                        isFirstTraceeEnd = true
                        traceGetCustomerList.putAttribute("CustomerSize", customers.size.toString())
                        traceGetCustomerList.stop()
                    }
                    Timber.d("<<<<CustomerRepo %s listActiveCustomers: subscribe", value.toString())
                    backend_listActiveCustomersCache[businessId]!!.onNext(customers)
                }
        }
        return backend_listActiveCustomersCache[businessId]!!.observeOn(ThreadUtils.worker())
            .doOnNext { Timber.d("<<<<CustomerRepo %s listActiveCustomers: Emitted", value.toString()) }
    }

    private fun backendGetCustomersCount(businessId: String): Observable<Int> {
        return customerDao.get().getCustomersCount(businessId)
    }

    private fun backendGetActiveCustomersCount(businessId: String): Observable<Long> {
        return customerDao.get().countActiveCustomers(businessId)
    }

    private fun backend_getCustomerFromCache(customerId: String?, businessId: String): Customer? {
        backend_listActiveCustomersCache[businessId]?.value?.map {
            if (it.id == customerId) {
                return it
            }
        }
        return null
    }

    // Returning customer object from customer list cache
    private fun backendGetCustomer(customerId: String?, businessId: String): Observable<Customer> {
        val customerFromCache = backend_getCustomerFromCache(customerId, businessId)
        return if (customerFromCache != null) {
            customerDao.get().getCustomerWithTransactionInfo(businessId = businessId, customerId = customerId)
                .subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
                .map { customer -> DbEntityMapper.CustomerWithTransactionView(businessId).convert(customer)!! }
                .doOnError { throwable: Throwable? ->
                    throwable?.let {
                        RecordException.recordException(it)
                    }
                    Timber.i(throwable)
                }
                .toObservable().startWith(customerFromCache)
        } else {
            customerDao.get().getCustomerWithTransactionInfo(businessId = businessId, customerId = customerId)
                .subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
                .map { customer -> DbEntityMapper.CustomerWithTransactionView(businessId).convert(customer)!! }
                .doOnError { throwable: Throwable? ->
                    throwable?.let {
                        RecordException.recordException(it)
                        Timber.i(throwable)
                    }
                }
                .toObservable()
        }
    }

    private fun backendPutCustomer(customer: Customer, businessId: String): Completable {
        return customerDao.get().getCustomer(customer.id)
            .onErrorResumeNext {
                RecordException.recordException(it)
                Single.just(DbEntities.Customer())
            }
            .flatMapCompletable { existingCustomer: DbEntities.Customer? ->
                Completable
                    .fromAction {
                        if (existingCustomer?.lastViewTime != null && existingCustomer.lastViewTime.millis != 0L && existingCustomer.id == customer.id) {
                            customer.lastViewTime = existingCustomer.lastViewTime
                        } else {
                            customer.lastViewTime = DateTimeUtils.currentDateTime().minusSeconds(10)
                        }
                        customerDao.get().putCustomer(DbEntityMapper.CUSTOMER(businessId).convert(customer))
                    }
                    .subscribeOn(ThreadUtils.database())
                    .observeOn(ThreadUtils.worker())
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    private suspend fun backendIsblocked(businessId: String, customerId: String): Boolean {
        return customerDao.get().getState(businessId, customerId)
            .let { it == Customer.State.BLOCKED.value }
    }

    private suspend fun backend_isAddTransactionRestricted(businessId: String, customerId: String): Boolean {
        return customerDao.get().getIsAddTransactionRestricted(businessId, customerId)
    }

    private fun backend_markActivityAsSeen(customerId: String?): Completable {
        return Completable
            .fromAction { customerDao.get().updateLastViewTime(customerId, DateTimeUtils.currentDateTime()) }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    // DANGEROUS: replaces existing customers of same id
    private fun backendResetCustomerList(customers: List<Customer>, businessId: String): Completable {
        Timber.d("<<<<resetCustomerList started")
        return customerDao.get().getCustomers(businessId)
            .onErrorReturnItem(ArrayList<DbEntities.Customer>())
            .firstOrError()
            .flatMapCompletable { existingCustomers: List<DbEntities.Customer> ->
                Completable.fromAction {
                    Timber.d("<<<<resetCustomerList existingCustomers Count: %d", existingCustomers.size)
                    val map = mutableMapOf<String, DbEntities.Customer>()
                    existingCustomers.forEach {
                        map[it.id] = it
                    }
                    customers.forEach {
                        if (map.containsKey(it.id)) {
                            it.lastViewTime = map[it.id]!!.lastViewTime
                        }
                    }
                    val list: Array<DbEntities.Customer> =
                        Utils.mapList(customers, DbEntityMapper.CUSTOMER(businessId)).toTypedArray()
                    customerDao.get().resetCustomerList(*list)
                }
                    .subscribeOn(ThreadUtils.database())
                    .observeOn(ThreadUtils.worker())
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    // DANGEROUS: clears entire customer list
    fun clear(): Completable {
        return Completable
            .fromAction { customerDao.get().deleteAllCustomers() }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun backend_updateDescription(name: String, accountId: String): Completable {
        return Completable
            .fromAction { customerDao.get().updateDescription(name, accountId) }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }
    // Core =====================

    private fun coreFindCustomerByMobile(mobile: String?, businessId: String): Single<Customer> {
        return coreGetCustomerByMobileSingle(mobile!!, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .onErrorResumeNext {
                RecordException.recordException(it)
                Single.error<Customer>(it)
            }
    }

    // Saving Customer List In Memory
    private fun coreListcustomers(businessId: String): Observable<List<Customer>> {
        val value = System.currentTimeMillis() % 1000
        Timber.d("<<<<CustomerRepo listCustomers: Started :%s", value.toString())
        if (core_listCustomersCache.containsKey(businessId).not()) {
            core_listCustomersCache[businessId] = BehaviorSubject.create()
            coreGetCustomerObservable(coreSdk.get().listActiveCustomers(businessId), businessId)
                .distinctUntilChanged()
                .subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
                .subscribe {
                    Timber.d("<<<<CustomerRepo listCustomers: Ended :%s", value.toString())
                    core_listCustomersCache[businessId]!!.onNext(it)
                }
        }
        return core_listCustomersCache[businessId]!!.observeOn(ThreadUtils.worker())
    }

    // Saving Customer List In Memory
    private fun coreListcustomersbylastpayment(businessId: String): Observable<List<Customer>> {
        if (core_listCustomersCache.containsKey(businessId).not()) {
            core_listCustomersCache[businessId] = BehaviorSubject.create()
            coreGetCustomerObservable(coreSdk.get().listCustomersByLastPayment(businessId), businessId)
                .distinctUntilChanged()
                .subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
                .subscribe {
                    core_listCustomersCache[businessId]!!.onNext(it)
                }
        }
        return core_listCustomersCache[businessId]!!.observeOn(ThreadUtils.worker())
    }

    // Saving List Customer List In Memory
    private fun coreListActiveCustomers(businessId: String): Observable<List<Customer>> {
        val value = System.currentTimeMillis() % 1000
        Timber.d("<<<<CustomerRepo %s listActiveCustomers: Started", value.toString())
        if (core_listActiveCustomersCache.containsKey(businessId).not()) {
            val traceGetCustomerList =
                FirebasePerformance.getInstance().newTrace(Traces.Trace_GetActiveCustomerListFromCoreModule)
            traceGetCustomerList.start()
            var isFirstTraceeEnd = false
            Timber.d("<<<<CustomerRepo %s listActiveCustomers: init", value.toString())
            core_listActiveCustomersCache[businessId] = BehaviorSubject.create()
            coreGetCustomerObservable(coreSdk.get().listActiveCustomers(businessId), businessId)
                .distinctUntilChanged()
                .subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
                .subscribe { customers: List<Customer> ->
                    if (isFirstTraceeEnd.not()) {
                        isFirstTraceeEnd = true
                        traceGetCustomerList.putAttribute("CustomerSize", customers.size.toString())
                        traceGetCustomerList.stop()
                    }
                    Timber.d("<<<<CustomerRepo %s listActiveCustomers: subscribe", value.toString())
                    core_listActiveCustomersCache[businessId]!!.onNext(customers)
                }
        }
        return core_listActiveCustomersCache[businessId]!!.observeOn(ThreadUtils.worker())
            .doOnNext { Timber.d("<<<<CustomerRepo %s listActiveCustomers: Emitted", value.toString()) }
    }

    private fun core_listActiveCustomersIds(businessId: String): Observable<List<String>> {
        return coreSdk.get().listActiveCustomersIds(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    private fun coreGetCustomerObservable(
        coreCustomerObservable: Observable<List<CoreCustomer>>,
        businessId: String,
    ): Observable<List<Customer>> {
        return Observable.combineLatest(
            coreCustomerObservable,
            dueInfoRepo.get().getAllCustomerDueInfo(businessId),

            BiFunction { customerList, dueInfoList ->
                val map = dueInfoList.associateBy { it.customerId }
                return@BiFunction customerList.map {
                    val dueInfo = map[it.id] ?: DueInfo(it.id)
                    it.toCustomer(dueInfo)
                }
            }
        )
    }

    private fun coreGetCustomerByIdObservable(customerId: String, businessId: String): Observable<Customer> {
        return dueInfoRepo.get().isDueInfoExists(customerId).toObservable()
            .flatMap { dueInfoExists ->
                if (dueInfoExists) {
                    Observable.combineLatest(
                        coreSdk.get().getCustomer(customerId),
                        dueInfoRepo.get().getDueInfoForCustomer(customerId, businessId),
                        BiFunction { customer, dueInfo ->
                            customer.toCustomer(dueInfo)
                        }
                    )
                } else {
                    coreSdk.get().getCustomer(customerId).map { customer ->
                        (CoreModuleMapper::toCustomer)(customer)
                    }
                }
            }
    }

    private fun coreGetCustomerByMobileSingle(mobile: String, businessId: String): Single<Customer> {
        return coreSdk.get().getCustomerByMobile(mobile, businessId)
            .flatMap { customer ->
                dueInfoRepo.get().getDueInfoForCustomer(customer.id, businessId)
                    .firstOrError()
                    .map { customer.toCustomer(it) }
            }
    }

    private fun coreGetCustomersCount(businessId: String): Observable<Int> {
        return coreSdk.get().getCustomerCount(businessId)
    }

    private fun coreGetActiveCustomersCount(businessId: String): Observable<Long> {
        return coreSdk.get().getActiveCustomerCount(businessId)
    }

    private fun core_getCustomerFromCache(customerId: String?, businessId: String): Customer? {
        core_listActiveCustomersCache[businessId]?.value?.map {
            if (it.id == customerId) {
                return it
            }
        }
        return null
    }

    // Returning customer object from customer list cache
    private fun coreGetCustomer(customerId: String?, businessId: String): Observable<Customer> {
        val customerFromCache = core_getCustomerFromCache(customerId, businessId)
        return if (customerFromCache != null) {
            coreGetCustomerByIdObservable(customerId!!, businessId)
                .subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
                .doOnError { throwable: Throwable? ->
                    throwable?.let {
                        RecordException.recordException(it)
                        Timber.i(throwable)
                    }
                }
                .startWith(customerFromCache)
        } else {
            coreGetCustomerByIdObservable(customerId!!, businessId)
                .subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
                .doOnError { throwable: Throwable? ->
                    throwable?.let {
                        RecordException.recordException(it)
                        Timber.i(throwable)
                    }
                }
        }
    }

    private fun corePutCustomer(customer: Customer, businessId: String): Completable {
        return customerDao.get().getCustomer(customer.id)
            .onErrorResumeNext {
                RecordException.recordException(it)
                Single.just(DbEntities.Customer())
            }
            .flatMapCompletable { existingCustomer: DbEntities.Customer? ->
                Completable
                    .fromAction {
                        if (existingCustomer?.lastViewTime != null &&
                            existingCustomer.lastViewTime.millis != 0L &&
                            existingCustomer.id == customer.id
                        ) {
                            customer.lastViewTime = existingCustomer.lastViewTime
                        } else {
                            customer.lastViewTime = DateTimeUtils.currentDateTime().minusSeconds(10)
                        }
                        customerDao.get().putCustomer(DbEntityMapper.CUSTOMER(businessId).convert(customer))
                    }
                    .subscribeOn(ThreadUtils.database())
                    .observeOn(ThreadUtils.worker())
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    private suspend fun coreIsblocked(businessId: String, customerId: String): Boolean {
        return coreSdk.get().getIsBlocked(businessId, customerId)
    }

    private suspend fun core_isAddTransactionRestricted(businessId: String, customerId: String): Boolean {
        return coreSdk.get().getIsAddTransactionRestricted(businessId, customerId)
    }

    private fun core_markActivityAsSeen(customerId: String?): Completable {
        return coreSdk.get().markActivityAsSeen(customerId!!)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    // DANGEROUS: replaces existing customers of same id
    private fun coreResetCustomerList(customers: List<Customer>, businessId: String): Completable {
        Timber.d("<<<<resetCustomerList started")
        return customerDao.get().getCustomers(businessId)
            .onErrorReturnItem(ArrayList<DbEntities.Customer>())
            .firstOrError()
            .flatMapCompletable { existingCustomers: List<DbEntities.Customer> ->
                Completable.fromAction {
                    Timber.d("<<<<resetCustomerList existingCustomers Count: %d", existingCustomers.size)
                    val map = mutableMapOf<String, DbEntities.Customer>()
                    existingCustomers.forEach {
                        map[it.id] = it
                    }
                    customers.forEach {
                        if (map.containsKey(it.id)) {
                            it.lastViewTime = map[it.id]!!.lastViewTime
                        }
                    }
                    val list: Array<DbEntities.Customer> =
                        Utils.mapList(customers, DbEntityMapper.CUSTOMER(businessId)).toTypedArray()
                    customerDao.get().resetCustomerList(*list)
                }
                    .subscribeOn(ThreadUtils.database())
                    .observeOn(ThreadUtils.worker())
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun core_updateDescription(name: String, accountId: String): Completable {
        return coreSdk.get().updateLocalCustomerDescription(name, accountId)
    }

    fun allCustomersBuyerTxnAlertFeatureList(businessId: String): Observable<MutableMap<String, Boolean>> {
        if (backend_listAccountFeaturesCache.containsKey(businessId).not()
        ) {
            backend_listAccountFeaturesCache[businessId] = BehaviorSubject.create()

            serverGetAllBuyerTxnAlertConfig(businessId)
        }
        return backend_listAccountFeaturesCache[businessId]!!.observeOn(ThreadUtils.worker())
    }

    private fun serverGetAllBuyerTxnAlertConfig(businessId: String) {
        remoteSource.get().getAllAccountBuyerTxnAlertConfig(businessId)
            .subscribe { list: AllAccountsBuyerTxnAlertConfigResponse?, t2: Throwable? ->
                list?.let {
                    val map = hashMapOf<String, Boolean>()
                    list.accountFeatures.forEach {
                        map[it.accountId] = it.buyerTxnAlert
                    }
                    backend_listAccountFeaturesCache[businessId]!!.onNext(map)
                }
            }
    }

    fun getCustomerTxnAlertMap(businessId: String): MutableMap<String, Boolean>? {
        return backend_listAccountFeaturesCache[businessId]?.value
    }

    fun updateBuyerMap(map: MutableMap<String, Boolean>, businessId: String) {
        backend_listAccountFeaturesCache[businessId]?.onNext(map)
    }

    fun updateAddTransactionRestrictedLocally(accountID: String, businessId: String): Completable {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapCompletable {
                if (it) {
                    coreUpdateCustomerAddTransactionPermission(accountID)
                } else {
                    backendUpdateCustomerAddTransactionPermission(accountID)
                }
            }
    }

    private fun coreUpdateCustomerAddTransactionPermission(accountID: String): Completable {
        return coreSdk.get().coreUpdateCustomerAddTransactionPermission(accountID, true)
    }

    private fun backendUpdateCustomerAddTransactionPermission(accountID: String): Completable {
        return Completable
            .fromAction { customerDao.get().updateCustomerAddTransaction(accountID, true) }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun invalidateAllCustomersBuyerTxnAlertFeatureList(businessId: String): Completable {
        return remoteSource.get().getAllAccountBuyerTxnAlertConfig(businessId).flatMapCompletable { list ->
            list.let {
                val map = hashMapOf<String, Boolean>()
                list.accountFeatures.forEach {
                    map[it.accountId] = it.buyerTxnAlert
                }
                if (backend_listAccountFeaturesCache.containsKey(businessId).not()
                ) {
                    backend_listAccountFeaturesCache[businessId] = BehaviorSubject.create()
                }
                backend_listAccountFeaturesCache[businessId]!!.onNext(map)
                Completable.complete()
            }
        }
    }

    fun getSpecificCustomerList(customerIdList: List<String>, businessId: String): Observable<List<Customer>> {
        return listActiveCustomers(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map {
                it.filter { customer ->
                    customer.id in customerIdList
                }
            }
    }

    // Bulk Reminder V2
    // todo(bonus) move bulk reminder in seperate repository

    fun getDefaultersDataForBanner(defaulterSince: String, businessId: String): Flow<BulkReminderDbInfo> {
        return coreSdk.get().isCoreSdkFeatureEnabledFlow(businessId).flatMapLatest { enabled ->
            if (enabled) {
                coreSdk.get().getDefaultersDataForBanner(defaulterSince, businessId)
            } else {
                backendGetDefaultersDataForBanner(defaulterSince, businessId)
            }
        }
    }

    private fun backendGetDefaultersDataForBanner(
        defaulterSince: String,
        businessId: String,
    ): Flow<BulkReminderDbInfo> {
        return customerDao.get().getDefaultersDataForBanner(defaulterSince, businessId)
    }

    fun getDefaultersForPendingReminders(
        businessId: String,
        defaulterSince: String,
    ): Flow<List<DbReminderProfile>> {
        return coreSdk.get().isCoreSdkFeatureEnabledFlow(businessId).flatMapLatest { enabled ->
            if (enabled) {
                coreSdk.get().getDefaultersForPendingReminders(defaulterSince, businessId)
                    .map {
                        it.toBackendReminderProfileList()
                    }
            } else {
                backendGetDefaultersForPendingReminders(defaulterSince, businessId)
            }
        }
    }

    private fun backendGetDefaultersForPendingReminders(
        defaulterSince: String,
        businessId: String,
    ): Flow<List<DbReminderProfile>> {
        return customerDao.get().getDefaultersForPendingReminders(defaulterSince, businessId)
    }

    fun getDefaultersForTodaysReminders(
        businessId: String,
        defaulterSince: String,
    ): Flow<List<DbReminderProfile>> {
        return coreSdk.get().isCoreSdkFeatureEnabledFlow(businessId).flatMapLatest { enabled ->
            if (enabled) {
                coreSdk.get().getDefaultersForTodaysReminders(defaulterSince, businessId)
                    .map {
                        it.toBackendReminderProfileList()
                    }
            } else {
                backendGetDefaultersForTodaysReminders(businessId, defaulterSince)
            }
        }
    }

    private fun backendGetDefaultersForTodaysReminders(
        businessId: String,
        defaulterSince: String,
    ): Flow<List<DbReminderProfile>> {
        return customerDao.get().getDefaultersForTodaysReminders(defaulterSince, businessId)
    }

    suspend fun updateLastReminderSentTime(businessId: String, customerId: String, lastReminderSentTime: DateTime) {
        withContext(Dispatchers.IO) {
            val enabled = coreSdk.get().isCoreSdkFeatureEnabledFlow(businessId).first()
            if (enabled) {
                val convertDateTime = CoreTimestamp(lastReminderSentTime.millis)
                coreSdk.get().updateLastReminderSendTime(customerId, convertDateTime, businessId)
            } else {
                backendUpdateLastReminderSentTime(businessId, customerId, lastReminderSentTime)
            }
        }
    }

    private suspend fun backendUpdateLastReminderSentTime(
        businessId: String,
        customerId: String,
        lastReminderSentTime: DateTime,
    ) {
        withContext(Dispatchers.IO) {
            customerDao.get().updateLastReminderSentTime(businessId, customerId, lastReminderSentTime)
        }
    }

    suspend fun getDirtyLastReminderSendTime(
        businessId: String,
        customerIds: List<String>,
    ): Flow<List<LastReminderSendTime>> {
        return coreSdk.get().isCoreSdkFeatureEnabledFlow(businessId).map { enabled ->
            if (enabled) {
                coreSdk.get().getDirtyLastReminderSendTime(customerIds, businessId).toLastReminderSendTime()
            } else {
                backendGetDirtyLastReminderSendTime(businessId, customerIds)
            }
        }
    }

    private suspend fun backendGetDirtyLastReminderSendTime(
        businessId: String,
        customerIds: List<String>,
    ): List<LastReminderSendTime> {
        return customerDao.get().getDirtyLastReminderSendTime(customerIds, businessId)
            .convertBackendToLastReminderSendTime()
    }
}
