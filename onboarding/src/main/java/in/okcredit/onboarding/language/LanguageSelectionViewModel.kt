package `in`.okcredit.onboarding.language

import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.onboarding.language.LanguageSelectionContract.*
import `in`.okcredit.onboarding.language.usecase.GetSortedLanguages
import `in`.okcredit.onboarding.language.usecase.SelectLanguage
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class LanguageSelectionViewModel @Inject constructor(
    initialState: State,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getSortedLanguages: Lazy<GetSortedLanguages>,
    private val selectLanguage: Lazy<SelectLanguage>,
    private val onboardingPreferences: Lazy<OnboardingPreferences>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    override fun handle(): Observable<PartialState> {
        return mergeArray(
            intent<Intent.OnResume>()
                .switchMap {
                    wrap(
                        rxSingle {
                            val selected = onboardingPreferences.get().getUserSelectedLanguage()
                            selected to getSortedLanguages.get().execute(selected)
                        }
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.ShowLoading
                        is Result.Success -> PartialState.SetLanguages(it.value.first, it.value.second)
                        is Result.Failure -> PartialState.StopLoading
                    }
                },

            intent<Intent.LanguageSelected>()
                .switchMapSingle {
                    onboardingPreferences.get().setUserSelectedLanguage(it.selectedLanguage)
                    selectLanguage.get().execute(it.selectedLanguage)
                        .toSingleDefault(firebaseRemoteConfig.get().getBoolean(IS_SOCIAL_VALIDATION_ENABLED))
                }
                .map {
                    val nextStep = if (it) ViewEvent.GoToWelcomeSocialValidation else ViewEvent.GoToEnterPhoneNumber
                    emitViewEvent(nextStep)
                    PartialState.NoChange
                },
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is PartialState.StopLoading -> currentState.copy(isLoading = false)
            is PartialState.SetLanguages -> currentState.copy(
                isLoading = false,
                languages = partialState.languages,
                selectedLanguage = partialState.selectedLanguage
            )
        }
    }

    companion object {
        const val IS_SOCIAL_VALIDATION_ENABLED = "is_social_validation_enabled"
    }
}
