package `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.error.CustomerErrors
import `in`.okcredit.backend._offline.usecase.AddCustomer
import `in`.okcredit.backend._offline.usecase.AddSupplier
import `in`.okcredit.home.SetRelationshipAddedAfterOnboarding
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.addrelationship.analytics.AddRelationshipEventTracker
import `in`.okcredit.merchant.customer_ui.addrelationship.enum.AddRelationshipFailedError.*
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContactsContract.Companion.ADD_CUSTOMER
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually.AddRelationshipManuallyContract.*
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.android.base.utils.MobileUtils
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class AddRelationshipManuallyViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam(AddRelationshipManually.ARG_ADD_RELATIONSHIP_TYPE) private val relationshipType: Lazy<Int>,
    @ViewModelParam(AddRelationshipManually.ARG_SOURCE) private val source: Lazy<String>,
    @ViewModelParam(AddRelationshipManually.ARG_DEFAULT_MODE) private val defaultMode: String,
    @ViewModelParam(AddRelationshipManually.ARG_OPEN_FOR_RESULT) private val openForResult: Boolean,
    private val addSupplier: Lazy<AddSupplier>,
    private val addCustomer: Lazy<AddCustomer>,
    private val relationshipAdded: Lazy<SetRelationshipAddedAfterOnboarding>,
    private val tracker: Lazy<Tracker>,
    private val customerEventTracker: Lazy<CustomerEventTracker>,
    private val addRelationshipTracker: Lazy<AddRelationshipEventTracker>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            observeRelationshipAddedIntent(),
            observeAddRelationshipIntent(),
            observeAddSupplierIntent(),
            observeAddCustomerIntent(),
            observeTrackSelectNameIntent(),
            observeTrackSelectMobileIntent(),
            observeNameChangedIntent(),
            observeMobileChangedIntent(),
            observeSetProfileIntent(),
            observeConfirmButtonClickedIntent(),
        )
    }

    private fun observeSetProfileIntent() = intent<Intent.SetProfileImage>()
        .map {
            PartialState.SetProfileImage(it.profileImage)
        }

    private fun observeMobileChangedIntent() = intent<Intent.MobileChanged>()
        .map {
            PartialState.MobileNumberChanged(it.mobile)
        }

    private fun observeNameChangedIntent() = intent<Intent.NameChanged>()
        .map {
            PartialState.NameChanged(it.relationshipName)
        }

    private fun observeTrackSelectNameIntent() = intent<Intent.TrackAddCustomerSelectName>()
        .take(1)
        .map {
            if (relationshipType.get() == ADD_CUSTOMER) {
                tracker.get().trackAddCustomerSelectName(
                    flow = "Add Relation",
                    relation = "Customer",
                    source = source.get(),
                    defaultMode = defaultMode

                )
            } else {
                tracker.get().trackAddCustomerSelectName(
                    flow = "Add Relation",
                    relation = "Supplier",
                    source = source.get(),
                    defaultMode = defaultMode
                )
            }
            PartialState.NoChange
        }

    private fun observeTrackSelectMobileIntent() = intent<Intent.TrackAddCustomerSelectMobile>()
        .take(1)
        .map {
            if (relationshipType.get() == ADD_CUSTOMER) {
                tracker.get().trackAddCustomerSelectMobile(
                    flow = "Add Relation",
                    relation = "Customer",
                    source = source.get(),
                    defaultMode = defaultMode
                )
            } else {
                tracker.get().trackAddCustomerSelectMobile(
                    flow = "Add Relation",
                    relation = "Supplier",
                    source = source.get(),
                    defaultMode = defaultMode
                )
            }
            PartialState.NoChange
        }

    private fun observeRelationshipAddedIntent() =
        intent<Intent.RelationshipAddedAfterOnboarding>()
            .take(1)
            .switchMap { wrap(rxSingle { relationshipAdded.get().execute() }) }
            .map {
                PartialState.NoChange
            }

    private fun observeConfirmButtonClickedIntent() = intent<Intent.ConfirmButtonClicked>()
        .map {
            if (getCurrentState().enableConfirmCTA) {
                addRelationshipTracker.get().trackAddRelationshipConfirm(
                    flow = "Add Relation",
                    relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer" else "Supplier",
                    source = getCurrentState().source,
                    defaultMode = defaultMode
                )
                pushIntent(Intent.AddRelationship)
            } else {
                if (getCurrentState().name.isEmpty()) {
                    emitViewEvent(ViewEvent.ShowError(R.string.invalid_name))
                } else {
                    emitViewEvent(ViewEvent.ShowError(R.string.invalid_mobile))
                }
            }
            PartialState.NoChange
        }

    private fun observeAddRelationshipIntent() = intent<Intent.AddRelationship>()
        .map {
            if (relationshipType.get() == ADD_CUSTOMER) {
                pushIntent(
                    Intent.AddCustomer(
                        getCurrentState().name,
                        getCurrentState().mobile,
                        getCurrentState().profile
                    )
                )
            } else {
                pushIntent(
                    Intent.AddSupplier(
                        getCurrentState().name,
                        getCurrentState().mobile,
                        getCurrentState().profile
                    )
                )
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
                        searchContact = PropertyValue.FALSE,
                        flow = "Add Relation",
                        defaultMode = defaultMode,
                        source = source.get()
                    )
                    pushIntent(Intent.RelationshipAddedAfterOnboarding)
                    if (openForResult) {
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
                                defaultMode = defaultMode
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
                                defaultMode = defaultMode
                            )
                            emitViewEvent(ViewEvent.ShowError(R.string.invalid_mobile))
                            PartialState.ShowProgress(false)
                        }

                        it.error is SupplierCreditServerErrors.ActiveCyclicAccount -> {
                            (it.error as SupplierCreditServerErrors.ActiveCyclicAccount).getInfo()?.let { errorData ->
                                emitViewEvent(
                                    ViewEvent.AddRelationshipFailed(
                                        errorData.id,
                                        errorData.name,
                                        errorData.mobile,
                                        errorData.profile,
                                        errorType = ACTIVE_CUSTOMER_CYCLIC_ACCOUNT,
                                        exception = it.error
                                    )
                                )
                            }
                            PartialState.ShowProgress(false)
                        }
                        it.error is SupplierCreditServerErrors.DeletedCyclicAccount -> {
                            (it.error as SupplierCreditServerErrors.DeletedCyclicAccount).getInfo()?.let { errorData ->
                                emitViewEvent(
                                    ViewEvent.AddRelationshipFailed(
                                        errorData.id,
                                        errorData.name,
                                        errorData.mobile,
                                        errorData.profile,
                                        errorType = DELETED_CUSTOMER_CYCLIC_ACCOUNT,
                                        exception = it.error
                                    )
                                )
                            }
                            PartialState.ShowProgress(false)
                        }
                        isInternetIssue(it.error) -> {
                            customerEventTracker.get().trackNoInternetError(
                                CustomerEventTracker.ADD_CUSTOMER_SCREEN,
                                CustomerEventTracker.SEARCH,
                                "Add Supplier",
                                flow = "Add Relation",
                                relation = "Supplier",
                                source = source.get(),
                                defaultMode = defaultMode
                            )
                            emitViewEvent(ViewEvent.ShowError(R.string.no_internet_connection))
                            PartialState.ShowProgress(false)
                        }
                        else -> {
                            addRelationshipTracker.get().trackAddTransactionFailed(
                                reason = "Unknown",
                                type = "Unknown",
                                exception = it.error.toString(),
                                flow = "Add Relation",
                                relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer"
                                else "Supplier",
                                source = getCurrentState().source,
                                defaultMode = defaultMode
                            )
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
                        searchContact = PropertyValue.FALSE,
                        flow = "Add Relation",
                        defaultMode = defaultMode,
                        source = source.get()
                    )
                    pushIntent(Intent.RelationshipAddedAfterOnboarding)
                    if (openForResult) {
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
                            (it.error as CustomerErrors.DeletedCyclicAccount).conflict?.let { supplier ->
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
                            }
                            PartialState.ShowProgress(false)
                        }
                        it.error is CustomerErrors.InvalidName -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.invalid_name))
                            PartialState.ShowProgress(false)
                        }
                        it.error is CustomerErrors.InvalidMobile -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.invalid_mobile))
                            PartialState.ShowProgress(false)
                        }
                        isInternetIssue(it.error) -> {
                            customerEventTracker.get().trackNoInternetError(
                                CustomerEventTracker.ADD_CUSTOMER_SCREEN,
                                CustomerEventTracker.SEARCH,
                                CustomerEventTracker.ADD_CUSTOMER,
                                flow = "Add Relation",
                                relation = "Customer",
                                source = source.get(),
                                defaultMode = defaultMode
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
            is PartialState.ShowProgress -> currentState.copy(
                isLoading = partialState.show
            )
            is PartialState.NameChanged -> currentState.copy(
                name = partialState.relationshipName
            )
            is PartialState.MobileNumberChanged -> currentState.copy(
                mobile = partialState.mobile
            )
            is PartialState.SetProfileImage -> currentState.copy(
                profile = partialState.profileImage
            )
            is PartialState.NoChange -> currentState
        }

        return tempState.copy(
            enableConfirmCTA = getCtaPartialState(tempState.name, tempState.mobile)
        )
    }

    private fun getCtaPartialState(name: String, mobile: String): Boolean {
        return name.isNotBlank() && (mobile.isBlank() || MobileUtils.isPhoneNumberValid(mobile))
    }
}
