package tech.okcredit.help.helpHome

import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.help.helpHome.HelpHomeContract.*
import tech.okcredit.help.helpHome.usecase.AddOkcreditNumberToContact
import tech.okcredit.help.helpHome.usecase.SyncHelpData
import javax.inject.Inject

class HelpHomeViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam(ARG_SOURCE) val source: String,
    private val getMerchantPreference: Lazy<GetMerchantPreference>,
    private val addOkcreditNumberToContact: Lazy<AddOkcreditNumberToContact>,
    private val getSupportNumber: Lazy<GetSupportNumber>,
    private val syncHelpData: Lazy<SyncHelpData>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {
    private var contactPermissionAvailable: Boolean = false
    private var isWhatsAppEnabledForThisUser: Boolean = false

    companion object {
        const val HELP_ID = "help_id"
        const val ARG_SOURCE = "source"
    }

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .map {
                    PartialState.SetSourceScreen(source)
                },
            intent<Intent.Load>()
                .switchMap { wrap(syncHelpData.get().execute()) }
                .map { PartialState.NoChange },
            intent<Intent.ClickHelp>()
                .map {
                    emitViewEvent(ViewEvent.GoToHelp(source))
                    PartialState.NoChange
                },
            intent<Intent.AboutUsClick>()
                .map {
                    emitViewEvent(ViewEvent.GoToAboutUsScreen)
                    PartialState.NoChange
                },
            intent<Intent.PrivacyClick>()
                .map {
                    emitViewEvent(ViewEvent.GoToPrivacyScreen)
                    PartialState.NoChange
                },
            intent<Intent.ChatWithUsClick>()
                .map {
                    emitViewEvent(ViewEvent.GoToManualChatScreen)
                    PartialState.NoChange
                },
            // getting contact permission enabled or not
            intent<Intent.OnWhatsAppPermissionCheck>()
                .map {
                    contactPermissionAvailable = it.isWhatsAppContactPermissionEnabled
                    PartialState.NoChange
                },
            // checking is whatsapp enabled for this user
            intent<Intent.OnWhatsAppPermissionCheck>()
                .switchMap {
                    UseCase.wrapSingle(
                        getMerchantPreference.get().execute(PreferenceKey.WHATSAPP).firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            isWhatsAppEnabledForThisUser = it.value!!.toBoolean()
                            PartialState.NoChange
                        }
                        is Result.Failure -> {
                            emitViewEvent(ViewEvent.OpenWhatsApp(getSupportNumber.get().supportNumber))
                            PartialState.NoChange
                        }
                    }
                },

            // if whatsapp and contact permission enabled add into contacts
            intent<Intent.OnWhatsAppPermissionCheck>()
                .filter { isWhatsAppEnabledForThisUser && contactPermissionAvailable }
                .switchMap { addOkcreditNumberToContact.get().execute(getSupportNumber.get().supportNumber) }
                .map {
                    when (it) {
                        is Result.Failure -> PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.OpenWhatsApp(getSupportNumber.get().supportNumber))
                            PartialState.NoChange
                        }
                        is Result.Progress -> PartialState.NoChange
                    }
                },

            intent<Intent.OnWhatsAppPermissionCheck>()
                .filter { !(isWhatsAppEnabledForThisUser && contactPermissionAvailable) }
                .map {
                    emitViewEvent(ViewEvent.GoToWhatsAppScreen)
                    PartialState.NoChange
                }

        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetSourceScreen -> currentState.copy(sourceScreen = partialState.source)
        }
    }
}
