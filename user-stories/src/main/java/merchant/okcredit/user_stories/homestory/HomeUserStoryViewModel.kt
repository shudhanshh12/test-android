package merchant.okcredit.user_stories.homestory

import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.BaseLayoutViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import merchant.okcredit.user_stories.usecase.GetActiveMyStoryCount
import merchant.okcredit.user_stories.usecase.GetHomeUserStories
import tech.okcredit.android.base.utils.DateTimeUtils
import javax.inject.Inject

class HomeUserStoryViewModel @Inject constructor(
    initialState: HomeUserStoryContract.State,
    private val getHomeUserStories: Lazy<GetHomeUserStories>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val getActiveMyStoryCount: Lazy<GetActiveMyStoryCount>,
) :
    BaseLayoutViewModel<HomeUserStoryContract.State, HomeUserStoryContract.PartialState>(
        initialState,
        Schedulers.newThread(),
        Schedulers.newThread()
    ) {
    private lateinit var interactor: HomeUserStoryContract.Interactor
    override fun handle(): Observable<out UiState.Partial<HomeUserStoryContract.State>> {
        return Observable.mergeArray(
            intent<HomeUserStoryContract.Intent.Load>()
                .switchMap { getHomeUserStories.get().execute(Unit) }
                .map {
                    when (it) {
                        is Result.Success -> {

                            if (it.value.isEnabled) {
                                HomeUserStoryContract.PartialState.SetHomeStories(it.value.homeStories)
                            } else {
                                HomeUserStoryContract.PartialState.SetUserStoryEnable(it.value.isEnabled)
                            }
                        }
                        else -> HomeUserStoryContract.PartialState.ErrorState
                    }
                },
            intent<HomeUserStoryContract.Intent.Load>()
                .switchMap { getActiveBusiness.get().execute() }
                .map {
                    HomeUserStoryContract.PartialState.SetActiveMerchantId(it.id)
                },

            intent<HomeUserStoryContract.Intent.Load>()
                .switchMap {
                    UseCase.wrapSingle(
                        getActiveMyStoryCount.get().execute(DateTimeUtils.currentDateTime().millis)
                    )
                }
                .map {
                    when (it) {
                        is Result.Success -> HomeUserStoryContract.PartialState.SetActiveMyStoryCount(it.value)
                        else -> HomeUserStoryContract.PartialState.NoChange
                    }
                }

        )
    }

    override fun reduce(
        currentState: HomeUserStoryContract.State,
        partialState: HomeUserStoryContract.PartialState,
    ): HomeUserStoryContract.State {
        return when (partialState) {
            is HomeUserStoryContract.PartialState.NoChange -> currentState
            is HomeUserStoryContract.PartialState.SetHomeStories -> currentState.copy(
                homeUserStory = partialState.homeUserStory,
                isLoading = false,
                isUserStoryEnabled = true
            )
            is HomeUserStoryContract.PartialState.SetLoading -> currentState.copy(isLoading = partialState.loading)
            is HomeUserStoryContract.PartialState.ErrorState -> currentState
            is HomeUserStoryContract.PartialState.SetUserStoryEnable ->
                currentState.copy(isUserStoryEnabled = partialState.enabled, isLoading = false)
            is HomeUserStoryContract.PartialState.SetActiveMerchantId -> currentState.copy(activeMerchantId = partialState.activeMerchantId)
            is HomeUserStoryContract.PartialState.SetActiveMyStoryCount -> currentState.copy(activeMyStoryCount = partialState.activeMyStoryCount)
        }
    }

    override fun setNavigation(baseLayout: BaseLayout<HomeUserStoryContract.State>) {
        this.interactor = genericCastOrNull<HomeUserStoryContract.Interactor>(baseLayout)!!
    }
}
