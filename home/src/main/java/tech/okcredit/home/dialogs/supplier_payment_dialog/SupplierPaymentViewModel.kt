package tech.okcredit.home.dialogs.supplier_payment_dialog

import `in`.okcredit.collection.contract.GetSupplierCollectionProfileWithSync
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.supplier.usecase.GetSupplier
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.home.dialogs.supplier_payment_dialog.SupplierPaymentContract.*
import timber.log.Timber
import javax.inject.Inject

class SupplierPaymentViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam(SupplierPaymentContract.ARG_SUPPLIER_ID) val supplierId: String,
    private val getSupplier: Lazy<GetSupplier>,
    private val getSupplierCollectionProfileWithSync: Lazy<GetSupplierCollectionProfileWithSync>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    private var supplier: Supplier? = null

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .switchMap {
                    UseCase.wrapSingle(getSupplierCollectionProfileWithSync.get().execute(supplierId, true).firstOrError())
                }.map {
                    when (it) {
                        is Result.Progress -> PartialState.ShowLoading
                        is Result.Success -> {
                            Timber.e("<<<CollectionV5 supplier destination viewModel: ${it.value.paymentAddress}")
                            PartialState.SetCollectionCustomerProfile(
                                it.value
                            )
                        }
                        is Result.Failure -> {
                            Timber.e("<<<CollectionV5 supplier destination viewModel error: some error")
                            PartialState.NoChange
                        }
                    }
                },

            // handle `load` screen intent
            intent<Intent.Load>()
                .switchMap {
                    getSupplier.get().execute(supplierId)
                }
                .map {
                    when (it) {
                        is Result.Progress -> {
                            PartialState.NoChange
                        }
                        is Result.Success -> {
                            Timber.e("<<<CollectionV5 supplier destination viewModel: ${it.value}")
                            supplier = it.value
                            PartialState.SetSupplier(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> {
                                    Timber.e("<<<CollectionV5 supplier destination viewModel error: network error")
                                    PartialState.SetNetworkError(
                                        true
                                    )
                                }
                                else -> {
                                    Timber.e("<<<CollectionV5 supplier destination viewModel error: ErrorState")
                                    // SupplierDestinationContract.PartialState.ErrorState
                                    PartialState.NoChange
                                }
                            }
                        }
                    }
                }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is PartialState.SetNetworkError -> currentState.copy(isNetworkError = true)
            is PartialState.SetSupplier -> currentState.copy(supplier = partialState.supplier)
            is PartialState.SetCollectionCustomerProfile -> currentState.copy(
                collectionCustomerProfile = partialState.collectionCustomerProfile,
                isLoading = false,
                isNetworkError = false
            )
            is PartialState.NoChange -> currentState
        }
    }
}
