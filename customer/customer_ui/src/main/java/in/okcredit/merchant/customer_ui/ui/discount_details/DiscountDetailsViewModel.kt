package `in`.okcredit.merchant.customer_ui.ui.discount_details

import `in`.okcredit.backend._offline.usecase.GetMerchantPreferenceImpl
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.SmsHelper
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.customer_ui.usecase.*
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.ScreenName
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.help.utils.PhoneBookUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DiscountDetailsViewModel @Inject constructor(
    private val getActiveBusiness: GetActiveBusiness,
    private val scheduleSyncTransactions: Lazy<ScheduleSyncTransactions>,
    private val getCollection: GetCollection,
    private val getTxnDetails: GetTxnDetails,
    private val isTransactionPresent: IsTransactionPresent,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val smsHelper: SmsHelper,
    @ViewModelParam("transaction_id") val transactionId: String?,
    private val context: Context,
    private val deleteTransactionImage: DeleteTransactionImage,
    private val updateTransactionNote: UpdateTransactionNote,
    private val getReferralLink: GetReferralLink,
    private val updateTransactionImageLocally: UpdateTransactionImageLocally,
    private val uploadTransactionImage: UploadTransactionImage,
    private val navigator: DiscountDetailsContract.Navigator,
    private val getMerchantPreference: GetMerchantPreferenceImpl,
    private val getSupportNumber: Lazy<GetSupportNumber>,
    private val getContextualHelpIds: Lazy<GetContextualHelpIds>,
) : BaseViewModel<DiscountDetailsContract.State, DiscountDetailsContract.PartialState, DiscountDetailsContract.ViewEvent>(
    DiscountDetailsContract.State()
) {

    lateinit var customer: Customer
    lateinit var transaction: merchant.okcredit.accounting.model.Transaction
    lateinit var business: Business
    private var contactPermissionAvailable = false
    private val getCollectionPublishSubject: PublishSubject<String> = PublishSubject.create()

    override fun handle(): Observable<UiState.Partial<DiscountDetailsContract.State>> {
        return mergeArray(
            observeContextualHelpIdsOnLoad(),
            // hide network error when network becomes available
            intent<DiscountDetailsContract.Intent.Load>()
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        DiscountDetailsContract.PartialState.SetNetworkError(false)
                    } else {
                        DiscountDetailsContract.PartialState.NoChange
                    }
                },

            // handle `show alert` intent
            intent<DiscountDetailsContract.Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<DiscountDetailsContract.PartialState> { DiscountDetailsContract.PartialState.HideAlert }
                        .startWith(DiscountDetailsContract.PartialState.ShowAlert(it.message))
                },

            // load page
            intent<DiscountDetailsContract.Intent.Load>()
                .switchMap { getTxnDetails.execute(transactionId ?: "") }
                .map {
                    when (it) {
                        is Result.Progress -> DiscountDetailsContract.PartialState.NoChange
                        is Result.Success -> {
                            this.customer = it.value.customer
                            this.transaction = it.value.transaction
                            if (it.value.transaction.isOnlinePaymentTransaction) {
                                it.value.transaction.collectionId?.let { it1 -> getCollectionPublishSubject.onNext(it1) }
                            }
                            DiscountDetailsContract.PartialState.SetTransactionDetails(
                                it.value.transaction,
                                it.value.customer
                            )
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    DiscountDetailsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> DiscountDetailsContract.PartialState.NoChange
                                else -> DiscountDetailsContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            intent<DiscountDetailsContract.Intent.Load>()
                .switchMap { UseCase.wrapSingle(getReferralLink.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> DiscountDetailsContract.PartialState.NoChange
                        is Result.Success -> {
                            DiscountDetailsContract.PartialState.SetReferralId(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    DiscountDetailsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> DiscountDetailsContract.PartialState.NoChange
                                else -> DiscountDetailsContract.PartialState.NoChange
                            }
                        }
                    }
                },

            intent<DiscountDetailsContract.Intent.Load>()
                .switchMap { isTransactionPresent.execute(transactionId ?: "") }
                .map {
                    when (it) {
                        is Result.Progress -> DiscountDetailsContract.PartialState.NoChange
                        is Result.Success -> {
                            if (!it.value) {
                                navigator.goBack()
                            }
                            DiscountDetailsContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    DiscountDetailsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> DiscountDetailsContract.PartialState.NoChange
                                else -> DiscountDetailsContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // load merchant
            intent<DiscountDetailsContract.Intent.Load>()
                .switchMap { getActiveBusiness.execute() }
                .map {
                    business = it
                    DiscountDetailsContract.PartialState.SetMerchant(it)
                },
            intent<DiscountDetailsContract.Intent.Load>()
                .switchMap { getTxnDetails.execute(transactionId ?: "") }
                .map {
                    when (it) {
                        is Result.Progress -> DiscountDetailsContract.PartialState.NoChange
                        is Result.Success -> {
                            this.customer = it.value.customer
                            this.transaction = it.value.transaction
                            if (it.value.transaction.isCreatedByCustomer) {
                                DiscountDetailsContract.PartialState.SetDeleteStatus(DiscountDetailsContract.DeleteLayoutStatus.InActive)
                            } else {
                                DiscountDetailsContract.PartialState.SetDeleteStatus(DiscountDetailsContract.DeleteLayoutStatus.Active)
                            }
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    DiscountDetailsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> DiscountDetailsContract.PartialState.NoChange
                                else -> DiscountDetailsContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // load page
            getCollectionPublishSubject
                .switchMap { getCollection.execute(it) }
                .map {
                    when (it) {
                        is Result.Progress -> DiscountDetailsContract.PartialState.NoChange
                        is Result.Success -> {
                            DiscountDetailsContract.PartialState.SetCollection(it.value.collection)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    DiscountDetailsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> DiscountDetailsContract.PartialState.NoChange
                                else -> DiscountDetailsContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // sync tx
            intent<DiscountDetailsContract.Intent.SyncTransaction>()
                .switchMap { UseCase.wrapCompletable(scheduleSyncTransactions.get().execute("txn_screen")) }
                .map {
                    when (it) {
                        is Result.Progress -> DiscountDetailsContract.PartialState.NoChange
                        is Result.Success -> DiscountDetailsContract.PartialState.NoChange
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    DiscountDetailsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> DiscountDetailsContract.PartialState.SetNetworkError(true)
                                else -> DiscountDetailsContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // delete tx
            intent<DiscountDetailsContract.Intent.Delete>()
                .map {
                    navigator.goToDeletePage(transactionId!!)
                    DiscountDetailsContract.PartialState.NoChange
                },

            // share tx
            intent<DiscountDetailsContract.Intent.ShareOnWhatsApp>()
                .map {
                    navigator.goToWhatsappShare(customer, business, transaction)
                    DiscountDetailsContract.PartialState.NoChange
                },

            // send sms by opening app
            intent<DiscountDetailsContract.Intent.OpenSmsApp>()
                .map {
                    navigator.goToSmsApp(
                        customer.mobile ?: "",
                        smsHelper.getTransactionSmsText(customer, business, transaction)
                    )
                    DiscountDetailsContract.PartialState.NoChange
                },

            intent<DiscountDetailsContract.Intent.Note>()
                .map {
                    DiscountDetailsContract.PartialState.NoteEditorState(it.canShowNoteInput)
                },
            intent<DiscountDetailsContract.Intent.NoteSubmitClicked>()
                .flatMap { updateTransactionNote.execute(UpdateTransactionNote.Request(it.note.first, it.note.second)) }
                .map {

                    DiscountDetailsContract.PartialState.NoteEditorState(false)
                },

            intent<DiscountDetailsContract.Intent.OnImagesChanged>()
                .filter { !it.isDirtyTransaction }
                .flatMap {
                    uploadTransactionImage.execute(
                        UploadTransactionImage.Request(
                            it.imagesInfo.newAddedImages,
                            it.imagesInfo.transactionId!!,
                            business.id
                        )
                    )
                }
                .map {
                    DiscountDetailsContract.PartialState.NoChange
                },

            intent<DiscountDetailsContract.Intent.OnImagesChanged>()
                .flatMap {
                    updateTransactionImageLocally.execute(
                        UpdateTransactionImageLocally.Request(
                            it.imagesInfo.tempImages,
                            it.imagesInfo.transactionId!!
                        )
                    )
                }
                .map {

                    DiscountDetailsContract.PartialState.NoChange
                },

            intent<DiscountDetailsContract.Intent.OnImagesChanged>()
                .filter { !it.isDirtyTransaction }
                .flatMap { deleteTransactionImage.execute(DeleteTransactionImage.RequestBody(it.imagesInfo.deletedImages)) }
                .map {

                    DiscountDetailsContract.PartialState.NoChange
                },

            intent<DiscountDetailsContract.Intent.OnKnowMoreClicked>()
                .map {
                    navigator.goToKnowMoreScreen(it.id, "customer")
                    DiscountDetailsContract.PartialState.NoChange
                },

            intent<DiscountDetailsContract.Intent.WhatsApp>()
                .switchMap {
                    contactPermissionAvailable = it.contactPermissionAvailable
                    UseCase.wrapSingle(
                        getMerchantPreference.execute(PreferenceKey.WHATSAPP)
                            .firstOrError()

                    )
                }.map {

                    when (it) {
                        is Result.Progress -> DiscountDetailsContract.PartialState.NoChange
                        is Result.Success -> {

                            val isWhatsAppEnabled = it.value!!.toBoolean()
                            if (isWhatsAppEnabled && contactPermissionAvailable) {

                                PhoneBookUtils.addOkCreditNumberToContact(context, getSupportNumber.get().supportNumber)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        navigator.openWhatsApp(getSupportNumber.get().supportNumber)
                                    }
                            } else {
                                navigator.goToWhatsAppOptIn()
                            }
                            DiscountDetailsContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            navigator.openWhatsApp(getSupportNumber.get().supportNumber)
                            DiscountDetailsContract.PartialState.NoChange
                        }
                    }
                }
        )
    }

    private fun observeContextualHelpIdsOnLoad() = intent<DiscountDetailsContract.Intent.Load>().switchMap {
        wrap(getContextualHelpIds.get().execute(ScreenName.TxnDetailsScreen.value))
    }.map {
        if (it is Result.Success) {
            return@map DiscountDetailsContract.PartialState.SetContextualHelpIds(it.value)
        }
        DiscountDetailsContract.PartialState.NoChange
    }

    override fun reduce(
        currentState: DiscountDetailsContract.State,
        partialState: DiscountDetailsContract.PartialState,
    ): DiscountDetailsContract.State {
        return when (partialState) {
            is DiscountDetailsContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is DiscountDetailsContract.PartialState.SetTransactionDetails -> currentState.copy(
                isLoading = false,
                transaction = partialState.transaction,
                customer = partialState.customer
            )
            is DiscountDetailsContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is DiscountDetailsContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is DiscountDetailsContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is DiscountDetailsContract.PartialState.SmsSent -> currentState.copy(isSmsSent = true)
            is DiscountDetailsContract.PartialState.SetNetworkError -> currentState.copy(networkError = partialState.networkError)
            is DiscountDetailsContract.PartialState.SetCollection -> currentState.copy(collection = partialState.collection)
            is DiscountDetailsContract.PartialState.NoChange -> currentState
            is DiscountDetailsContract.PartialState.NoteEditorState -> currentState.copy(canOpenNoteEditor = partialState.canOpenNoteEditor)
            is DiscountDetailsContract.PartialState.SetDeleteStatus -> currentState.copy(deleteStatus = partialState.deleteStatus)
            is DiscountDetailsContract.PartialState.SetMerchant -> currentState.copy(business = partialState.business)
            is DiscountDetailsContract.PartialState.SetReferralId -> currentState.copy(referralId = partialState.referralId)
            is DiscountDetailsContract.PartialState.SetContextualHelpIds -> currentState.copy(contextualHelpIds = partialState.helpIds)
        }
    }
}
