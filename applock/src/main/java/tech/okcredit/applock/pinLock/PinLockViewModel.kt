package tech.okcredit.applock.pinLock

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableSource
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.android.auth.usecases.ResetPassword
import tech.okcredit.applock.AppLockActivityV2.Companion.ENTRY
import tech.okcredit.applock.R
import tech.okcredit.applock.changePin.ChangeSecurityPinFragment.Companion.Source
import tech.okcredit.applock.pinLock.PinLockContract.*
import tech.okcredit.applock.pinLock.usecase.UpdatePinPrefStatus
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class PinLockViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam(Source) val source: String,
    @ViewModelParam(ENTRY) val entry: String,
    private val isPasswordSet: Lazy<IsPasswordSet>,
    private val resetPassword: Lazy<ResetPassword>,
    private val updatePinPrefStatus: Lazy<UpdatePinPrefStatus>
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .switchMap { UseCase.wrapSingle(isPasswordSet.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.SetIsUpdatePin(it.value)
                        is Result.Failure -> PartialState.NoChange
                    }
                },
            setPin(),
            verifyPin(),
            verifyPinApiCall(),
            updateMerchantPreference(),
            updateSource()
        )
    }

    private fun updateSource(): Observable<PartialState> {
        return intent<Intent.Load>()
            .map { PartialState.SetSourceAndEntry(source, entry) }
    }

    private fun updateMerchantPreference(): Observable<PartialState> {
        return intent<Intent.FourDigitPinUpdated>()
            .switchMap { updatePinPrefStatus.get().execute(true) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Failure -> PartialState.NoChange
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.PinVerified)
                        PartialState.NoChange
                    }
                }
            }
    }

    private fun verifyPinApiCall(): ObservableSource<out PartialState>? {
        return intent<Intent.ConfirmPin>()
            .filter { it.pinValue.equals(it.confirmPin) }
            .switchMap { UseCase.wrapCompletable(resetPassword.get().execute(it.confirmPin)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.UpdateMerchantPref)
                        PartialState.NoChange
                    }
                    is Result.Failure -> {
                        if (isInternetIssue(it.error)) {
                            emitViewEvent(ViewEvent.ShowError(R.string.err_network))
                            PartialState.NoChange
                        } else {
                            emitViewEvent(ViewEvent.ShowError(R.string.default_error_msg))
                            PartialState.NoChange
                        }
                    }
                }
            }
    }

    private fun setPin(): Observable<PartialState> {
        return intent<Intent.SetPin>()
            .map {
                emitViewEvent(ViewEvent.AskConfirmPin)
                PartialState.SetPinValue(it.pinValue)
            }
    }

    private fun verifyPin(): Observable<PartialState> {
        return intent<Intent.ConfirmPin>()
            .map {
                if (it.pinValue == it.confirmPin) {
                    PartialState.NoChange
                } else {
                    emitViewEvent(ViewEvent.ShowIncorrectPin)
                    PartialState.SetIncorrectPin
                }
            }
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState.copy(isIncorrectPin = false)
            is PartialState.SetPinValue -> currentState.copy(pinValue = partialState.pinValue)
            is PartialState.SetIncorrectPin -> currentState.copy(isIncorrectPin = true)
            is PartialState.SetIsUpdatePin -> currentState.copy(isUpdatePin = partialState.isUpdatePin)
            is PartialState.SetSourceAndEntry -> currentState.copy(source = partialState.source, entry = partialState.entry)
        }
    }
}
