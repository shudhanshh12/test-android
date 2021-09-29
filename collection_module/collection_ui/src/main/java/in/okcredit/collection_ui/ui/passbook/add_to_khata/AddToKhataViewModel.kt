package `in`.okcredit.collection_ui.ui.passbook.add_to_khata

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.usecase.GetCollectionOnlinePayment
import `in`.okcredit.collection_ui.usecase.TagOnlinePaymentWithCustomer
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class AddToKhataViewModel @Inject constructor(
    initialState: AddToKhataContract.State,
    @ViewModelParam("customer_id") val customerId: String,
    @ViewModelParam("payment_id") val paymentId: String,
    private val getCustomer: Lazy<GetCustomer>,
    private val getCollectionOnlinePayment: Lazy<GetCollectionOnlinePayment>,
    private val tagOnlinePaymentWithCustomer: Lazy<TagOnlinePaymentWithCustomer>,
    private val context: Context,
) : BaseViewModel<AddToKhataContract.State, AddToKhataContract.PartialState, AddToKhataContract.ViewEvent>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<AddToKhataContract.State>> {
        return Observable.mergeArray(
            getCustomer(),
            getCollection(),
            tagCustomer()
        )
    }

    private fun getCustomer() = intent<AddToKhataContract.Intent.Load>()
        .switchMap { UseCase.wrapObservable(getCustomer.get().execute(customerId)) }
        .map {
            when (it) {
                is Result.Progress -> AddToKhataContract.PartialState.NoChange
                is Result.Success -> {
                    AddToKhataContract.PartialState.SetCustomer(it.value)
                }
                is Result.Failure -> when {
                    isAuthenticationIssue(it.error) -> AddToKhataContract.PartialState.NoChange
                    else -> AddToKhataContract.PartialState.NoChange
                }
            }
        }

    private fun getCollection() = intent<AddToKhataContract.Intent.Load>()
        .switchMap { getCollectionOnlinePayment.get().execute(paymentId) }
        .map {
            when (it) {
                is Result.Progress -> AddToKhataContract.PartialState.NoChange
                is Result.Success -> {
                    AddToKhataContract.PartialState.SetCollectionOnlinePayment(it.value)
                }
                is Result.Failure -> when {
                    isAuthenticationIssue(it.error) -> AddToKhataContract.PartialState.NoChange
                    else -> AddToKhataContract.PartialState.NoChange
                }
            }
        }

    private fun tagCustomer() = intent<AddToKhataContract.Intent.TagCustomer>()
        .switchMap {
            tagOnlinePaymentWithCustomer.get().execute(TagOnlinePaymentWithCustomer.Request(customerId, paymentId))
        }
        .map {
            when (it) {
                is Result.Progress -> AddToKhataContract.PartialState.SetLoader(true)
                is Result.Success -> {
                    emitViewEvent(AddToKhataContract.ViewEvent.OnSuccess)
                    AddToKhataContract.PartialState.SetLoader(false)
                }
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            emitViewEvent(AddToKhataContract.ViewEvent.OnError(context.getString(R.string.no_internet_connection)))
                            AddToKhataContract.PartialState.SetLoader(false)
                        }

                        notAbleToTagCustomer(it.error) -> {
                            emitViewEvent(AddToKhataContract.ViewEvent.OnError(context.getString(R.string.not_able_to_tag_customer)))
                            AddToKhataContract.PartialState.SetLoader(false)
                        }

                        else -> {
                            emitViewEvent(AddToKhataContract.ViewEvent.OnError(context.getString(R.string.err_default)))
                            AddToKhataContract.PartialState.SetLoader(false)
                        }
                    }
                }
            }
        }

    private fun notAbleToTagCustomer(error: Throwable): Boolean {
        error.localizedMessage?.let {
            return it.contains("507")
        }
        return false
    }

    override fun reduce(
        currentState: AddToKhataContract.State,
        partialState: AddToKhataContract.PartialState
    ): AddToKhataContract.State {
        return when (partialState) {
            AddToKhataContract.PartialState.NoChange -> currentState
            is AddToKhataContract.PartialState.SetCustomer -> currentState.copy(customer = partialState.customer)
            is AddToKhataContract.PartialState.SetCollectionOnlinePayment -> currentState.copy(collectionOnlinePayment = partialState.collectionOnlinePayment)
            is AddToKhataContract.PartialState.SetLoader -> currentState.copy(isLoading = partialState.isLoading)
        }
    }
}
