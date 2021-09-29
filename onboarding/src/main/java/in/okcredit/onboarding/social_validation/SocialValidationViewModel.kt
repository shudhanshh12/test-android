package `in`.okcredit.onboarding.social_validation

import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.social_validation.SocialValidationContract.*
import `in`.okcredit.onboarding.social_validation.data.FetchSocialValidationScreens
import `in`.okcredit.onboarding.social_validation.data.SocialValidationPage
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import kotlinx.coroutines.rx2.rxSingle
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class SocialValidationViewModel @Inject constructor(
    initialState: State,
    private val onboardingAnalytics: Lazy<OnboardingAnalytics>,
    private val fetchSocialValidationScreens: Lazy<FetchSocialValidationScreens>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    override fun handle(): Observable<PartialState> {
        return mergeArray(
            loadScreens(),

            intent<Intent.GetStarted>()
                .doOnNext { emitViewEvent(ViewEvent.GoToEnterPhoneNumber) }
                .map { PartialState.NoChange },
        )
    }

    private fun loadScreens() = intent<Intent.Load>()
        .switchMap {
            wrap(
                rxSingle {
                    withTimeout(2_000) {
                        fetchSocialValidationScreens.get().execute()
                    }
                }
            )
        }
        .map {
            when (it) {
                is Result.Progress -> PartialState.ShowLoading
                is Result.Success -> {
                    val pages = it.value.filterIsInstance<SocialValidationPage.ImageBacked>()
                    onboardingAnalytics.get().trackSocialScreenLoaded(true, pages.size)
                    PartialState.SetPages(pages)
                }
                is Result.Failure -> {
                    val pages = defaultPages.filterIsInstance<SocialValidationPage.ImageBacked>()
                    onboardingAnalytics.get().trackSocialScreenLoaded(false, pages.size)
                    PartialState.SetPages(pages)
                }
            }
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is PartialState.StopLoading -> currentState.copy(isLoading = false)
            is PartialState.SetPages -> currentState.copy(isLoading = false, pages = partialState.pages)
        }
    }

    companion object {
        private val defaultPages = listOf(
            SocialValidationPage.ImageBacked(
                "https://storage.googleapis.com/onboarding-social-experiment/WhatsApp%20Image%202021-08-30%20at%2010.50.14%20AM.jpeg",
                5000L
            ),
            SocialValidationPage.ImageBacked(
                "https://storage.googleapis.com/onboarding-social-experiment/WhatsApp%20Image%202021-08-30%20at%2010.50.14%20AM.jpeg",
                5000L
            ),
            SocialValidationPage.ImageBacked(
                "https://storage.googleapis.com/onboarding-social-experiment/WhatsApp%20Image%202021-08-30%20at%2010.50.14%20AM.jpeg",
                5000L
            ),
            SocialValidationPage.ImageBacked(
                "https://storage.googleapis.com/onboarding-social-experiment/WhatsApp%20Image%202021-08-30%20at%2010.50.14%20AM.jpeg",
                5000L
            ),
            SocialValidationPage.ImageBacked(
                "https://storage.googleapis.com/onboarding-social-experiment/WhatsApp%20Image%202021-08-30%20at%2010.50.14%20AM.jpeg",
                5000L
            ),
            SocialValidationPage.ImageBacked(
                "https://storage.googleapis.com/onboarding-social-experiment/WhatsApp%20Image%202021-08-30%20at%2010.50.14%20AM.jpeg",
                5000L
            ),
            SocialValidationPage.VideoBacked(
                "https://storage.googleapis.com/onboarding-social-experiment/WhatsApp%20Video%202021-08-22%20at%2011.37.51%20AM.mp4",
                ""
            )
        )
    }
}
