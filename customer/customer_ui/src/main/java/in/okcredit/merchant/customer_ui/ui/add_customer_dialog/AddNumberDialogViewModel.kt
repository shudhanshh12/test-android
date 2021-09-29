package `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog

import `in`.okcredit.backend._offline.error.CustomerErrors
import `in`.okcredit.backend._offline.usecase.UpdateCustomer
import `in`.okcredit.backend._offline.usecase.UpdateSupplier
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogContract.*
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.supplier.contract.SyncSupplierEnabledCustomerIds
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddNumberDialogViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam("customer_id") val customerId: String,
    @ViewModelParam("description") val description: String,
    @ViewModelParam("screen") val screen: String?,
    @ViewModelParam("is_skip_and_send") val isSkipAndSend: Boolean,
    @ViewModelParam("is_supplier") val isSupplier: Boolean,
    @ViewModelParam("mobile") val mobile: String?,
    private val updateCustomer: Lazy<UpdateCustomer>,
    private val updateSupplier: Lazy<UpdateSupplier>,
    private val syncSupplierEnabledCustomerIds: Lazy<SyncSupplierEnabledCustomerIds>
) : BaseViewModel<State, PartialState, ViewEvents>(initialState) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.EnteredMobileNumber>()
                .map {
                    PartialState.EnteredMobileNumber(it.enteredMobileNumber)
                },

            intent<Intent.SubmitMobileNumber>()
                .switchMap {
                    if (isSupplier) {
                        UseCase.wrapCompletable(
                            updateSupplier.get().updateMoblie(customerId, it.moblieNumber)
                        )
                    } else {
                        UseCase.wrapCompletable(
                            updateCustomer.get().executeUpdateMobile(customerId, it.moblieNumber)
                                .andThen(syncSupplierEnabledCustomerIds.get().execute())
                        )
                    }
                }
                .switchMap {
                    when (it) {
                        is Result.Progress -> Observable.just(PartialState.ShowLoader(true))
                        is Result.Success -> {
                            emitViewEvent(ViewEvents.OnAccountAddedSuccessfully)
                            Observable.just(PartialState.NoChange)
                        }
                        is Result.Failure -> {
                            when {
                                it.error is CustomerErrors.MobileConflict -> {
                                    Observable.just(
                                        PartialState.ShowError(
                                            true, AddNumberErrorType.MobileConflict,
                                            errorCustomer = (it.error as CustomerErrors.MobileConflict).conflict
                                        )
                                    )
                                }
                                it.error is CustomerErrors.ActiveCyclicAccount -> {
                                    onErrorObservable(
                                        AddNumberErrorType.ActiveCyclicAccount,
                                        (it.error as CustomerErrors.ActiveCyclicAccount).conflict
                                    )
                                }
                                it.error is CustomerErrors.DeletedCyclicAccount -> {
                                    onErrorObservable(
                                        AddNumberErrorType.DeletedCyclicAccount,
                                        (it.error as CustomerErrors.DeletedCyclicAccount).conflict
                                    )
                                }
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(ViewEvents.GoToLogin)
                                    Observable.just(PartialState.ShowLoader(false))
                                }
                                isInternetIssue(it.error) -> {
                                    Observable.just(
                                        PartialState.ShowError(true, AddNumberErrorType.InternetIssue)
                                    )
                                }
                                else -> {
                                    Observable.just(
                                        PartialState.ShowError(true, AddNumberErrorType.SomeErrorOccurred)
                                    )
                                }
                            }
                        }
                    }
                },

            intent<Intent.SetEditTextFocus>()
                .map {
                    PartialState.SetEditTextFocus(it.hasFocus)
                },

            intent<Intent.Load>()
                .take(1)
                .map {
                    PartialState.SetBundleArguments(
                        customerId,
                        description,
                        mobile,
                        isSkipAndSend,
                        screen ?: CustomerEventTracker.RELATIONSHIP_REMINDER
                    )
                }
        )
    }

    private fun onErrorObservable(
        errorType: AddNumberErrorType,
        errorSupplier: Supplier? = null,
        errorCustomer: Customer? = null
    ): Observable<PartialState>? {
        return Observable.timer(2, TimeUnit.SECONDS)
            .map<PartialState> { PartialState.ShowPopupError(false, errorType, errorSupplier, errorCustomer) }
            .startWith(PartialState.ShowPopupError(true, errorType, errorSupplier))
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.EnteredMobileNumber -> currentState.copy(
                enteredMobileNumber = partialState.enteredMobileNumber,
                showLoader = false,
                error = false
            )
            is PartialState.ShowPopupError -> currentState.copy(
                errorPopup = partialState.errorPopup,
                errorType = partialState.errorType,
                errorSupplier = partialState.errorSupplier,
                errorCustomer = partialState.errorCustomer,
                showLoader = false
            )
            is PartialState.ShowError -> currentState.copy(
                error = partialState.error,
                errorType = partialState.errorType,
                errorCustomer = partialState.errorCustomer,
                showLoader = false
            )
            is PartialState.SetEditTextFocus -> currentState.copy(hasFocus = partialState.hasFocus)
            is PartialState.ShowLoader -> currentState.copy(
                showLoader = partialState.showLoader,
                error = false
            )
            is PartialState.SetBundleArguments -> currentState.copy(
                customerId = partialState.customerId,
                description = partialState.description,
                mobile = partialState.mobile,
                isSkipAndSend = partialState.isSkipAndSend,
                screen = partialState.screen,
            )
        }
    }
}
