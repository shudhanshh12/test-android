package `in`.okcredit.merchant.ui.create_business

import `in`.okcredit.merchant.merchant.R
import `in`.okcredit.merchant.server.BusinessRemoteServerImpl.Companion.BUSINESS_LIMIT_REACHED
import `in`.okcredit.merchant.usecase.CreateBusiness
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CreateBusinessViewModel @Inject constructor(
    state: CreateBusinessContract.State,
    private val context: Lazy<Context>,
    private val createBusiness: Lazy<CreateBusiness>,
    private val createBusinessAnalytics: Lazy<CreateBusinessAnalytics>,
) : BaseViewModel<CreateBusinessContract.State, CreateBusinessContract.PartialState, CreateBusinessContract.ViewEvent>(
    state
) {
    override fun handle(): Observable<out UiState.Partial<CreateBusinessContract.State>> {
        return Observable.mergeArray(
            handleCreateBusinessIntent(),
            handleAutoDismissAndGoHome()
        )
    }

    private fun handleCreateBusinessIntent() = intent<CreateBusinessContract.Intent.CreateBusiness>()
        .switchMap { wrap { createBusiness.get().execute(it.businessName) } }
        .map {
            when (it) {
                is Result.Progress -> CreateBusinessContract.PartialState.SetLoading(true)
                is Result.Failure -> {
                    val message = when {
                        isInternetIssue(it.error) -> {
                            context.get().getString(R.string.home_no_internet_msg)
                        }
                        it.error.message == BUSINESS_LIMIT_REACHED -> {
                            createBusinessAnalytics.get().trackCreateBusinessError(BUSINESS_LIMIT_REACHED)
                            context.get().getString(R.string.t_001_multi_acc_msg_biz_count_max_limit_reached)
                        }
                        else -> {
                            createBusinessAnalytics.get().trackCreateBusinessError(it.error)
                            context.get().getString(R.string.err_default)
                        }
                    }
                    emitViewEvent(CreateBusinessContract.ViewEvent.ShowError(message))
                    CreateBusinessContract.PartialState.SetLoading(false)
                }
                is Result.Success -> {
                    emitViewEvent(CreateBusinessContract.ViewEvent.CreateSuccessful)
                    CreateBusinessContract.PartialState.SetSuccessful
                }
            }
        }

    private fun handleAutoDismissAndGoHome() = intent<CreateBusinessContract.Intent.AutoDismissAndGoToHome>()
        .delay(1000, TimeUnit.MILLISECONDS)
        .map {
            emitViewEvent(CreateBusinessContract.ViewEvent.DismissAndGoHome)
            CreateBusinessContract.PartialState.NoChange
        }

    override fun reduce(
        currentState: CreateBusinessContract.State,
        partialState: CreateBusinessContract.PartialState,
    ): CreateBusinessContract.State {
        return when (partialState) {
            CreateBusinessContract.PartialState.NoChange -> currentState
            is CreateBusinessContract.PartialState.SetLoading -> currentState.copy(
                loading = partialState.loading,
            )
            is CreateBusinessContract.PartialState.SetSuccessful -> currentState.copy(
                loading = false,
                successful = true
            )
        }
    }
}
