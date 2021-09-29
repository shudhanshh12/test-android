package `in`.okcredit.sales_ui.ui.list_sales

import `in`.okcredit.merchant.contract.BusinessScopedPreferenceWithActiveBusinessId
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.usecase.DeleteSale
import `in`.okcredit.sales_ui.usecase.GetCashSales
import `in`.okcredit.sales_ui.utils.Constants
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_IS_FIRST_TIME_SALE
import tech.okcredit.android.base.utils.ThreadUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SalesOnCashViewModel @Inject constructor(
    val initialState: SalesOnCashContract.State,
    private val navigator: SalesOnCashContract.Navigator,
    private val checkNetworkHealth: CheckNetworkHealth,
    private val getActiveBusinessId: GetActiveBusinessId,
    private val getCashSales: GetCashSales,
    private val deleteSale: DeleteSale,
    private val businessScopedPreferenceWithActiveBusinessId: Lazy<BusinessScopedPreferenceWithActiveBusinessId>,
    private val defaultPreferences: Lazy<DefaultPreferences>,
    private val context: Context,
    private val ab: AbRepository,
) : BasePresenter<SalesOnCashContract.State, SalesOnCashContract.PartialState>(initialState) {

    private var isLoaded = false
    private var merchantId = ""
    private val internetPinger: PublishSubject<Boolean> = PublishSubject.create()
    private val newUser: PublishSubject<Unit> = PublishSubject.create()
    private val showAlertPublishSubject: PublishSubject<String> = PublishSubject.create()

    private var deleteSaleId = ""

    override fun handle(): Observable<out UiState.Partial<SalesOnCashContract.State>> {
        return mergeArray(
            intent<SalesOnCashContract.Intent.Load>()
                .filter { !isLoaded }
                .switchMap { checkNetworkHealth.execute(Unit) }
                .switchMap { UseCase.wrapSingle(getActiveBusinessId.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> SalesOnCashContract.PartialState.ShowLoading
                        is Result.Success -> {
                            isLoaded = true
                            merchantId = it.value
                            navigator.showAll()
                            newUser.onNext(Unit)
                            SalesOnCashContract.PartialState.ChangeFilter(SalesOnCashFragment.Filter.ALL)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    SalesOnCashContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    SalesOnCashContract.PartialState.SetNetworkError(true)
                                }
                                else -> {
                                    showAlertPublishSubject.onNext(context.getString(R.string.err_default))
                                    SalesOnCashContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },
            intent<SalesOnCashContract.Intent.Load>()
                .filter { merchantId.isNotEmpty() }
                .switchMap { checkNetworkHealth.execute(Unit) }
                .map {
                    newUser.onNext(Unit)
                    SalesOnCashContract.PartialState.NoChange
                },
            intent<SalesOnCashContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(ab.isFeatureEnabled(Constants.CASH_SALES_WITH_BILL)) }
                .map {
                    when (it) {
                        is Result.Progress -> SalesOnCashContract.PartialState.NoChange
                        is Result.Success -> {
                            SalesOnCashContract.PartialState.SetBillingAb(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    SalesOnCashContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> SalesOnCashContract.PartialState.NoChange
                                else -> SalesOnCashContract.PartialState.NoChange
                            }
                        }
                    }
                },
            intent<SalesOnCashContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(ab.isExperimentEnabled(Constants.CASH_SALES_WITH_BILL_INFO_GRAPHIC_AB)) }
                .map {
                    when (it) {
                        is Result.Progress -> SalesOnCashContract.PartialState.NoChange
                        is Result.Success -> {
                            SalesOnCashContract.PartialState.SetBillingInfoGraphicAb(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    SalesOnCashContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> SalesOnCashContract.PartialState.NoChange
                                else -> SalesOnCashContract.PartialState.NoChange
                            }
                        }
                    }
                },
            intent<SalesOnCashContract.Intent.Load>()
                .switchMap {
                    UseCase.wrapSingle(
                        businessScopedPreferenceWithActiveBusinessId.get()
                            .contains(defaultPreferences.get(), PREF_BUSINESS_IS_FIRST_TIME_SALE)
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> SalesOnCashContract.PartialState.NoChange
                        is Result.Success -> {
                            val isFirstTime = !it.value
                            if (isFirstTime) {
                                businessScopedPreferenceWithActiveBusinessId.get().setString(
                                    defaultPreferences.get(), PREF_BUSINESS_IS_FIRST_TIME_SALE, false.toString()
                                )
                                    .subscribeOn(ThreadUtils.database())
                                    .blockingAwait()
                            }
                            SalesOnCashContract.PartialState.SetFirstTime(isFirstTime)
                        }
                        is Result.Failure -> {
                            SalesOnCashContract.PartialState.NoChange
                        }
                    }
                },
            intent<SalesOnCashContract.Intent.GetAllSales>()
                .switchMap {
                    getCashSales.execute(GetCashSales.Request(merchantId))
                }
                .map {
                    when (it) {
                        is Result.Progress -> SalesOnCashContract.PartialState.ShowLoading
                        is Result.Success -> {
                            SalesOnCashContract.PartialState.SetAllCashSales(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    SalesOnCashContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    internetPinger.onNext(true)
                                    SalesOnCashContract.PartialState.SetNetworkError(true)
                                }
                                else -> {
                                    showAlertPublishSubject.onNext(context.getString(R.string.err_default))
                                    SalesOnCashContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },
            intent<SalesOnCashContract.Intent.GetSales>()
                .switchMap {
                    getCashSales.execute(GetCashSales.Request(merchantId, it.startDate, it.endDate))
                }
                .map {
                    when (it) {
                        is Result.Progress -> SalesOnCashContract.PartialState.ShowLoading
                        is Result.Success -> {
                            SalesOnCashContract.PartialState.SetSales(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    SalesOnCashContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    internetPinger.onNext(true)
                                    SalesOnCashContract.PartialState.SetNetworkError(true)
                                }
                                else -> {
                                    showAlertPublishSubject.onNext(context.getString(R.string.err_default))
                                    SalesOnCashContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },
            intent<SalesOnCashContract.Intent.DeleteSale>()
                .switchMap {
                    deleteSaleId = it.id
                    deleteSale.execute(DeleteSale.Request(it.id))
                }
                .map {
                    when (it) {
                        is Result.Progress -> SalesOnCashContract.PartialState.ShowLoading
                        is Result.Success -> {
                            navigator.onDeleted(deleteSaleId)
                            SalesOnCashContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    SalesOnCashContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    SalesOnCashContract.PartialState.SetNetworkError(true)
                                }
                                else -> {
                                    showAlertPublishSubject.onNext(context.getString(R.string.err_default))
                                    SalesOnCashContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },
            intent<SalesOnCashContract.Intent.ChangeFilter>()
                .map {
                    when (it.filter) {
                        SalesOnCashFragment.Filter.ALL -> navigator.showAll()
                        SalesOnCashFragment.Filter.TODAY -> navigator.showToday()
                        SalesOnCashFragment.Filter.THIS_MONTH -> navigator.showThisMonth()
                        SalesOnCashFragment.Filter.LAST_MONTH -> navigator.showLastMonth()
                    }
                    SalesOnCashContract.PartialState.ChangeFilter(it.filter)
                },
            newUser.switchMap {
                getCashSales.execute(GetCashSales.Request(merchantId))
            }.map {
                when (it) {
                    is Result.Progress -> SalesOnCashContract.PartialState.ShowLoading
                    is Result.Success -> {
                        SalesOnCashContract.PartialState.ShowYoutubeVideo(it.value.salesList.isNullOrEmpty())
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                navigator.gotoLogin()
                                SalesOnCashContract.PartialState.NoChange
                            }
                            isInternetIssue(it.error) -> {
                                internetPinger.onNext(true)
                                SalesOnCashContract.PartialState.SetNetworkError(true)
                            }
                            else -> {
                                showAlertPublishSubject.onNext(context.getString(R.string.err_default))
                                SalesOnCashContract.PartialState.NoChange
                            }
                        }
                    }
                }
            },
            intent<SalesOnCashContract.Intent.SetDate>()
                .map {
                    SalesOnCashContract.PartialState.setDate(it.startDate, it.endDate)
                },
            internetPinger
                .switchMap { checkNetworkHealth.execute(Unit) }
                .map {
                    navigator.reLoad()
                    SalesOnCashContract.PartialState.SetNetworkError(false)
                },
            showAlertPublishSubject
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<SalesOnCashContract.PartialState> { SalesOnCashContract.PartialState.HideAlert }
                        .startWith(SalesOnCashContract.PartialState.ShowAlert(it))
                },
            intent<SalesOnCashContract.Intent.OnAddSaleIntent>()
                .map {
                    navigator.gotoAddSaleScreen()
                    SalesOnCashContract.PartialState.NoChange
                },
            intent<SalesOnCashContract.Intent.ShowSaleDetailIntent>()
                .map {
                    navigator.gotoSalesDetailScreen(it.saleid)
                    SalesOnCashContract.PartialState.NoChange
                },
            intent<SalesOnCashContract.Intent.ShowBillSummaryIntent>()
                .map {
                    navigator.gotoBillSummaryScreen(it.saleid)
                    SalesOnCashContract.PartialState.NoChange
                },
            intent<SalesOnCashContract.Intent.OnNewBillIntent>()
                .map {
                    navigator.gotoBillItemScreen()
                    SalesOnCashContract.PartialState.NoChange
                }
        )
    }

    override fun reduce(
        currentState: SalesOnCashContract.State,
        partialState: SalesOnCashContract.PartialState,
    ): SalesOnCashContract.State {
        return when (partialState) {
            SalesOnCashContract.PartialState.NoChange -> currentState
            SalesOnCashContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is SalesOnCashContract.PartialState.SetAllCashSales -> currentState.copy(
                list = partialState.sales.salesList,
                totalAmount = partialState.sales.totalAmount,
                startDate = partialState.sales.startDate,
                endDate = partialState.sales.endDate,
                isLoading = false,
                networkError = false
            )
            is SalesOnCashContract.PartialState.SetSales -> currentState.copy(
                list = partialState.sales.salesList,
                totalAmount = partialState.sales.totalAmount,
                isLoading = false,
                networkError = false
            )
            is SalesOnCashContract.PartialState.SetNetworkError -> currentState.copy(
                isLoading = false,
                networkError = true
            )
            is SalesOnCashContract.PartialState.ChangeFilter -> currentState.copy(filter = partialState.filter)
            is SalesOnCashContract.PartialState.setDate -> currentState.copy(
                startDate = partialState.startDate,
                endDate = partialState.endDate
            )
            is SalesOnCashContract.PartialState.ShowYoutubeVideo -> currentState.copy(canYoutubeVideoShow = partialState.canShow)
            is SalesOnCashContract.PartialState.SetFirstTime -> currentState.copy(isFirstTime = partialState.firstTime)
            is SalesOnCashContract.PartialState.ShowAlert -> currentState.copy(
                alert = partialState.msg,
                canShowAlert = true
            )
            SalesOnCashContract.PartialState.HideAlert -> currentState.copy(canShowAlert = false)
            is SalesOnCashContract.PartialState.SetBillingAb -> currentState.copy(isBillingAbEnabled = partialState.isBillingAbEnabled)
            is SalesOnCashContract.PartialState.SetBillingInfoGraphicAb -> currentState.copy(
                isBillingInfoGraphicAbEnabled = partialState.isBillingInfoGraphicAbEnabled
            )
        }
    }
}
