package `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.error.CustomerErrors
import `in`.okcredit.backend._offline.usecase.AddCustomer
import `in`.okcredit.backend._offline.usecase.AddSupplier
import `in`.okcredit.home.SetRelationshipAddedAfterOnboarding
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.addrelationship.analytics.AddRelationshipEventTracker
import `in`.okcredit.merchant.customer_ui.addrelationship.enum.AddRelationshipFailedError.*
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContacts.Companion.ARG_ADD_RELATIONSHIP_TYPE
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContacts.Companion.ARG_CAN_SHOW_ADD_MANUALLY_OPTION
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContactsContract.*
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContactsContract.Companion.ADD_CUSTOMER
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.usecase.GetContacts
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class AddRelationshipFromContactsViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam(ARG_ADD_RELATIONSHIP_TYPE) private val relationshipType: Lazy<Int>,
    @ViewModelParam(ARG_CAN_SHOW_ADD_MANUALLY_OPTION) private val canShowAddManuallyOption: Lazy<Boolean>,
    @ViewModelParam(AddRelationshipFromContacts.ARG_SOURCE) val source: Lazy<String>,
    @ViewModelParam(AddRelationshipFromContacts.ARG_DEFAULT_MODE) val defaultMode: Lazy<String>,
    @ViewModelParam(AddRelationshipFromContacts.ARG_OPEN_FOR_RESULT) val openForResult: Lazy<Boolean>,
    private val getContacts: Lazy<GetContacts>,
    private val addSupplier: Lazy<AddSupplier>,
    private val addCustomer: Lazy<AddCustomer>,
    private val relationshipAdded: Lazy<SetRelationshipAddedAfterOnboarding>,
    private val tracker: Lazy<Tracker>,
    private val addRelationshipTracker: Lazy<AddRelationshipEventTracker>,
    private val customerEventTracker: Lazy<CustomerEventTracker>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            loadContacts(),
            observeRelationshipAddedIntent(),
            observeAddCustomerIntent(),
            observeAddRelationshipIntent(),
            observeAddSupplierIntent(),
            observeShowSearchInputIntent(),
            observeSearchQueryIntent(),
            trackSearchUser(),
            observeRedirectToLedgerScreen(),
            observeReturnResultIntent(),
        )
    }

    private fun observeReturnResultIntent() = intent<Intent.ReturnResult>()
        .map { intent ->
            getCurrentState()
                .listContactModel
                .firstOrNull { it.mobile == intent.mobile }
                ?.also { contactModel ->
                    emitViewEvent(ViewEvent.ReturnResult(contactModel))
                }
            PartialState.NoChange
        }

    private fun observeRedirectToLedgerScreen() = intent<Intent.RedirectToLedgerScreen>()
        .map {
            if (relationshipType.get() == ADD_CUSTOMER) {
                emitViewEvent(ViewEvent.GoToCustomerFragment(it.relationshipId))
            } else {
                emitViewEvent(ViewEvent.GoToSupplierFragment(it.relationshipId))
            }
            PartialState.NoChange
        }

    private fun observeSearchQueryIntent() = intent<Intent.SearchQuery>()
        .switchMap { getContacts.get().execute(it.query) }
        .map {
            if (it.second.isNotNullOrBlank()) {
                pushIntent(Intent.TrackSearchUsed)
            }
            PartialState.SetContacts(
                it.first,
                it.second
            )
        }

    private fun trackSearchUser() = intent<Intent.TrackSearchUsed>()
        .take(1)
        .map {
            addRelationshipTracker.get().trackAddRelationshipContactSearchUsed(
                flow = "Add Relation",
                relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer" else "Supplier",
                source = getCurrentState().source,
                defaultMode = defaultMode.get()
            )
            PartialState.NoChange
        }

    private fun observeShowSearchInputIntent() = intent<Intent.ShowSearchInput>()
        .map {
            PartialState.ShowSearchInput(it.canShow)
        }

    private fun loadContacts() = intent<Intent.Load>()
        .switchMap { getContacts.get().execute(null) }
        .map {
            PartialState.SetContacts(it.first)
        }

    private fun observeRelationshipAddedIntent() = intent<Intent.RelationshipAddedAfterOnboarding>()
        .take(1)
        .switchMap { wrap(rxSingle { relationshipAdded.get().execute() }) }
        .map {
            PartialState.NoChange
        }

    private fun observeAddRelationshipIntent() = intent<Intent.AddRelationship>()
        .map { intent ->
            val contactModel = getCurrentState()
                .listContactModel
                .firstOrNull { it.mobile == intent.mobile }

            if (contactModel == null) {
                emitViewEvent(
                    ViewEvent.ShowError(
                        R.string.t_001_simplify_add_relation_error_in_add_relation_something_went_wong
                    )
                )
            } else {
                addRelationshipTracker.get().trackAddRelationshipConfirm(
                    flow = "Add Relation",
                    relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer" else "Supplier",
                    source = getCurrentState().source,
                    defaultMode = defaultMode.get()
                )
                if (relationshipType.get() == ADD_CUSTOMER) {
                    pushIntent(
                        Intent.AddCustomer(
                            contactModel.name,
                            contactModel.mobile,
                            contactModel.profileImage
                        )
                    )
                } else {
                    pushIntent(
                        Intent.AddSupplier(
                            contactModel.name,
                            contactModel.mobile,
                            contactModel.profileImage
                        )
                    )
                }
            }
            PartialState.NoChange
        }

    private fun observeAddSupplierIntent() = intent<Intent.AddSupplier>()
        .switchMap {
            wrap(
                addSupplier.get().execute(
                    AddSupplier.Request(
                        it.name,
                        it.mobile,
                        it.profileImage
                    )
                )
            )
        }
        .map {
            when (it) {
                is Result.Progress -> PartialState.ShowProgress(true)
                is Result.Success -> {
                    tracker.get().trackAddRelationshipSuccessFlows(
                        relation = PropertyValue.SUPPLIER,
                        search = PropertyValue.FALSE,
                        contact = PropertyValue.TRUE,
                        searchContact = if (getCurrentState().searchQuery.isNotNullOrBlank()) PropertyValue.TRUE
                        else PropertyValue.FALSE,
                        flow = "Add Relation",
                        defaultMode = defaultMode.get(),
                        source = source.get()
                    )
                    pushIntent(Intent.RelationshipAddedAfterOnboarding)
                    if (openForResult.get()) {
                        emitViewEvent(ViewEvent.SetResultRelationshipAddedSuccessfully)
                    }
                    emitViewEvent(ViewEvent.GoToSupplierFragment(it.value.id))
                    PartialState.ShowProgress(false)
                }
                is Result.Failure -> {
                    when {
                        it.error is SupplierCreditServerErrors.MobileConflict -> {
                            val supplier = (it.error as SupplierCreditServerErrors.MobileConflict).getSupplier()
                            emitViewEvent(
                                ViewEvent.AddRelationshipFailed(
                                    id = supplier.id,
                                    name = supplier.name,
                                    profile = supplier.profileImage,
                                    mobile = supplier.mobile,
                                    errorType = MOBILE_CONFLICT_ACCOUNT_WITH_SUPPLIER,
                                    exception = it.error
                                )
                            )
                            PartialState.ShowProgress(false)
                        }
                        it.error is SupplierCreditServerErrors.InvalidName -> {
                            addRelationshipTracker.get().trackAddTransactionFailed(
                                reason = "Invalid Input",
                                type = "invalid_input: name",
                                exception = it.error.toString(),
                                flow = "Add Relation",
                                relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer"
                                else "Supplier",
                                source = getCurrentState().source,
                                defaultMode = defaultMode.get()
                            )
                            emitViewEvent(ViewEvent.ShowError(R.string.invalid_name))
                            PartialState.ShowProgress(false)
                        }

                        it.error is SupplierCreditServerErrors.InvalidMobile -> {
                            addRelationshipTracker.get().trackAddTransactionFailed(
                                reason = "Invalid Input",
                                type = "invalid_input: mobile",
                                exception = it.error.toString(),
                                flow = "Add Relation",
                                relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer"
                                else "Supplier",
                                source = getCurrentState().source,
                                defaultMode = defaultMode.get()
                            )
                            emitViewEvent(ViewEvent.ShowError(R.string.invalid_mobile))
                            PartialState.ShowProgress(false)
                        }

                        it.error is SupplierCreditServerErrors.ActiveCyclicAccount -> {
                            val errorData = (it.error as SupplierCreditServerErrors.ActiveCyclicAccount).getInfo()
                            emitViewEvent(
                                ViewEvent.AddRelationshipFailed(
                                    errorData?.id,
                                    errorData?.name,
                                    errorData?.mobile,
                                    errorData?.profile,
                                    errorType = ACTIVE_CUSTOMER_CYCLIC_ACCOUNT,
                                    exception = it.error
                                )
                            )
                            PartialState.ShowProgress(false)
                        }
                        it.error is SupplierCreditServerErrors.DeletedCyclicAccount -> {
                            val errorData = (it.error as SupplierCreditServerErrors.DeletedCyclicAccount).getInfo()
                            emitViewEvent(
                                ViewEvent.AddRelationshipFailed(
                                    errorData?.id,
                                    errorData?.name,
                                    errorData?.mobile,
                                    errorData?.profile,
                                    errorType = DELETED_CUSTOMER_CYCLIC_ACCOUNT,
                                    exception = it.error
                                )
                            )
                            PartialState.ShowProgress(false)
                        }
                        isInternetIssue(it.error) -> {
                            customerEventTracker.get().trackNoInternetError(
                                "Add Relationship",
                                CustomerEventTracker.SEARCH,
                                "Add Supplier",
                                flow = "Add Relation",
                                relation = "Supplier",
                                source = source.get(),
                                defaultMode = defaultMode.get()
                            )
                            emitViewEvent(ViewEvent.ShowError(R.string.no_internet_connection))
                            PartialState.ShowProgress(false)
                        }
                        else -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.t_001_simplify_add_relation_error_something_went_wong))
                            PartialState.ShowProgress(false)
                        }
                    }
                }
            }
        }

    private fun observeAddCustomerIntent() = intent<Intent.AddCustomer>()
        .switchMap {
            wrap(
                addCustomer.get().execute(
                    it.name,
                    it.mobile,
                    it.profileImage
                )
            )
        }
        .map {
            when (it) {
                is Result.Progress -> PartialState.ShowProgress(true)
                is Result.Success -> {
                    tracker.get().trackAddRelationshipSuccessFlows(
                        relation = PropertyValue.CUSTOMER,
                        search = PropertyValue.FALSE,
                        contact = PropertyValue.TRUE,
                        searchContact = if (getCurrentState().searchQuery.isNotNullOrBlank()) PropertyValue.TRUE
                        else PropertyValue.FALSE,
                        flow = "Add Relation",
                        defaultMode = defaultMode.get(),
                        source = source.get()
                    )
                    pushIntent(Intent.RelationshipAddedAfterOnboarding)
                    if (openForResult.get()) {
                        emitViewEvent(ViewEvent.SetResultRelationshipAddedSuccessfully)
                    }
                    emitViewEvent(ViewEvent.GoToCustomerFragment(it.value.id))
                    PartialState.ShowProgress(false)
                }
                is Result.Failure -> {
                    when {
                        it.error is CustomerErrors.MobileConflict -> {
                            val customer = (it.error as CustomerErrors.MobileConflict).conflict
                            emitViewEvent(
                                ViewEvent.AddRelationshipFailed(
                                    id = customer.id,
                                    name = customer.description,
                                    profile = customer.profileImage,
                                    mobile = customer.mobile,
                                    errorType = MOBILE_CONFLICT_ACCOUNT_WITH_CUSTOMER,
                                    exception = it.error
                                )
                            )
                            PartialState.ShowProgress(false)
                        }
                        it.error is CustomerErrors.ActiveCyclicAccount -> {
                            val supplier = (it.error as CustomerErrors.ActiveCyclicAccount).conflict
                            emitViewEvent(
                                ViewEvent.AddRelationshipFailed(
                                    id = supplier.id,
                                    name = supplier.name,
                                    mobile = supplier.mobile,
                                    profile = supplier.profileImage,
                                    errorType = ACTIVE_SUPPLIER_CYCLIC_ACCOUNT,
                                    exception = it.error
                                )
                            )
                            PartialState.ShowProgress(false)
                        }
                        it.error is CustomerErrors.DeletedCyclicAccount -> {
                            val supplier = (it.error as CustomerErrors.DeletedCyclicAccount).conflict
                            emitViewEvent(
                                ViewEvent.AddRelationshipFailed(
                                    id = supplier.id,
                                    name = supplier.name,
                                    mobile = supplier.mobile,
                                    profile = supplier.profileImage,
                                    errorType = DELETED_SUPPLIER_CYCLIC_ACCOUNT,
                                    exception = it.error
                                )
                            )
                            PartialState.ShowProgress(false)
                        }
                        it.error is CustomerErrors.InvalidName -> {
                            addRelationshipTracker.get().trackAddTransactionFailed(
                                reason = "Invalid Input",
                                type = "invalid_input: name",
                                exception = it.error.toString(),
                                flow = "Add Relation",
                                relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer"
                                else "Supplier",
                                source = getCurrentState().source,
                                defaultMode = defaultMode.get()
                            )
                            emitViewEvent(ViewEvent.ShowError(R.string.invalid_name))
                            PartialState.ShowProgress(false)
                        }
                        it.error is CustomerErrors.InvalidMobile -> {
                            addRelationshipTracker.get().trackAddTransactionFailed(
                                reason = "Invalid Input",
                                type = "invalid_input: mobile",
                                exception = it.error.toString(),
                                flow = "Add Relation",
                                relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer"
                                else "Supplier",
                                source = getCurrentState().source,
                                defaultMode = defaultMode.get()
                            )
                            emitViewEvent(ViewEvent.ShowError(R.string.invalid_mobile))
                            PartialState.ShowProgress(false)
                        }
                        isInternetIssue(it.error) -> {
                            customerEventTracker.get().trackNoInternetError(
                                "Add Relationship",
                                CustomerEventTracker.SEARCH,
                                "Add Customer",
                                flow = "Add Relation",
                                relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer"
                                else "Supplier",
                                source = source.get(),
                                defaultMode = defaultMode.get()
                            )
                            emitViewEvent(ViewEvent.ShowError(R.string.no_internet_connection))
                            PartialState.ShowProgress(false)
                        }
                        else -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.t_001_simplify_add_relation_error_something_went_wong))
                            PartialState.ShowProgress(false)
                        }
                    }
                }
            }
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        val tempState = when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetContacts -> currentState.copy(
                listContactModel = partialState.contactList,
                searchQuery = partialState.searchQuery
            )
            is PartialState.ShowProgress -> currentState.copy(
                isLoading = partialState.show
            )
            is PartialState.ShowSearchInput -> currentState.copy(
                canShowSearchInput = partialState.canShow
            )
        }

        return tempState.copy(
            addRelationshipEpoxyModels = getAddRelationshipEpoxyModels(tempState)
        )
    }

    private fun getAddRelationshipEpoxyModels(state: State): List<AddRelationshipEpoxyModels> {
        val epoxyModels = mutableListOf<AddRelationshipEpoxyModels>()
        addManuallyOption(epoxyModels, state)
        selectFromContactsHeader(epoxyModels, state)
        addAllContactModel(epoxyModels, state)
        return epoxyModels
    }

    private fun addAllContactModel(
        epoxyModels: MutableList<AddRelationshipEpoxyModels>,
        state: State,
    ) {
        if (state.listContactModel.isNotEmpty()) {
            epoxyModels.addAll(state.listContactModel)
        }
    }

    private fun selectFromContactsHeader(epoxyModels: MutableList<AddRelationshipEpoxyModels>, state: State) {
        if (state.listContactModel.isNotEmpty()) {
            epoxyModels.add(AddRelationshipEpoxyModels.AddRelationshipHeader)
        }
    }

    private fun addManuallyOption(
        epoxyModels: MutableList<AddRelationshipEpoxyModels>,
        state: State,
    ) {
        if (canShowAddManuallyOption.get()) {
            epoxyModels.add(AddRelationshipEpoxyModels.AddManuallyModel(state.searchQuery))
        }
    }
}
