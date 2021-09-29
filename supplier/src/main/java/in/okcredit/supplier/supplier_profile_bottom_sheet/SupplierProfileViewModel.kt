package `in`.okcredit.supplier.supplier_profile_bottom_sheet

import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.supplier.R
import `in`.okcredit.supplier.supplier_profile_bottom_sheet.SupplierProfileBottomSheet.Companion.ARG_SUPPLIER_ID_PROFILE_PAGE
import `in`.okcredit.supplier.usecase.GetSupplier
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.supplier.contract.IsAccountChatEnabledForSupplier
import merchant.okcredit.supplier.contract.IsSupplierCollectionEnabled
import tech.okcredit.account_chat_contract.IGetChatUnreadMessageCount
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class SupplierProfileViewModel @Inject constructor(
    initialState: SupplierProfileContract.State,
    @ViewModelParam(ARG_SUPPLIER_ID_PROFILE_PAGE) val supplierId: String,
    private val getSupplier: Lazy<GetSupplier>,
    private val getChatUnreadMessages: Lazy<IGetChatUnreadMessageCount>,
    private val isAccountChatEnabledForSupplier: Lazy<IsAccountChatEnabledForSupplier>,
    private val isSupplierCollectionEnabled: Lazy<IsSupplierCollectionEnabled>,
    val context: Lazy<Context>
) : BaseViewModel<SupplierProfileContract.State, SupplierProfileContract.PartialState, SupplierProfileContract.ViewEvents>(
    initialState
) {

    internal var supplier: Supplier? = null

    override fun handle(): Observable<out UiState.Partial<SupplierProfileContract.State>> {
        return Observable.mergeArray(
            getSupplierDetails(),
            actionOnCall(),
            shareWhatsappReminder(),
            getChatMessageInfo(),
            redirectToChatScreen(),
            gotoSupplierPaymentScreen(),
            setAccountChatEnabled(),
            setSupplierCollectionEnabled()
        )
    }

    private fun setAccountChatEnabled(): Observable<SupplierProfileContract.PartialState> {
        return intent<SupplierProfileContract.Intent.Load>()
            .take(1)
            .switchMap {
                UseCase.wrapObservable(isAccountChatEnabledForSupplier.get().execute())
            }
            .filter { it is Result.Success }
            .map {
                SupplierProfileContract.PartialState.SetChatEnabled((it as Result.Success).value)
            }
    }

    private fun setSupplierCollectionEnabled(): Observable<SupplierProfileContract.PartialState> {
        return intent<SupplierProfileContract.Intent.Load>()
            .take(1)
            .switchMap { UseCase.wrapObservable(isSupplierCollectionEnabled.get().execute()) }
            .filter { it is Result.Success }
            .map {
                SupplierProfileContract.PartialState.SetPaymentEnabled((it as Result.Success).value)
            }
    }

    private fun getSupplierDetails(): Observable<SupplierProfileContract.PartialState> {
        return intent<SupplierProfileContract.Intent.Load>()
            .take(1)
            .switchMap {
                getSupplier.get().execute(supplierId)
            }
            .map {
                when (it) {
                    is Result.Progress -> SupplierProfileContract.PartialState.NoChange
                    is Result.Success -> {
                        supplier = it.value
                        SupplierProfileContract.PartialState.SetSupplier(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> emitViewEvent(
                                SupplierProfileContract.ViewEvents.ShowToast(
                                    context.get().getString(R.string.supplier_network)
                                )
                            )
                            else -> {
                                emitViewEvent(
                                    SupplierProfileContract.ViewEvents.ShowToast(
                                        context.get().getString(R.string.supplier_other_error)
                                    )
                                )
                            }
                        }
                        SupplierProfileContract.PartialState.NoChange
                    }
                }
            }
    }

    private fun actionOnCall(): Observable<SupplierProfileContract.PartialState> {
        return intent<SupplierProfileContract.Intent.ActionOnCall>()
            .map {
                supplier?.let {
                    if (it.mobile.isNullOrEmpty()) {
                        emitViewEvent(SupplierProfileContract.ViewEvents.AddSupplierMobile(it.id))
                    } else {
                        emitViewEvent(SupplierProfileContract.ViewEvents.CallToSupplier(it.mobile!!))
                    }
                }
                SupplierProfileContract.PartialState.NoChange
            }
    }

    private fun shareWhatsappReminder(): Observable<SupplierProfileContract.PartialState> {
        return intent<SupplierProfileContract.Intent.SendWhatsAppReminder>()
            .map {
                emitViewEvent(
                    SupplierProfileContract.ViewEvents.ShareWhatsappReminder(
                        supplier?.mobile,
                        supplier?.name ?: ""
                    )
                )
                SupplierProfileContract.PartialState.NoChange
            }
    }

    private fun redirectToChatScreen(): Observable<SupplierProfileContract.PartialState> {
        return intent<SupplierProfileContract.Intent.RedirectToChatScreen>()
            .map {
                emitViewEvent(
                    SupplierProfileContract.ViewEvents.RedirectToChatScreen
                )
                SupplierProfileContract.PartialState.NoChange
            }
    }

    private fun gotoSupplierPaymentScreen(): Observable<SupplierProfileContract.PartialState> {
        return intent<SupplierProfileContract.Intent.GoToSupplierPaymentScreen>()
            .map {
                emitViewEvent(
                    SupplierProfileContract.ViewEvents.GoToSupplierPaymentScreen(supplierId)
                )
                SupplierProfileContract.PartialState.NoChange
            }
    }

    private fun getChatMessageInfo(): Observable<SupplierProfileContract.PartialState> {
        return intent<SupplierProfileContract.Intent.Load>()
            .switchMap {
                getChatUnreadMessages.get().execute((supplierId))
            }
            .map {
                when (it) {
                    is Result.Progress -> SupplierProfileContract.PartialState.NoChange
                    is Result.Success -> {
                        var count = it.value.first
                        val id = it.value.second
                        if (count == "0") {
                            SupplierProfileContract.PartialState.SetUnreadMessageCount("", null)
                        } else if (count.isNotEmpty() && id.isNullOrEmpty().not()) {
                            if (count.toInt() > 9) {
                                count = "9+"
                            }
                            SupplierProfileContract.PartialState.SetUnreadMessageCount(count, it.value.second!!)
                        } else SupplierProfileContract.PartialState.NoChange
                    }

                    is Result.Failure -> SupplierProfileContract.PartialState.NoChange
                }
            }
    }

    override fun reduce(
        currentState: SupplierProfileContract.State,
        partialState: SupplierProfileContract.PartialState
    ): SupplierProfileContract.State {
        return when (partialState) {
            is SupplierProfileContract.PartialState.NoChange -> currentState
            is SupplierProfileContract.PartialState.SetSupplier -> currentState.copy(supplier = partialState.supplier)
            is SupplierProfileContract.PartialState.SetUnreadMessageCount -> currentState.copy(
                unreadMessageCount = partialState.unreadMessageCount,
                firstUnseenMessageId = partialState.firstUnseenMessageId
            )
            is SupplierProfileContract.PartialState.SetChatEnabled -> currentState.copy(isChatEnabled = partialState.isEnabled)
            is SupplierProfileContract.PartialState.SetPaymentEnabled -> currentState.copy(isPaymentEnabled = partialState.isEnabled)
        }
    }
}
