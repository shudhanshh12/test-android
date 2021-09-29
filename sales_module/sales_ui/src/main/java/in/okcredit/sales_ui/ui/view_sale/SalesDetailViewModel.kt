package `in`.okcredit.sales_ui.ui.view_sale

import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.usecase.DeleteSale
import `in`.okcredit.sales_ui.usecase.GetCashSaleItem
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class SalesDetailViewModel @Inject constructor(
    val initialState: SalesDetailContract.State,
    @ViewModelParam("sale_id") val saleId: String,
    private val navigator: SalesDetailContract.Navigator,
    private val deleteSale: DeleteSale,
    private val getSale: GetCashSaleItem,
    private val context: Context
) : BasePresenter<SalesDetailContract.State, SalesDetailContract.PartialState>(initialState) {

    private val showAlertPublishSubject: PublishSubject<String> = PublishSubject.create()

    override fun handle(): Observable<out UiState.Partial<SalesDetailContract.State>> {
        return Observable.mergeArray(
            intent<SalesDetailContract.Intent.Load>()
                .switchMap { getSale.execute(GetCashSaleItem.Request(saleId)) }
                .map {
                    when (it) {
                        is Result.Progress -> SalesDetailContract.PartialState.NoChange
                        is Result.Success -> {
                            SalesDetailContract.PartialState.ShowSaleDetail(it.value.sale)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    SalesDetailContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    SalesDetailContract.PartialState.SetNetworkError(true)
                                }
                                else -> {
                                    showAlertPublishSubject.onNext(context.getString(R.string.err_default))
                                    SalesDetailContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },
            intent<SalesDetailContract.Intent.ShowDeleteDialog>()
                .map {
                    navigator.showDeleteDialog(saleId)
                    SalesDetailContract.PartialState.NoChange
                },
            intent<SalesDetailContract.Intent.DeleteSale>()
                .switchMap { deleteSale.execute(DeleteSale.Request(it.saleId)) }
                .map {
                    when (it) {
                        is Result.Progress -> SalesDetailContract.PartialState.NoChange
                        is Result.Success -> {
                            navigator.onDeleted()
                            SalesDetailContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    SalesDetailContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    SalesDetailContract.PartialState.SetNetworkError(true)
                                }
                                else -> {
                                    showAlertPublishSubject.onNext(context.getString(R.string.err_default))
                                    SalesDetailContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                }
        )
    }

    override fun reduce(
        currentState: SalesDetailContract.State,
        partialState: SalesDetailContract.PartialState
    ): SalesDetailContract.State {
        return when (partialState) {
            SalesDetailContract.PartialState.NoChange -> currentState
            is SalesDetailContract.PartialState.SetNetworkError -> currentState.copy(
                isLoading = false,
                networkError = partialState.networkError
            )
            is SalesDetailContract.PartialState.ShowSaleDetail -> currentState.copy(
                isLoading = false,
                sale = partialState.sale
            )
            SalesDetailContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is SalesDetailContract.PartialState.ShowAlert -> currentState.copy(
                alert = partialState.msg,
                canShowAlert = true
            )
            SalesDetailContract.PartialState.HideAlert -> currentState.copy(canShowAlert = false)
        }
    }
}
