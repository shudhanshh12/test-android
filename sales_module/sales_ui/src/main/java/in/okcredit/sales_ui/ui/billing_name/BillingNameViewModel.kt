package `in`.okcredit.sales_ui.ui.billing_name

import `in`.okcredit.sales_ui.usecase.GetContact
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import io.reactivex.Observable
import javax.inject.Inject

class BillingNameViewModel @Inject constructor(
    val initialState: BillingNameContract.State,
    private val navigator: BillingNameContract.Navigator,
    private val getContact: GetContact
) : BasePresenter<BillingNameContract.State, BillingNameContract.PartialState>(initialState) {
    private var isLoaded = false
    override fun handle(): Observable<out UiState.Partial<BillingNameContract.State>> {
        return Observable.mergeArray(
            intent<BillingNameContract.Intent.Load>()
                .filter { isLoaded.not() }
                .switchMap {
                    getContact.execute("")
                }
                .map {
                    when (it) {
                        is Result.Progress -> BillingNameContract.PartialState.NoChange
                        is Result.Success -> {
                            BillingNameContract.PartialState.SetContactsData(
                                it.value.contacts,
                                it.value.isPermissionAllowed
                            )
                        }
                        is Result.Failure -> {
                            BillingNameContract.PartialState.NoChange
                        }
                    }
                },
            intent<BillingNameContract.Intent.GetContactsIntent>()
                .switchMap {
                    getContact.execute(it.name)
                }
                .map {
                    when (it) {
                        is Result.Progress -> BillingNameContract.PartialState.NoChange
                        is Result.Success -> {
                            BillingNameContract.PartialState.SetContactsDataWithName(
                                it.value.contacts,
                                it.value.isPermissionAllowed,
                                it.value.searchQuery ?: ""
                            )
                        }
                        is Result.Failure -> {
                            BillingNameContract.PartialState.NoChange
                        }
                    }
                },
            intent<BillingNameContract.Intent.ShowMobileFieldIntent>()
                .map {
                    navigator.showMobileField()
                    BillingNameContract.PartialState.ShowMobileField
                },
            intent<BillingNameContract.Intent.SubmitIntent>()
                .map {
                    navigator.onSubmit(it.name, it.mobile)
                    BillingNameContract.PartialState.NoChange
                },
            intent<BillingNameContract.Intent.GetContactPermissionIntent>()
                .map {
                    navigator.getContactPermission()
                    BillingNameContract.PartialState.NoChange
                },
            intent<BillingNameContract.Intent.SetNameIntent>()
                .switchMap {
                    getContact.execute(it.name)
                }
                .map {
                    when (it) {
                        is Result.Progress -> BillingNameContract.PartialState.NoChange
                        is Result.Success -> {
                            BillingNameContract.PartialState.SetContactsDataWithName(
                                it.value.contacts,
                                it.value.isPermissionAllowed,
                                name = it.value.searchQuery ?: ""
                            )
                        }
                        is Result.Failure -> {
                            BillingNameContract.PartialState.NoChange
                        }
                    }
                },
            intent<BillingNameContract.Intent.SetMobileIntent>()
                .switchMap {
                    getContact.execute(it.mobile)
                }
                .map {
                    when (it) {
                        is Result.Progress -> BillingNameContract.PartialState.NoChange
                        is Result.Success -> {
                            BillingNameContract.PartialState.SetContactsDataWithMobile(
                                it.value.contacts,
                                it.value.isPermissionAllowed,
                                mobile = it.value.searchQuery ?: ""
                            )
                        }
                        is Result.Failure -> {
                            BillingNameContract.PartialState.NoChange
                        }
                    }
                },
            intent<BillingNameContract.Intent.SetDataIntent>()
                .map {
                    navigator.onSubmit(it.name, it.mobile)
                    BillingNameContract.PartialState.SetData(it.name, it.mobile)
                }
        )
    }

    override fun reduce(
        currentState: BillingNameContract.State,
        partialState: BillingNameContract.PartialState
    ): BillingNameContract.State {
        return when (partialState) {
            is BillingNameContract.PartialState.SetContactsData -> currentState.copy(
                contacts = partialState.contacts,
                isPermissionGranted = partialState.isPermissionGranted
            )
            is BillingNameContract.PartialState.SetContactsDataWithName -> currentState.copy(
                contacts = partialState.contacts,
                isPermissionGranted = partialState.isPermissionGranted,
                name = partialState.name
            )
            is BillingNameContract.PartialState.SetContactsDataWithMobile -> currentState.copy(
                contacts = partialState.contacts,
                isPermissionGranted = partialState.isPermissionGranted,
                mobile = partialState.mobile
            )
            BillingNameContract.PartialState.NoChange -> currentState
            BillingNameContract.PartialState.ShowMobileField -> currentState.copy(canShowMobileField = true)
            is BillingNameContract.PartialState.SetData -> currentState.copy(
                name = partialState.name,
                mobile = partialState.mobile
            )
            is BillingNameContract.PartialState.SetName -> currentState.copy(name = partialState.name)
            is BillingNameContract.PartialState.SetMobile -> currentState.copy(mobile = partialState.mobile)
        }
    }
}
