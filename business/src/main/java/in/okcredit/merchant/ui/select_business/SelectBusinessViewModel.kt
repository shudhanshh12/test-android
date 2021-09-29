package `in`.okcredit.merchant.ui.select_business

import `in`.okcredit.merchant.merchant.R
import `in`.okcredit.merchant.usecase.GetBusinessList
import `in`.okcredit.merchant.usecase.GetNetBalanceForBusiness
import `in`.okcredit.merchant.usecase.SwitchBusiness
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class SelectBusinessViewModel @Inject constructor(
    state: SelectBusinessContract.State,
    private val getBusinessList: Lazy<GetBusinessList>,
    private val switchBusiness: Lazy<SwitchBusiness>,
    private val context: Lazy<Context>,
    private val getNetBalanceForBusiness: Lazy<GetNetBalanceForBusiness>,
) : BaseViewModel<SelectBusinessContract.State, SelectBusinessContract.PartialState, SelectBusinessContract.ViewEvent>(
    state
) {
    override fun handle(): Observable<out UiState.Partial<SelectBusinessContract.State>> {
        return Observable.mergeArray(
            handleLoad(),
            handleSetActiveBusinessIntent()
        )
    }

    private fun handleLoad() = intent<SelectBusinessContract.Intent.Load>()
        .switchMap { getBusinessList.get().execute().toObservable() }
        .flatMapSingle {
            rxSingle {
                val list = it.map { business ->
                    // TODO - uncomment after fixing net balance bug
                    // val balance = getNetBalanceForBusiness.get().execute(business.id).firstOrError().await()
                    SelectBusinessContract.BusinessData(business)
                }
                SelectBusinessContract.PartialState.SetBusinessData(list)
            }
        }

    private fun handleSetActiveBusinessIntent() = intent<SelectBusinessContract.Intent.SetActiveBusiness>()
        .switchMap { wrap { switchBusiness.get().execute(it.activeBusinessId, it.businessName, it.weakActivity) } }
        .map {
            if (it is Result.Failure) {
                emitViewEvent(SelectBusinessContract.ViewEvent.ShowError(context.get().getString(R.string.err_default)))
            }
            SelectBusinessContract.PartialState.NoChange
        }

    override fun reduce(
        currentState: SelectBusinessContract.State,
        partialState: SelectBusinessContract.PartialState,
    ): SelectBusinessContract.State {
        return when (partialState) {
            SelectBusinessContract.PartialState.NoChange -> currentState
            is SelectBusinessContract.PartialState.SetBusinessData -> currentState.copy(businessList = partialState.businessDataList)
        }
    }
}
