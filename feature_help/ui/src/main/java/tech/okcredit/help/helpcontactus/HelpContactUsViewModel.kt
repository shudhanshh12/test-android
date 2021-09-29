package tech.okcredit.help.helpcontactus

import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.app_contract.AppConstants
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.help.utils.PhoneBookUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HelpContactUsViewModel @Inject constructor(
    initialState: HelpContactUsContract.State,
    private val getMerchantPreference: GetMerchantPreference,
    private val ab: AbRepository,
    @ViewModelParam(AppConstants.ARG_SOURCE) val source: String,
    private val context: Context,
    private val checkNetworkHealth: CheckNetworkHealth,
    private val navigator: HelpContactUsContract.Navigator,
    private val getSupportNumber: Lazy<GetSupportNumber>
) : BasePresenter<HelpContactUsContract.State, HelpContactUsContract.PartialState>(
    initialState
) {
    companion object {
        const val HELP_ID = "help_id"
        const val HELP_FEATURE = "help_chat"
    }

    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private var contactPermissionAvailable = false
    private var isManualChatEnabled = false
    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    override fun handle(): Observable<UiState.Partial<HelpContactUsContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<HelpContactUsContract.Intent.Load>()
                .switchMap { checkNetworkHealth.execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                        HelpContactUsContract.PartialState.ClearNetworkError
                    } else {
                        HelpContactUsContract.PartialState.NoChange
                    }
                },

            intent<HelpContactUsContract.Intent.Load>()
                .map {
                    HelpContactUsContract.PartialState.SetSourceScreen(source)
                },

            // abvarient_experiment
            intent<HelpContactUsContract.Intent.Load>()
                .switchMap {
                    ab.isFeatureEnabled(HELP_FEATURE)
                }.map {
                    isManualChatEnabled = it
                    HelpContactUsContract.PartialState.SetManualChatEnabled(it)
                },

            // load screen
            intent<HelpContactUsContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(Observable.just("")) }
                .map {
                    when (it) {
                        is Result.Progress -> HelpContactUsContract.PartialState.NoChange
                        is Result.Success -> {
                            HelpContactUsContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    HelpContactUsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> HelpContactUsContract.PartialState.SetNetworkError(true)
                                else -> HelpContactUsContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            /***********************  WhatsApp Us  ***********************/
            intent<HelpContactUsContract.Intent.WhatsApp>()
                .switchMap {
                    contactPermissionAvailable = it.contactPermissionAvailable
                    UseCase.wrapSingle(
                        getMerchantPreference.execute(PreferenceKey.WHATSAPP)
                            .firstOrError()
                    )
                }
                .map {

                    when (it) {
                        is Result.Progress -> HelpContactUsContract.PartialState.NoChange
                        is Result.Success -> {

                            val isWhatsAppEnabled = it.value!!.toBoolean()
                            if (isWhatsAppEnabled && contactPermissionAvailable) {

                                val disposable =
                                    PhoneBookUtils.addOkCreditNumberToContact(context, getSupportNumber.get().supportNumber)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            navigator.openWhatsApp(getSupportNumber.get().supportNumber)
                                        }
                                disposables.add(disposable)
                            } else {
                                navigator.goToWhatsAppOptIn()
                            }
                            HelpContactUsContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            navigator.openWhatsApp(getSupportNumber.get().supportNumber)
                            HelpContactUsContract.PartialState.NoChange
                        }
                    }
                },

            intent<HelpContactUsContract.Intent.ContactUs>()
                .map {
                    if (isManualChatEnabled) navigator.goToManualChatScreen() else navigator.onContactUsClicked()
                    HelpContactUsContract.PartialState.NoChange
                },

            intent<HelpContactUsContract.Intent.EmailUs>()
                .map {
                    navigator.onEmailClicked()
                    HelpContactUsContract.PartialState.NoChange
                },

            // handle `show alert` intent
            intent<HelpContactUsContract.Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<HelpContactUsContract.PartialState> { HelpContactUsContract.PartialState.HideAlert }
                        .startWith(HelpContactUsContract.PartialState.ShowAlert(it.message))
                }
        )
    }

    override fun reduce(
        currentState: HelpContactUsContract.State,
        partialState: HelpContactUsContract.PartialState
    ): HelpContactUsContract.State {
        return when (partialState) {
            is HelpContactUsContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is HelpContactUsContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is HelpContactUsContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is HelpContactUsContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is HelpContactUsContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is HelpContactUsContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is HelpContactUsContract.PartialState.SetSourceScreen -> currentState.copy(sourceScreen = partialState.source)
            is HelpContactUsContract.PartialState.NoChange -> currentState
            is HelpContactUsContract.PartialState.SetManualChatEnabled -> currentState.copy(isManualChatEnabled = partialState.isManualChatEnabled)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
