package `in`.okcredit.sales_ui.ui.bill_summary

import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.usecase.DeleteSale
import `in`.okcredit.sales_ui.usecase.GetCashSaleItem
import `in`.okcredit.sales_ui.usecase.UpdateSale
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.auth.AccessTokenProvider
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class BillSummaryViewModel @Inject constructor(
    val initialState: BillSummaryContract.State,
    @ViewModelParam("sale_id") val saleId: String,
    @ViewModelParam("editable") val isEditable: Boolean,
    private val getCashSaleItem: Lazy<GetCashSaleItem>,
    private val updateSale: Lazy<UpdateSale>,
    private val deleteSale: Lazy<DeleteSale>,
    private val context: Lazy<Context>,
    private val tokenProvider: Lazy<AccessTokenProvider>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>
) : BaseViewModel<BillSummaryContract.State, BillSummaryContract.PartialState, BillSummaryContract.ViewEvent>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<BillSummaryContract.State>> {
        return Observable.mergeArray(
            intent<BillSummaryContract.Intent.Load>()
                .map {
                    BillSummaryContract.PartialState.SetSaleId(saleId)
                },
            intent<BillSummaryContract.Intent.Load>()
                .map {
                    BillSummaryContract.PartialState.SetEditable(isEditable)
                },
            intent<BillSummaryContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(getActiveBusiness.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> BillSummaryContract.PartialState.NoChange
                        is Result.Success -> {
                            BillSummaryContract.PartialState.SetBusinessName(it.value.name)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(BillSummaryContract.ViewEvent.GoToLoginScreen)
                                    BillSummaryContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    emitViewEvent(
                                        BillSummaryContract.ViewEvent.ShowError(
                                            context.get().getString(R.string.no_internet_msg)
                                        )
                                    )
                                    BillSummaryContract.PartialState.NoChange
                                }
                                else -> {
                                    emitViewEvent(
                                        BillSummaryContract.ViewEvent.ShowError(
                                            context.get().getString(R.string.err_default)
                                        )
                                    )
                                    BillSummaryContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },
            intent<BillSummaryContract.Intent.Load>()
                .switchMap { getCashSaleItem.get().execute(GetCashSaleItem.Request(saleId)) }
                .map {
                    when (it) {
                        is Result.Progress -> BillSummaryContract.PartialState.NoChange
                        is Result.Success -> {
                            BillSummaryContract.PartialState.SetSale(it.value.sale)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(BillSummaryContract.ViewEvent.GoToLoginScreen)
                                    BillSummaryContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    emitViewEvent(
                                        BillSummaryContract.ViewEvent.ShowError(
                                            context.get().getString(R.string.no_internet_msg)
                                        )
                                    )
                                    BillSummaryContract.PartialState.NoChange
                                }
                                else -> {
                                    emitViewEvent(
                                        BillSummaryContract.ViewEvent.ShowError(
                                            context.get().getString(R.string.err_default)
                                        )
                                    )
                                    BillSummaryContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },
            intent<BillSummaryContract.Intent.Load>()
                .switchMap { UseCase.wrapSingle(Single.just(tokenProvider.get().getAccessToken())) }
                .map {
                    when (it) {
                        is Result.Progress -> BillSummaryContract.PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(BillSummaryContract.ViewEvent.LoadWebView(saleId, it.value ?: ""))
                            BillSummaryContract.PartialState.SetAuthToken(it.value ?: "")
                        }
                        is Result.Failure -> {
                            emitViewEvent(
                                BillSummaryContract.ViewEvent.ShowError(
                                    context.get().getString(R.string.err_default)
                                )
                            )
                            BillSummaryContract.PartialState.NoChange
                        }
                    }
                },
            intent<BillSummaryContract.Intent.UpdateBillingDataIntent>()
                .switchMap { updateSale.get().execute(UpdateSale.Request(saleId, it.updateSaleItemRequest)) }
                .map {
                    when (it) {
                        is Result.Progress -> BillSummaryContract.PartialState.SetLoading(true)
                        is Result.Success -> {
                            emitViewEvent(BillSummaryContract.ViewEvent.UpdateSale(it.value.sale.toJsonString()))
                            BillSummaryContract.PartialState.SetLoading(false)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> {
                                    emitViewEvent(
                                        BillSummaryContract.ViewEvent.ShowError(
                                            context.get().getString(R.string.no_internet_msg)
                                        )
                                    )
                                    BillSummaryContract.PartialState.SetLoading(false)
                                }
                                else -> {
                                    emitViewEvent(
                                        BillSummaryContract.ViewEvent.ShowError(
                                            context.get().getString(R.string.err_default)
                                        )
                                    )
                                    BillSummaryContract.PartialState.SetLoading(false)
                                }
                            }
                        }
                    }
                },
            intent<BillSummaryContract.Intent.DeleteSaleIntent>()
                .switchMap { deleteSale.get().execute(DeleteSale.Request(saleId)) }
                .map {
                    when (it) {
                        is Result.Progress -> BillSummaryContract.PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(BillSummaryContract.ViewEvent.OnDeleted)
                            BillSummaryContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> {
                                    emitViewEvent(
                                        BillSummaryContract.ViewEvent.ShowError(
                                            context.get().getString(R.string.no_internet_msg)
                                        )
                                    )
                                    BillSummaryContract.PartialState.NoChange
                                }
                                else -> {
                                    emitViewEvent(
                                        BillSummaryContract.ViewEvent.ShowError(
                                            context.get().getString(R.string.err_default)
                                        )
                                    )
                                    BillSummaryContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },
            intent<BillSummaryContract.Intent.ShowDeleteDialogIntent>()
                .map {
                    emitViewEvent(BillSummaryContract.ViewEvent.ShowDeleteDialog(saleId))
                    BillSummaryContract.PartialState.NoChange
                },
            intent<BillSummaryContract.Intent.ShowLoaderIntent>()
                .map {
                    BillSummaryContract.PartialState.SetLoading(it.canShow)
                },
            intent<BillSummaryContract.Intent.GoToMerchantProfile>()
                .map {
                    emitViewEvent(BillSummaryContract.ViewEvent.GoToMerchantProfileScreen)
                    BillSummaryContract.PartialState.NoChange
                },
            intent<BillSummaryContract.Intent.ShowErrorIntent>()
                .map {
                    emitViewEvent(BillSummaryContract.ViewEvent.ShowError(it.msg))
                    BillSummaryContract.PartialState.NoChange
                },
            intent<BillSummaryContract.Intent.ShareIntent>()
                .map {
                    val items = it.sale?.billedItems?.items
                    if (items.isNullOrEmpty().not() && items!!.size > 5) {
                        emitViewEvent(BillSummaryContract.ViewEvent.ShareAsPDF)
                    } else {
                        emitViewEvent(BillSummaryContract.ViewEvent.ShareAsImage)
                    }
                    BillSummaryContract.PartialState.NoChange
                }
        )
    }

    override fun reduce(
        currentState: BillSummaryContract.State,
        partialState: BillSummaryContract.PartialState
    ): BillSummaryContract.State {
        return when (partialState) {
            BillSummaryContract.PartialState.NoChange -> currentState
            is BillSummaryContract.PartialState.SetAuthToken -> currentState.copy(authToken = partialState.authToken)
            is BillSummaryContract.PartialState.SetSale -> currentState.copy(sale = partialState.sale)
            is BillSummaryContract.PartialState.SetBusinessName -> currentState.copy(businessName = partialState.name)
            is BillSummaryContract.PartialState.SetEditable -> currentState.copy(isEditable = partialState.isEditable)
            is BillSummaryContract.PartialState.SetLoading -> currentState.copy(isLoading = partialState.canShow)
            is BillSummaryContract.PartialState.SetSaleId -> currentState.copy(
                saleId = partialState.saleId,
                isLoading = true
            )
        }
    }
}
