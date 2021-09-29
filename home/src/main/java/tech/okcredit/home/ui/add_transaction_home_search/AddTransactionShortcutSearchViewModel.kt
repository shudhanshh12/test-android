package tech.okcredit.home.ui.add_transaction_home_search

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.error.CustomerErrors
import `in`.okcredit.backend._offline.usecase.AddCustomer
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend._offline.usecase.GetUnSyncedCustomers
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.home.GetSupplierCreditEnabledCustomerIds
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.AbFeatures
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.BehaviorSubject
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.auth.AuthService
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.home.ui.homesearch.HomeConstants
import tech.okcredit.home.usecase.GetHomeSearchData
import tech.okcredit.home.usecase.GetSuggestedCustomersForAddTransactionShortcut
import tech.okcredit.home.usecase.GetUnSyncSupplier
import tech.okcredit.home.usecase.IsPermissionGranted
import javax.inject.Inject

class AddTransactionShortcutSearchViewModel @Inject constructor(
    initialState: Lazy<AddTransactionShortcutSearchContract.State>,
    private val getHomeSearchData: Lazy<GetHomeSearchData>,
    private val getSuggestedCustomersForAddTransactionShortcut: Lazy<GetSuggestedCustomersForAddTransactionShortcut>,
    private val getPaymentReminderIntent: Lazy<GetPaymentReminderIntent>,
    private val getSupplierCreditEnabledCustomerIds: Lazy<GetSupplierCreditEnabledCustomerIds>,
    private val getUnSyncedCustomers: Lazy<GetUnSyncedCustomers>,
    private val isPermissionGranted: Lazy<IsPermissionGranted>,
    private val addCustomer: Lazy<AddCustomer>,
    private val tracker: Lazy<Tracker>,
    private val getUnSyncedSuppliers: Lazy<GetUnSyncSupplier>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val contactsRepository: Lazy<ContactsRepository>,
    private val authService: Lazy<AuthService>,
    private val ab: Lazy<AbRepository>,
) : BaseViewModel<AddTransactionShortcutSearchContract.State, AddTransactionShortcutSearchContract.PartialState, AddTransactionShortcutSearchContract.ViewEvent>(
    initialState.get()
) {

    private val onSearchPublishSubject: BehaviorSubject<String> = BehaviorSubject.create()
    private val addCustomerPublishSubject: BehaviorSubject<AddCustomerRequest?> = BehaviorSubject.create()
    private val resetData: BehaviorSubject<Unit> = BehaviorSubject.createDefault(Unit)

    override fun handle(): Observable<UiState.Partial<AddTransactionShortcutSearchContract.State>> {
        return mergeArray(

            authenticate(),

            observeSearchDataChanges(),

            observeSuggestedCustomers(),

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

            intent<AddTransactionShortcutSearchContract.Intent.SendWhatsAppReminder>()
                .switchMap { UseCase.wrapSingle(getPaymentReminderIntent.get().execute(it.customerId, "home", null)) }
                .map {
                    when (it) {
                        is Result.Progress -> AddTransactionShortcutSearchContract.PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.ShareReminder(it.value))
                            AddTransactionShortcutSearchContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.GotoLogin)
                                    AddTransactionShortcutSearchContract.PartialState.NoChange
                                }
                                else -> {
                                    AddTransactionShortcutSearchContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },

            intent<AddTransactionShortcutSearchContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(getActiveBusiness.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> AddTransactionShortcutSearchContract.PartialState.NoChange
                        is Result.Success -> {
                            AddTransactionShortcutSearchContract.PartialState.SetBusiness(it.value)
                        }
                        is Result.Failure -> {
                            AddTransactionShortcutSearchContract.PartialState.NoChange
                        }
                    }
                },

            intent<AddTransactionShortcutSearchContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(collectionRepository.get().isCollectionActivated()) }
                .map {
                    when (it) {
                        is Result.Progress -> AddTransactionShortcutSearchContract.PartialState.NoChange
                        is Result.Success -> {
                            AddTransactionShortcutSearchContract.PartialState.SetCollectionActivatedStatus(it.value)
                        }
                        is Result.Failure -> {
                            AddTransactionShortcutSearchContract.PartialState.NoChange
                        }
                    }
                },

            intent<AddTransactionShortcutSearchContract.Intent.OnBackPressed>()
                .map {
                    emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.OnBackPressed)
                    AddTransactionShortcutSearchContract.PartialState.NoChange
                },

            intent<AddTransactionShortcutSearchContract.Intent.GoToHomeScreen>()
                .map {
                    emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.GotoHomeScreen)
                    AddTransactionShortcutSearchContract.PartialState.NoChange
                },

            isProfilePicClickable(),
        )
    }

    private fun isProfilePicClickable(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.Load>().switchMap {
            ab.get().isFeatureEnabled(AbFeatures.SINGLE_LIST)
        }
            .map {
                AddTransactionShortcutSearchContract.PartialState.IsProfilePicClickable(it.not())
            }
    }

    private fun observeAddCustomerResult(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return addCustomerPublishSubject
            .switchMap { UseCase.wrapSingle(addCustomer.get().execute(it.name, it.mobile, it.contactUrl)) }
            .map {
                when (it) {
                    is Result.Progress -> AddTransactionShortcutSearchContract.PartialState.NoChange
                    is Result.Success -> {
                        tracker.get().trackAddRelationshipSuccessFlows(
                            relation = PropertyValue.CUSTOMER,
                            accountId = it.value.id,
                            search = PropertyValue.TRUE,
                            contact = addCustomerPublishSubject.value?.isContact.toString()
                        )
                        emitViewEvent(
                            AddTransactionShortcutSearchContract.ViewEvent.GotoCustomerScreenAndCloseSearch(
                                it.value.id,
                                it.value.mobile
                            )
                        )
                        AddTransactionShortcutSearchContract.PartialState.NoChange
                    }
                    is Result.Failure -> {
                        when {
                            it.error is CustomerErrors.InvalidMobile -> {
                                emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.ShowInvalidMobileNumber)
                            }

                            it.error is CustomerErrors.MobileConflict -> {
                                emitViewEvent(
                                    AddTransactionShortcutSearchContract.ViewEvent.ShowMobileConflictForCustomer((it.error as CustomerErrors.MobileConflict).conflict)
                                )
                            }

                            it.error is CustomerErrors.ActiveCyclicAccount -> {
                                emitViewEvent(
                                    AddTransactionShortcutSearchContract.ViewEvent.ShowCyclicAccountForSupplier((it.error as CustomerErrors.ActiveCyclicAccount).conflict)
                                )
                            }

                            it.error is CustomerErrors.DeletedCyclicAccount -> {
                                emitViewEvent(
                                    AddTransactionShortcutSearchContract.ViewEvent
                                        .ShowCyclicAccountForDeletedSupplier((it.error as CustomerErrors.DeletedCyclicAccount).conflict)
                                )
                            }

                            it.error is CustomerErrors.InvalidName -> {
                                emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.ShowInvalidName)
                            }

                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.GotoLogin)
                            }
                            isInternetIssue(it.error) -> {
                                emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.ShowInternetError)
                            }
                            else -> {
                                emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.ShowError)
                            }
                        }
                        AddTransactionShortcutSearchContract.PartialState.NoChange
                    }
                }
            }
    }

    private fun observeAddRelationFromContact(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.AddRelationFromContact>()
            .map {
                addCustomerPublishSubject.onNext(
                    AddCustomerRequest(it.contact.name, it.contact.mobile, it.contact.picUri, true)
                )
                AddTransactionShortcutSearchContract.PartialState.NoChange
            }
    }

    private fun observeAddRelationFromSearch(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.AddRelationFromSearch>()
            .map {
                addCustomerPublishSubject.onNext(
                    AddCustomerRequest(it.query, null, null, false)
                )
                AddTransactionShortcutSearchContract.PartialState.NoChange
            }
    }

    private fun observeContactPermission(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.Load>()
            .switchMap { isPermissionGranted.get().execute(android.Manifest.permission.READ_CONTACTS) }
            .map {
                when (it) {
                    is Result.Progress -> AddTransactionShortcutSearchContract.PartialState.NoChange
                    is Result.Success -> {
                        AddTransactionShortcutSearchContract.PartialState.SetContactPermissionStatus(it.value)
                    }
                    is Result.Failure -> AddTransactionShortcutSearchContract.PartialState.NoChange
                }
            }
    }

    private fun observeLoadForContactSyncWithPhonebook(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.Load>()
            .switchMap { wrap(contactsRepository.get().getContactsAndSyncWithPhonebook()) }
            .map { AddTransactionShortcutSearchContract.PartialState.NoChange }
    }

    private fun observeImportContact(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.ImportContact>()
            .switchMap { wrap(contactsRepository.get().getContactsAndSyncWithPhonebook()) }
            .map {
                when (it) {
                    is Result.Progress -> AddTransactionShortcutSearchContract.PartialState.SetImportContactVisibility(
                        true
                    )
                    is Result.Success -> AddTransactionShortcutSearchContract.PartialState.SetImportContactVisibility(
                        false
                    )
                    is Result.Failure -> AddTransactionShortcutSearchContract.PartialState.SetImportContactVisibility(
                        false
                    )
                }
            }
    }

    private fun authenticate(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.Load>()
            .map {
                if (authService.get().isAuthenticated()) {
                    AddTransactionShortcutSearchContract.PartialState.NoChange
                } else {
                    emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.GotoLogin)
                    AddTransactionShortcutSearchContract.PartialState.NoChange
                }
            }
    }

    private fun observeSearchDataChanges(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.Load>()
            .switchMap { resetData }
            .switchMap { onSearchPublishSubject }
            .switchMap {
                UseCase.wrapObservable(
                    getHomeSearchData.get().execute(
                        GetHomeSearchData.Request(
                            arrayListOf(), HomeConstants.SORT_BY_LATEST, HomeConstants.SORT_BY_LATEST, it
                        )
                    )
                )
            }
            .map { result ->
                when (result) {
                    is Result.Progress -> AddTransactionShortcutSearchContract.PartialState.NoChange
                    is Result.Success -> {
                        AddTransactionShortcutSearchContract.PartialState.SetData(
                            result.value.customers.map { it.customer },
                            result.value.suppliers,
                            result.value.contacts
                        )
                    }
                    is Result.Failure -> {
                        AddTransactionShortcutSearchContract.PartialState.HideLoading
                    }
                }
            }
    }

    private fun observeSuggestedCustomers(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.Load>()
            .switchMap { UseCase.wrapSingle(getSuggestedCustomersForAddTransactionShortcut.get().getSuggestions()) }
            .map {
                when (it) {
                    is Result.Progress -> AddTransactionShortcutSearchContract.PartialState.NoChange
                    is Result.Success -> AddTransactionShortcutSearchContract.PartialState.SetSuggestedCustomers(it.value)
                    is Result.Failure -> AddTransactionShortcutSearchContract.PartialState.HideLoading
                }
            }
    }

    private fun observeSearch(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.SearchQuery>()
            .map {
                onSearchPublishSubject.onNext(it.searchQuery)
                AddTransactionShortcutSearchContract.PartialState.UpdateSearchQuery(it.searchQuery)
            }
    }

    private fun observeUnSyncedCustomers(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.Load>()
            .switchMap { UseCase.wrapObservable(getUnSyncedCustomers.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> AddTransactionShortcutSearchContract.PartialState.NoChange
                    is Result.Success -> AddTransactionShortcutSearchContract.PartialState.SetUnSyncCustomers(it.value)
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.GotoLogin)
                                AddTransactionShortcutSearchContract.PartialState.NoChange
                            }
                            else -> {
                                AddTransactionShortcutSearchContract.PartialState.NoChange
                            }
                        }
                    }
                }
            }
    }

    private fun observeSupplierCreditEnabledCustomerIds(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.Load>()
            .switchMap { UseCase.wrapObservable(getSupplierCreditEnabledCustomerIds.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> AddTransactionShortcutSearchContract.PartialState.NoChange
                    is Result.Success -> {
                        AddTransactionShortcutSearchContract.PartialState.SetSupplierCreditEnabledCustomerIds(it.value)
                    }
                    is Result.Failure -> {
                        AddTransactionShortcutSearchContract.PartialState.NoChange
                    }
                }
            }
    }

    private fun observeUnSyncedSuppliers(): Observable<AddTransactionShortcutSearchContract.PartialState> {
        return intent<AddTransactionShortcutSearchContract.Intent.Load>()
            .switchMap { UseCase.wrapObservable(getUnSyncedSuppliers.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> AddTransactionShortcutSearchContract.PartialState.NoChange
                    is Result.Success -> AddTransactionShortcutSearchContract.PartialState.SetUnSyncSuppliers(it.value)
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(AddTransactionShortcutSearchContract.ViewEvent.GotoLogin)
                                AddTransactionShortcutSearchContract.PartialState.NoChange
                            }
                            else -> {
                                AddTransactionShortcutSearchContract.PartialState.NoChange
                            }
                        }
                    }
                }
            }
    }

    override fun reduce(
        currentState: AddTransactionShortcutSearchContract.State,
        partialState: AddTransactionShortcutSearchContract.PartialState
    ): AddTransactionShortcutSearchContract.State {
        return when (partialState) {
            is AddTransactionShortcutSearchContract.PartialState.HideLoading -> currentState.copy(isLoading = false)
            is AddTransactionShortcutSearchContract.PartialState.UpdateSearchQuery -> currentState.copy(
                searchQuery = partialState.searchQuery, showSuggestedCustomers = partialState.searchQuery.isBlank()
            )
            is AddTransactionShortcutSearchContract.PartialState.SetUnSyncCustomers -> currentState.copy(
                unSyncCustomerIds = partialState.customers
            )
            is AddTransactionShortcutSearchContract.PartialState.SetUnSyncSuppliers -> currentState.copy(
                unSyncSupplierIds = partialState.suppliers
            )
            is AddTransactionShortcutSearchContract.PartialState.SetContactPermissionStatus -> currentState.copy(
                isContactsPermissionGranted = partialState.status
            )
            is AddTransactionShortcutSearchContract.PartialState.SetImportContactVisibility -> currentState.copy(
                isContactsLoading = partialState.status
            )
            is AddTransactionShortcutSearchContract.PartialState.SetSupplierCreditEnabledCustomerIds -> currentState.copy(
                supplierCreditEnabledCustomerIds = partialState.customerIds
            )
            is AddTransactionShortcutSearchContract.PartialState.SetData -> currentState.copy(
                customers = partialState.customers, suppliers = partialState.suppliers,
                contacts = partialState.contacts, isLoading = false
            )
            is AddTransactionShortcutSearchContract.PartialState.SetSuggestedCustomers -> currentState.copy(
                suggestedCustomers = partialState.suggestedCustomers, isSuggestedCustomersLoading = false
            )
            is AddTransactionShortcutSearchContract.PartialState.SetBusiness -> currentState.copy(
                business = partialState.business
            )
            is AddTransactionShortcutSearchContract.PartialState.SetCollectionActivatedStatus -> currentState.copy(
                isCollectionActivated = partialState.status
            )
            is AddTransactionShortcutSearchContract.PartialState.NoChange -> currentState
            is AddTransactionShortcutSearchContract.PartialState.IsProfilePicClickable -> currentState.copy(
                isProfilePicClickable = partialState.isProfilePicClickable
            )
        }
    }

    data class AddSupplierRequest(
        val name: String,
        val mobile: String?,
        val contactUrl: String?,
        val isContact: Boolean
    )

    data class AddCustomerRequest(
        val name: String,
        val mobile: String?,
        val contactUrl: String?,
        val isContact: Boolean
    )
}
