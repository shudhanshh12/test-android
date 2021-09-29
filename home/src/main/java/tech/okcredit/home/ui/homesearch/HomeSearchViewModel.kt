package tech.okcredit.home.ui.homesearch

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.PropertyValue.CUSTOMER
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.error.CustomerErrors
import `in`.okcredit.backend._offline.usecase.AddCustomer
import `in`.okcredit.backend._offline.usecase.AddSupplier
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend._offline.usecase.GetUnSyncedCustomers
import `in`.okcredit.backend.contract.Features
import `in`.okcredit.collection.contract.GetKycRiskCategory
import `in`.okcredit.collection.contract.OnlineCollectionNotification
import `in`.okcredit.home.GetSupplierCreditEnabledCustomerIds
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.rx2.awaitFirst
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.home.R
import tech.okcredit.home.ui.home.views.HomeSupplierView
import tech.okcredit.home.ui.homesearch.HomeSearchContract.*
import tech.okcredit.home.usecase.GetHomeSearchData
import tech.okcredit.home.usecase.GetUnSyncSupplier
import tech.okcredit.home.usecase.IsPermissionGranted
import javax.inject.Inject

class HomeSearchViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam(HomeSearchFragment.ARG_SOURCE) val source: Lazy<String>,
    @ViewModelParam(HomeSearchFragment.ACCOUNT_SELECTION) val isAccountSelection: Lazy<Boolean>,
    private val getHomeSearchData: Lazy<GetHomeSearchData>,
    private val getSupplierCreditEnabledCustomerIds: Lazy<GetSupplierCreditEnabledCustomerIds>,
    private val getUnSyncedCustomers: Lazy<GetUnSyncedCustomers>,
    private val isPermissionGranted: Lazy<IsPermissionGranted>,
    private val addCustomer: Lazy<AddCustomer>,
    private val addSupplier: Lazy<AddSupplier>,
    private val tracker: Lazy<Tracker>,
    private val getUnSyncedSuppliers: Lazy<GetUnSyncSupplier>,
    private val contactsRepository: Lazy<ContactsRepository>,
    private val getKycRiskCategory: Lazy<GetKycRiskCategory>,
    private val getPaymentReminderIntent: Lazy<GetPaymentReminderIntent>,
    private val abRepository: Lazy<AbRepository>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    private val onSearchPublishSubject: BehaviorSubject<String> = BehaviorSubject.create()
    private val addSupplierPublishSubject: BehaviorSubject<AddSupplierRequest?> = BehaviorSubject.create()
    private val addCustomerPublishSubject: BehaviorSubject<AddCustomerRequest?> = BehaviorSubject.create()
    private val resetData: BehaviorSubject<Unit> = BehaviorSubject.createDefault(Unit)

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(

            setViewModelParams(),

            observeSearchDataChanges(),

            observeUnSyncedSuppliers(),

            observeSupplierCreditEnabledCustomerIds(),

            observeUnSyncedCustomers(),

            observeSearch(),

            observeImportContact(),

            observeContactPermission(),

            observeLoadForContactSyncWithPhonebook(),

            observeAddRelationFromContact(),

            observeAddRelationFromSearch(),

            observeAddCustomerResult(),

            observeAddSupplierResult(),

            observeResetData(),

            getKycRiskCategory(),

            setSearchVisibility(),

            observeOnlinePaymentReceived(),

            sendCustomerReminder(),

            observeShowCustomerQr(),
        )
    }

    private fun observeOnlinePaymentReceived() = intent<Intent.Load>()
        .switchMap { OnlineCollectionNotification.toObservable() }
        .map {
            emitViewEvent(ViewEvent.ShowPaymentReceived(it.amount, it.customerId))
            PartialState.NoChange
        }

    private fun observeAddCustomerResult(): Observable<PartialState> {
        return addCustomerPublishSubject
            .switchMap { UseCase.wrapSingle(addCustomer.get().execute(it.name, it.mobile, it.contactUrl)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        tracker.get().trackAddRelationshipSuccessFlows(
                            relation = PropertyValue.CUSTOMER,
                            accountId = it.value.id,
                            search = PropertyValue.TRUE,
                            contact = addCustomerPublishSubject.value?.isContact.toString()
                        )
                        if (isAccountSelection.get()) {
                            emitViewEvent(ViewEvent.ReturnAccountSelectionResult(it.value.id, CUSTOMER))
                        } else {
                            emitViewEvent(
                                ViewEvent.GoToCustomerScreenAndCloseSearch(
                                    it.value.id,
                                    it.value.mobile
                                )
                            )
                        }
                        PartialState.NoChange
                    }
                    is Result.Failure -> {
                        when {
                            it.error is CustomerErrors.InvalidMobile -> {
                                emitViewEvent(ViewEvent.ShowInvalidMobileNumber)
                            }

                            it.error is CustomerErrors.MobileConflict -> {
                                emitViewEvent(
                                    ViewEvent.ShowMobileConflictForCustomer(
                                        (it.error as CustomerErrors.MobileConflict).conflict
                                    )
                                )
                            }

                            it.error is CustomerErrors.ActiveCyclicAccount -> {
                                emitViewEvent(
                                    ViewEvent.ShowCyclicAccountForSupplier(
                                        (it.error as CustomerErrors.ActiveCyclicAccount).conflict
                                    )
                                )
                            }

                            it.error is CustomerErrors.DeletedCyclicAccount -> {
                                emitViewEvent(
                                    ViewEvent.ShowCyclicAccountForDeletedSupplier(
                                        (it.error as CustomerErrors.DeletedCyclicAccount).conflict
                                    )
                                )
                            }

                            it.error is CustomerErrors.InvalidName -> {
                                emitViewEvent(ViewEvent.ShowInvalidName)
                            }
                            isInternetIssue(it.error) -> {
                                emitViewEvent(ViewEvent.ShowInternetError)
                            }
                            else -> {
                                emitViewEvent(ViewEvent.ShowError)
                            }
                        }
                        PartialState.NoChange
                    }
                }
            }
    }

    private fun observeAddSupplierResult(): Observable<PartialState> {
        return addSupplierPublishSubject
            .switchMap {
                UseCase.wrapSingle(
                    addSupplier.get().execute(
                        AddSupplier.Request(
                            it.name,
                            it.mobile,
                            it.contactUrl
                        )
                    )
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        tracker.get().trackAddRelationshipSuccessFlows(
                            relation = PropertyValue.SUPPLIER,
                            accountId = it.value.id,
                            search = PropertyValue.TRUE,
                            contact = addSupplierPublishSubject.value?.isContact.toString()
                        )
                        if (isAccountSelection.get()) {
                            emitViewEvent(ViewEvent.ReturnAccountSelectionResult(it.value.id, PropertyValue.SUPPLIER))
                        } else {
                            emitViewEvent(ViewEvent.GoToSupplierScreenAndCloseSearch(it.value))
                        }
                        PartialState.NoChange
                    }
                    is Result.Failure -> {
                        when {
                            it.error is SupplierCreditServerErrors.InvalidMobile -> {
                                emitViewEvent(ViewEvent.ShowInvalidMobileNumber)
                            }
                            it.error is SupplierCreditServerErrors.MobileConflict -> {
                                emitViewEvent(
                                    ViewEvent.ShowMobileConflictForSupplier(
                                        (it.error as SupplierCreditServerErrors.MobileConflict).getSupplier()
                                    )
                                )
                            }
                            it.error is SupplierCreditServerErrors.ActiveCyclicAccount -> {
                                emitViewEvent(
                                    ViewEvent
                                        .ShowCyclicAccount(
                                            (it.error as SupplierCreditServerErrors.ActiveCyclicAccount).getInfo()
                                        )
                                )
                            }
                            it.error is SupplierCreditServerErrors.DeletedCyclicAccount -> {
                                emitViewEvent(
                                    ViewEvent.ShowCyclicAccountForDeletedCustomer(
                                        (it.error as SupplierCreditServerErrors.DeletedCyclicAccount).getInfo()
                                    )
                                )
                            }
                            it.error is SupplierCreditServerErrors.InvalidName -> {
                                emitViewEvent(ViewEvent.ShowInvalidName)
                            }
                            isInternetIssue(it.error) -> {
                                emitViewEvent(ViewEvent.ShowInternetError)
                            }
                            else -> {
                                emitViewEvent(ViewEvent.ShowError)
                            }
                        }
                        PartialState.NoChange
                    }
                }
            }
    }

    private fun observeAddRelationFromContact(): Observable<PartialState> {
        return intent<Intent.AddRelationFromContact>()
            .map {
                if (it.source == SOURCE.HOME_CUSTOMER_TAB) {
                    addCustomerPublishSubject.onNext(
                        AddCustomerRequest(it.contact.name, it.contact.mobile, it.contact.picUri, true)
                    )
                } else if (it.source == SOURCE.HOME_SUPPLIER_TAB) {
                    addSupplierPublishSubject.onNext(
                        AddSupplierRequest(it.contact.name, it.contact.mobile, it.contact.picUri, true)
                    )
                }
                PartialState.NoChange
            }
    }

    private fun observeAddRelationFromSearch(): Observable<PartialState> {
        return intent<Intent.AddRelationFromSearch>()
            .map {
                if (it.source == SOURCE.HOME_CUSTOMER_TAB) {
                    addCustomerPublishSubject.onNext(
                        AddCustomerRequest(it.query, null, null, false)
                    )
                } else if (it.source == SOURCE.HOME_SUPPLIER_TAB) {
                    addSupplierPublishSubject.onNext(
                        AddSupplierRequest(it.query, null, null, false)
                    )
                }
                PartialState.NoChange
            }
    }

    private fun observeContactPermission(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { isPermissionGranted.get().execute(android.Manifest.permission.READ_CONTACTS) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        PartialState.SetContactPermissionStatus(it.value)
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun observeLoadForContactSyncWithPhonebook(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(contactsRepository.get().getContactsAndSyncWithPhonebook()) }
            .map { PartialState.NoChange }
    }

    private fun observeImportContact(): Observable<PartialState> {
        return intent<Intent.ImportContact>()
            .switchMap {
                wrap(contactsRepository.get().getContactsAndSyncWithPhonebook().firstOrError())
                    .flatMap { isPermissionGranted.get().execute(android.Manifest.permission.READ_CONTACTS) }
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.SetImportContactVisibility(true)
                    is Result.Success -> PartialState.SetImportContactVisibility(false, it.value)
                    is Result.Failure -> PartialState.SetImportContactVisibility(false)
                }
            }
    }

    private fun observeResetData(): Observable<PartialState> {
        return intent<Intent.ResetData>()
            .map {
                resetData.onNext(Unit)
                PartialState.NoChange
            }
    }

    private fun observeSearchDataChanges(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { resetData }
            .switchMap { onSearchPublishSubject }
            .switchMap {
                UseCase.wrapObservable(
                    getHomeSearchData.get().execute(
                        GetHomeSearchData.Request(
                            ArrayList(Sort.sortfilter), Sort.sortBy, Sort.sortBy, it
                        )
                    )
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        PartialState.SetData(
                            it.value.customers,
                            it.value.suppliers,
                            it.value.contacts
                        )
                    }
                    is Result.Failure -> {
                        PartialState.HideLoading
                    }
                }
            }
    }

    private fun observeSearch(): Observable<PartialState> {
        return intent<Intent.SearchQuery>()
            .map {
                onSearchPublishSubject.onNext(it.searchQuery)
                PartialState.UpdateSearchQuery(it.searchQuery)
            }
    }

    private fun observeUnSyncedCustomers(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { UseCase.wrapObservable(getUnSyncedCustomers.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> PartialState.SetUnSyncCustomers(it.value)
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun observeSupplierCreditEnabledCustomerIds(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { UseCase.wrapObservable(getSupplierCreditEnabledCustomerIds.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        PartialState.SetSupplierCreditEnabledCustomerIds(it.value)
                    }
                    is Result.Failure -> {
                        PartialState.NoChange
                    }
                }
            }
    }

    private fun observeUnSyncedSuppliers(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { UseCase.wrapObservable(getUnSyncedSuppliers.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> PartialState.SetUnSyncSuppliers(it.value)
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun setViewModelParams(): Observable<PartialState> {
        return intent<Intent.Load>()
            .map {
                PartialState.SetViewModelParams(
                    SOURCE.valueOf(source.get()),
                    isAccountSelection = isAccountSelection.get()
                )
            }
    }

    private fun getKycRiskCategory() = intent<Intent.Load>()
        .switchMap { UseCase.wrapObservable(getKycRiskCategory.get().execute()) }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    PartialState.SetKycRiskCategory(it.value.kycRiskCategory)
                }
                is Result.Failure -> {
                    PartialState.NoChange
                }
            }
        }

    private fun setSearchVisibility() = intent<Intent.ShowSearchInput>()
        .map {
            if (it.canShow) {
                emitViewEvent(ViewEvent.ShowKeyboard)
            }
            PartialState.SetSearchInput(it.canShow.not())
        }

    private fun observeShowCustomerQr() = intent<Intent.ShowCustomerQr>()
        .switchMap {
            wrap {
                val enabled = abRepository.get().isFeatureEnabled(Features.CUSTOMER_QR_PAYMENT).awaitFirst()
                return@wrap it to enabled
            }
        }
        .map {
            if (it is Result.Success) {
                if (it.value.second) {
                    emitViewEvent(ViewEvent.CustomerAddPayment(it.value.first.customerId, it.value.first.source))
                } else {
                    emitViewEvent(ViewEvent.CustomerQrDialog(it.value.first.customerId, it.value.first.source))
                }
            }
            PartialState.NoChange
        }

    private fun sendCustomerReminder() = intent<Intent.SendCustomerReminder>()
        .switchMap {
            wrap(
                getPaymentReminderIntent.get().execute(
                    customerId = it.customerId,
                    screen = "home_search",
                    reminderMode = GetPaymentReminderIntent.Companion.ReminderMode.WHATSAPP.value
                )
            )
        }
        .map {
            if (it is Result.Success) {
                emitViewEvent(ViewEvent.SendReminder(it.value))
            }
            PartialState.NoChange
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        // this is done so that we can build HomeCustomerItem list from latest state
        val tempState = when (partialState) {
            is PartialState.HideLoading -> currentState.copy(isLoading = false)
            is PartialState.UpdateSearchQuery -> currentState.copy(searchQuery = partialState.searchQuery)
            is PartialState.SetUnSyncCustomers -> currentState.copy(unSyncCustomerIds = partialState.customers)
            is PartialState.SetUnSyncSuppliers -> currentState.copy(unSyncSupplierIds = partialState.suppliers)
            is PartialState.SetViewModelParams -> currentState.copy(
                source = partialState.source,
                isAccountSelection = partialState.isAccountSelection,
                hideSearchInput = partialState.isAccountSelection
            )
            is PartialState.SetContactPermissionStatus -> currentState.copy(
                isContactsPermissionGranted = partialState.status
            )
            is PartialState.SetImportContactVisibility -> currentState.copy(
                isContactsLoading = partialState.status,
                isContactsPermissionGranted = partialState.isPermissionGranted
            )
            is PartialState.SetSupplierCreditEnabledCustomerIds -> currentState.copy(
                supplierCreditEnabledCustomerIds = partialState.customerIds
            )
            is PartialState.SetData -> currentState.copy(
                customers = partialState.customers, suppliers = partialState.suppliers,
                contacts = partialState.contacts, isLoading = false
            )
            is PartialState.NoChange -> currentState
            is PartialState.SetKycRiskCategory -> currentState.copy(
                kycRiskCategory = partialState.kycRiskCategory
            )
            is PartialState.SetBillCountMap -> currentState.copy(
                billCountMap = partialState.billCountMap
            )
            is PartialState.SetSearchInput -> currentState.copy(hideSearchInput = partialState.hide)
        }

        return tempState.copy(
            itemList = buildSearchItemList(tempState)
        )
    }

    private fun buildSearchItemList(state: State): List<HomeSearchItem> {
        val list = mutableListOf<HomeSearchItem>()
        addFilterItem(list)
        addNoUserFoundItem(state, list)
        if (state.source == SOURCE.HOME_CUSTOMER_TAB) {
            addCustomers(state, list)
            addSuppliers(state, list)
        } else {
            addSuppliers(state, list)
            addCustomers(state, list)
        }

        addContacts(state, list)
        addLoadingView(state, list)
        addImportContacts(state, list)
        return list
    }

    private fun addImportContacts(state: State, list: MutableList<HomeSearchItem>) {
        if (state.contacts.isNotEmpty()) return
        if (state.isContactsPermissionGranted) return

        list.add(HomeSearchItem.HeaderItem(R.string.contacts))
        list.add(HomeSearchItem.ImportCustomerContactItem(state.source))
    }

    private fun addLoadingView(state: State, list: MutableList<HomeSearchItem>) {
        if (state.contacts.isNotEmpty()) return
        if (!state.isContactsLoading) return

        list.add(HomeSearchItem.HeaderItem(R.string.contacts))
        list.add(HomeSearchItem.ShimmerListLoadingItem)
    }

    private fun addContacts(state: State, list: MutableList<HomeSearchItem>) {
        if (state.contacts.isEmpty()) return

        if (state.searchQuery.isNotEmpty()) {
            list.add(HomeSearchItem.HeaderItem(R.string.contacts))
        }
        state.contacts.forEach { contact ->
            list.add(HomeSearchItem.ContactsItem(contact))
        }
    }

    private fun addSuppliers(state: State, list: MutableList<HomeSearchItem>) {
        if (state.suppliers.isEmpty()) return

        if (state.isAccountSelection && state.source == SOURCE.HOME_CUSTOMER_TAB) return

        if (state.searchQuery.isNotEmpty()) {
            list.add(HomeSearchItem.HeaderItem(R.string.suppliers))
        }

        state.suppliers.forEach { supplier ->
            if (state.isAccountSelection) {
                // for account selection add only supplier with balance negative
                if (supplier.balance < 0) {
                    list.add(
                        HomeSearchItem.SupplierItem(
                            id = supplier.id,
                            supplier = supplier,
                            syncType = if (supplier.lastActivityTime != null) {
                                if (state.unSyncSupplierIds.contains(supplier.id)) {
                                    HomeSupplierView.SYNC_PENDING
                                } else {
                                    HomeSupplierView.SYNC_COMPLETED
                                }
                            } else {
                                HomeSupplierView.SYNC_NO_TXN
                            },
                        )
                    )
                }
            } else {
                list.add(
                    HomeSearchItem.SupplierItem(
                        id = supplier.id,
                        supplier = supplier,
                        syncType = if (supplier.lastActivityTime != null) {
                            if (state.unSyncSupplierIds.contains(supplier.id)) {
                                HomeSupplierView.SYNC_PENDING
                            } else {
                                HomeSupplierView.SYNC_COMPLETED
                            }
                        } else {
                            HomeSupplierView.SYNC_NO_TXN
                        },
                    )
                )
            }
        }
    }

    private fun addCustomers(state: State, list: MutableList<HomeSearchItem>) {
        if (state.customers.isEmpty()) return

        if (state.isAccountSelection && state.source == SOURCE.HOME_SUPPLIER_TAB) return

        if (state.searchQuery.isNotEmpty()) {
            list.add(HomeSearchItem.HeaderItem(R.string.customers))
        }

        state.customers.forEach { customerWithQrIntent ->
            val commonLedger = state.supplierCreditEnabledCustomerIds.contains(customerWithQrIntent.customer.id)
            val addTxnPermissionDenied = if (commonLedger) {
                customerWithQrIntent.customer.isAddTransactionPermissionDenied()
            } else {
                false
            }
            list.add(
                HomeSearchItem.CustomerItem(
                    customerId = customerWithQrIntent.customer.id,
                    name = customerWithQrIntent.customer.description,
                    profileImage = customerWithQrIntent.customer.profileImage,
                    balance = customerWithQrIntent.customer.balanceV2,
                    commonLedger = commonLedger,
                    addTxnPermissionDenied = addTxnPermissionDenied,
                    showFullQRCard = state.customers.size == 1 && // show qr card only if one customer is left in all the lists
                        state.suppliers.isEmpty() &&
                        customerWithQrIntent.qrIntent.isNotNullOrBlank(),
                    qrIntent = customerWithQrIntent.qrIntent,
                    showQROption = customerWithQrIntent.qrIntent.isNotNullOrBlank() && !state.isAccountSelection,
                    showReminderOption = customerWithQrIntent.qrIntent.isNullOrEmpty() && customerWithQrIntent.customer.balanceV2 < 0 && !state.isAccountSelection
                )
            )
        }
    }

    private fun addFilterItem(list: MutableList<HomeSearchItem>) {
        if (Sort.sortApplied && Sort.sortfilter.isNotEmpty()) {
            list.add(HomeSearchItem.FilterItem)
        }
    }

    private fun addNoUserFoundItem(state: State, list: MutableList<HomeSearchItem>) {
        if (state.customers.isEmpty() && state.suppliers.isEmpty() && state.contacts.isEmpty() &&
            state.isLoading.not() && state.searchQuery.isNotNullOrBlank()
        ) {
            list.add(HomeSearchItem.NoUserFoundItem(state.addRelationLoading, state.searchQuery))
        }
    }

    data class AddSupplierRequest(
        val name: String,
        val mobile: String?,
        val contactUrl: String?,
        val isContact: Boolean,
    )

    data class AddCustomerRequest(
        val name: String,
        val mobile: String?,
        val contactUrl: String?,
        val isContact: Boolean,
    )
}
