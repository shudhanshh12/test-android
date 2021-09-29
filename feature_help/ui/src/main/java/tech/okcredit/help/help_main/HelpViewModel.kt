package tech.okcredit.help.help_main

import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.help.HelpActivity
import tech.okcredit.help.helpHome.usecase.AddOkcreditNumberToContact
import tech.okcredit.userSupport.ContextualHelp
import tech.okcredit.userSupport.SupportRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HelpViewModel @Inject constructor(
    initialState: HelpContract.State,
    private val userSupport: Lazy<SupportRepository>,
    @ViewModelParam(HelpActivity.HELP_ID) val filterHelpIds: List<String>,
    @ViewModelParam(HelpActivity.EXTRA_SOURCE) val source: String,
    @ViewModelParam(HelpActivity.EXTRA_CONTEXTUAL_HELP) val contextualHelp: ContextualHelp?,
    private val checkNetworkHealth: CheckNetworkHealth,
    private val addOkCreditNumberToContact: Lazy<AddOkcreditNumberToContact>,
    private val getMerchantPreference: Lazy<GetMerchantPreference>,
    private val getSupportNumber: Lazy<GetSupportNumber>
) : BaseViewModel<HelpContract.State, HelpContract.PartialState, HelpContract.ViewEvent>(initialState) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private var contactPermissionAvailable: Boolean = false
    private var isWhatsAppEnabledForThisUser: Boolean = false

    override fun handle(): Observable<UiState.Partial<HelpContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<HelpContract.Intent.Load>()
                .take(1)
                .switchMap { checkNetworkHealth.execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                        HelpContract.PartialState.ClearNetworkError
                    } else {
                        HelpContract.PartialState.NoChange
                    }
                },

            intent<HelpContract.Intent.Load>()
                .take(1)
                .map {
                    HelpContract.PartialState.SetSourceScreen(source)
                },

            intent<HelpContract.Intent.ChatWithUsClick>()
                .map {
                    emitViewEvent(HelpContract.ViewEvent.GoToManualChatScreen)
                    HelpContract.PartialState.NoChange
                },

            // getting contact permission enabled or not
            intent<HelpContract.Intent.OnWhatsAppPermissionCheck>()
                .map {
                    contactPermissionAvailable = it.isWhatsAppContactPermissionEnabled
                    HelpContract.PartialState.NoChange
                },

            // checking is whatsapp enabled for this user
            intent<HelpContract.Intent.OnWhatsAppPermissionCheck>()
                .switchMap {
                    UseCase.wrapSingle(
                        getMerchantPreference.get().execute(PreferenceKey.WHATSAPP).firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> HelpContract.PartialState.NoChange
                        is Result.Success -> {
                            isWhatsAppEnabledForThisUser = it.value!!.toBoolean()
                            HelpContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            emitViewEvent(HelpContract.ViewEvent.OpenWhatsApp(getSupportNumber.get().supportNumber))
                            HelpContract.PartialState.NoChange
                        }
                    }
                },

            // if whatsapp and contact permission enabled add into contacts
            intent<HelpContract.Intent.OnWhatsAppPermissionCheck>()
                .filter { isWhatsAppEnabledForThisUser && contactPermissionAvailable }
                .switchMap { addOkCreditNumberToContact.get().execute(getSupportNumber.get().supportNumber) }
                .map {
                    when (it) {
                        is Result.Failure -> HelpContract.PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(HelpContract.ViewEvent.OpenWhatsApp(getSupportNumber.get().supportNumber))
                            HelpContract.PartialState.NoChange
                        }
                        is Result.Progress -> HelpContract.PartialState.NoChange
                    }
                },

            intent<HelpContract.Intent.OnWhatsAppPermissionCheck>()
                .filter { !(isWhatsAppEnabledForThisUser && contactPermissionAvailable) }
                .map {
                    emitViewEvent(HelpContract.ViewEvent.GoToWhatsAppOptIn)
                    HelpContract.PartialState.NoChange
                },

            // load screen
            intent<HelpContract.Intent.Load>()
                .take(1)
                .switchMap {
                    UseCase.wrapObservable(userSupport.get().getHelp())
                }
                .map {
                    when (it) {
                        is Result.Progress -> HelpContract.PartialState.NoChange
                        is Result.Success -> {
                            when {
                                filterHelpIds.isNotEmpty() -> {
                                    val list = filterHelpIds.mapNotNull { id ->
                                        it.value.find { help -> help.id == id }
                                    }
                                    HelpContract.PartialState.SetHelpData(list, true)
                                }

                                contextualHelp != null -> {
                                    val list = contextualHelp.value.mapNotNull { displayType ->
                                        it.value.find { help -> help.display_type == displayType }
                                    }
                                    HelpContract.PartialState.SetHelpData(list, true)
                                }

                                else -> HelpContract.PartialState.SetHelpData(it.value)
                            }
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> HelpContract.PartialState.SetNetworkError(true)
                                else -> HelpContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            intent<HelpContract.Intent.Load>()
                .take(1)
                .map {
                    if (filterHelpIds.isEmpty())
                        emitViewEvent(HelpContract.ViewEvent.OpenDefaultFaq)
                    HelpContract.PartialState.NoChange
                },

            intent<HelpContract.Intent.MainItemClick>()
                .map {
                    HelpContract.PartialState.ExpandedId(it.secId, it.isExpanded)
                },

            intent<HelpContract.Intent.OnSectionItemClick>()
                .map {
                    emitViewEvent(HelpContract.ViewEvent.GotoHelpItem(it.secId))
                    HelpContract.PartialState.NoChange
                },

            // handle `show alert` intent
            intent<HelpContract.Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<HelpContract.PartialState> { HelpContract.PartialState.HideAlert }
                        .startWith(HelpContract.PartialState.ShowAlert(it.message))
                }
        )
    }

    override fun reduce(currentState: HelpContract.State, partialState: HelpContract.PartialState): HelpContract.State {
        return when (partialState) {
            is HelpContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is HelpContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is HelpContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is HelpContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is HelpContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is HelpContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is HelpContract.PartialState.SetHelpData -> {
                if (partialState.expand) {
                    currentState.copy(
                        help = partialState.helpList,
                        expandedId = partialState.helpList[0].id,
                        isExpanded = true
                    )
                } else {
                    currentState.copy(
                        help = partialState.helpList,
                        isExpanded = false
                    )
                }
            }
            is HelpContract.PartialState.NoChange -> currentState
            is HelpContract.PartialState.ExpandedId ->
                currentState.copy(
                    expandedId = if (partialState.isExpanded) partialState.expandedId else "",
                    isExpanded = partialState.isExpanded
                )
            is HelpContract.PartialState.SetSourceScreen -> currentState.copy(sourceScreen = partialState.source)
        }
    }
}
