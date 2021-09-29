package `in`.okcredit.merchant.customer_ui.ui.transaction_details

import `in`.okcredit.backend._offline.usecase.GetMerchantPreferenceImpl
import `in`.okcredit.backend._offline.usecase.GetTransaction
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.backend.utils.SmsHelper
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.GetSubscription
import `in`.okcredit.merchant.customer_ui.usecase.*
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.AbFeatures
import `in`.okcredit.shared.utils.ScreenName
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import merchant.okcredit.accounting.model.Transaction
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.contract.MerchantPrefSyncStatus
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.help.utils.PhoneBookUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TransactionViewModel @Inject constructor(
    private val getActiveBusiness: GetActiveBusiness,
    private val scheduleSyncTransactions: Lazy<ScheduleSyncTransactions>,
    private val getCollection: GetCollection,
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
    private val navigator: TransactionContract.Navigator,
    private val getMerchantPreference: GetMerchantPreferenceImpl,
    private val getTransactionAmountHistory: Lazy<GetTransactionAmountHistory>,
    private val isEditTransactionAmountEnabled: Lazy<IsEditTransactionAmountEnabled>,
    private val rxSharedPreference: Lazy<DefaultPreferences>,
    private val getCustomer: GetCustomer,
    private val ab: Lazy<AbRepository>,
    private val isPasswordSet: Lazy<IsPasswordSet>,
    private val merchantPrefSyncStatus: Lazy<MerchantPrefSyncStatus>,
    private val getSubscription: Lazy<GetSubscription>,
    private val getSupportNumber: Lazy<GetSupportNumber>,
    private val getContextualHelpIds: Lazy<GetContextualHelpIds>,
    private val getCustomerSupportType: Lazy<GetCustomerSupportType>,
    private val getTransaction: Lazy<GetTransaction>,
) : BaseViewModel<TransactionContract.State, TransactionContract.PartialState, TransactionContract.ViewEvent>(
    TransactionContract.State()
) {

    private var subscription: Subscription? = null
    private lateinit var customer: Customer
    private lateinit var transaction: Transaction
    private lateinit var business: Business
    private var contactPermissionAvailable = false
    private val getCollectionPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val getTxnAmountHistoryPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val getCustomerPublishSubject: PublishSubject<String> = PublishSubject.create()

    override fun handle(): Observable<UiState.Partial<TransactionContract.State>> {
        return mergeArray(
            observeContextualHelpIdsOnLoad(),
            observeSubscriptionDetail(),
            observeSubscriptionClicked(),
            // hide network error when network becomes available
            intent<TransactionContract.Intent.Load>()
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        TransactionContract.PartialState.SetNetworkError(false)
                    } else {
                        TransactionContract.PartialState.NoChange
                    }
                },

            // handle `show alert` intent
            intent<TransactionContract.Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<TransactionContract.PartialState> { TransactionContract.PartialState.HideAlert }
                        .startWith(TransactionContract.PartialState.ShowAlert(it.message))
                },

            // load page
            intent<TransactionContract.Intent.Load>()
                .switchMap {
                    UseCase.wrapObservable(getTransaction.get().execute(transactionId ?: ""))
                }
                .map {
                    when (it) {
                        is Result.Progress -> TransactionContract.PartialState.NoChange
                        is Result.Success -> {
                            this.transaction = it.value
                            if (it.value.isOnlinePaymentTransaction) {
                                it.value.collectionId?.let { it1 -> getCollectionPublishSubject.onNext(it1) }
                            } else if (it.value.isSubscriptionTransaction) {
                                it.value.collectionId?.let { it1 ->
                                    pushIntent(TransactionContract.Intent.SubscriptionDetail(it1))
                                }
                            }
                            getCustomerPublishSubject.onNext(it.value.customerId)

                            val deleteStatus = if (it.value.isCreatedByCustomer) {
                                TransactionContract.DeleteLayoutStatus.InActive
                            } else {
                                TransactionContract.DeleteLayoutStatus.Active
                            }

                            TransactionContract.PartialState.SetTransactionDetails(
                                it.value,
                                deleteStatus
                            )
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    TransactionContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> TransactionContract.PartialState.NoChange
                                else -> TransactionContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            intent<TransactionContract.Intent.Resume>()
                .switchMap {
                    wrap(isPasswordSet.get().execute())
                }.map {
                    when (it) {
                        is Result.Progress -> TransactionContract.PartialState.NoChange
                        is Result.Success -> TransactionContract.PartialState.SetIsPasswordSet(it.value)
                        is Result.Failure -> TransactionContract.PartialState.NoChange
                    }
                },
            loadMerchantPref(),
            loadFourDigitPinSet(),
            setNewPin(),
            updatePin(),
            syncMerchantPref(),
            checkIsFourDigitPinSet(),
            // handle editTransaction
            intent<TransactionContract.Intent.EditPayment>()
                .map {
                    navigator.goToEnterPinScreen()
                    TransactionContract.PartialState.NoChange
                },

            getCustomerPublishSubject
                .switchMap { UseCase.wrapObservable(getCustomer.execute(it)) }
                .map {
                    when (it) {
                        is Result.Progress -> TransactionContract.PartialState.NoChange
                        is Result.Success -> {
                            this.customer = it.value
                            TransactionContract.PartialState.SetCustomerDetails(
                                it.value
                            )
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    TransactionContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> TransactionContract.PartialState.NoChange
                                else -> TransactionContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            intent<TransactionContract.Intent.Load>()
                .switchMap { UseCase.wrapSingle(getReferralLink.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> TransactionContract.PartialState.NoChange
                        is Result.Success -> {
                            TransactionContract.PartialState.SetReferralId(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    TransactionContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> TransactionContract.PartialState.NoChange
                                else -> TransactionContract.PartialState.NoChange
                            }
                        }
                    }
                },

            intent<TransactionContract.Intent.Load>()
                .switchMap { isTransactionPresent.execute(transactionId ?: "") }
                .map {
                    when (it) {
                        is Result.Progress -> TransactionContract.PartialState.NoChange
                        is Result.Success -> {
                            if (!it.value) {
                                navigator.goBack()
                            }
                            TransactionContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    TransactionContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> TransactionContract.PartialState.NoChange
                                else -> TransactionContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // load merchant
            intent<TransactionContract.Intent.Load>()
                .switchMap { getActiveBusiness.execute() }
                .map {
                    business = it
                    TransactionContract.PartialState.SetBusiness(it)
                },

            // load page
            getCollectionPublishSubject
                .switchMap { getCollection.execute(it) }
                .map {
                    when (it) {
                        is Result.Progress -> TransactionContract.PartialState.NoChange
                        is Result.Success -> {
                            TransactionContract.PartialState.SetCollection(it.value.collection)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    TransactionContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> TransactionContract.PartialState.NoChange
                                else -> TransactionContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // sync tx
            intent<TransactionContract.Intent.SyncTransaction>()
                .switchMap { UseCase.wrapCompletable(scheduleSyncTransactions.get().execute("txn_screen")) }
                .map {
                    when (it) {
                        is Result.Progress -> TransactionContract.PartialState.NoChange
                        is Result.Success -> TransactionContract.PartialState.NoChange
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    TransactionContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> TransactionContract.PartialState.SetNetworkError(true)
                                else -> TransactionContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // delete tx
            intent<TransactionContract.Intent.Delete>()
                .map {
                    navigator.goToDeletePage(transactionId!!)
                    TransactionContract.PartialState.NoChange
                },

            // share tx
            intent<TransactionContract.Intent.ShareOnWhatsApp>()
                .map {
                    navigator.goToWhatsappShare(customer, business, transaction)
                    TransactionContract.PartialState.NoChange
                },

            // send sms by opening app
            intent<TransactionContract.Intent.OpenSmsApp>()
                .map {
                    navigator.goToSmsApp(
                        customer.mobile ?: "",
                        smsHelper.getTransactionSmsText(customer, business, transaction)
                    )
                    TransactionContract.PartialState.NoChange
                },

            intent<TransactionContract.Intent.Note>()
                .map {
                    TransactionContract.PartialState.NoteEditorState(it.canShowNoteInput)
                },

            intent<TransactionContract.Intent.NoteSubmitClicked>()
                .flatMap { updateTransactionNote.execute(UpdateTransactionNote.Request(it.note.first, it.note.second)) }
                .map {

                    TransactionContract.PartialState.NoteEditorState(false)
                },

            intent<TransactionContract.Intent.OnImagesChanged>()
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

                    TransactionContract.PartialState.NoChange
                },

            intent<TransactionContract.Intent.OnImagesChanged>()
                .flatMap {
                    updateTransactionImageLocally.execute(
                        UpdateTransactionImageLocally.Request(
                            it.imagesInfo.tempImages,
                            it.imagesInfo.transactionId!!
                        )
                    )
                }
                .map {

                    TransactionContract.PartialState.NoChange
                },

            intent<TransactionContract.Intent.OnImagesChanged>()
                .filter { !it.isDirtyTransaction }
                .flatMap { deleteTransactionImage.execute(DeleteTransactionImage.RequestBody(it.imagesInfo.deletedImages)) }
                .map {

                    TransactionContract.PartialState.NoChange
                },

            intent<TransactionContract.Intent.OnKnowMoreClicked>()
                .map {
                    navigator.goToKnowMoreScreen(it.id, "customer")
                    TransactionContract.PartialState.NoChange
                },

            intent<TransactionContract.Intent.WhatsApp>()
                .switchMap {
                    contactPermissionAvailable = it.contactPermissionAvailable
                    UseCase.wrapSingle(
                        getMerchantPreference.execute(PreferenceKey.WHATSAPP)
                            .firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> TransactionContract.PartialState.NoChange
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
                            TransactionContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            navigator.openWhatsApp(getSupportNumber.get().supportNumber)
                            TransactionContract.PartialState.NoChange
                        }
                    }
                },

            getTransactionAmountHistoryObservable(),

            isEditTransactionAmountEnabled(),

            intent<TransactionContract.Intent.IsTxnViewExpanded>()
                .map {
                    if (it.isTxnViewExpanded) {
                        getTxnAmountHistoryPublishSubject.onNext(Unit)
                    }
                    TransactionContract.PartialState.IsTxnViewExpanded(it.isTxnViewExpanded)
                },

            editAmountEducationObservable(),

            intent<TransactionContract.Intent.RxPreferenceBoolean>()
                .switchMap { wrap(rxCompletable { rxSharedPreference.get().set(it.key, it.value, it.scope) }) }
                .map { TransactionContract.PartialState.NoChange },

            showDeleteEducationObservable(),

            isSingleListEnabled(),

            getCustomerSupportType()
        )
    }

    private fun observeContextualHelpIdsOnLoad() = intent<TransactionContract.Intent.Load>().switchMap {
        wrap(getContextualHelpIds.get().execute(ScreenName.CustomerScreen.value))
    }.map {
        if (it is Result.Success) {
            return@map TransactionContract.PartialState.SetContextualHelpIds(it.value)
        }
        TransactionContract.PartialState.NoChange
    }

    private fun observeSubscriptionDetail() = intent<TransactionContract.Intent.SubscriptionDetail>().switchMap {
        getSubscription.get().execute(subscriptionId = it.subscriptionId)
    }.map {
        when (it) {
            is Result.Success -> {
                this.subscription = it.value
                TransactionContract.PartialState.SubscriptionName(it.value.name)
            }
            else -> TransactionContract.PartialState.NoChange
        }
    }

    private fun observeSubscriptionClicked() = intent<TransactionContract.Intent.SubscriptionClicked>().map {
        subscription?.let { emitViewEvent(TransactionContract.ViewEvent.SubscriptionDetail(it)) }
        TransactionContract.PartialState.NoChange
    }

    private fun loadFourDigitPinSet(): Observable<TransactionContract.PartialState> {
        return intent<TransactionContract.Intent.Resume>()
            .switchMap { UseCase.wrapObservable(getMerchantPreference.execute(PreferenceKey.FOUR_DIGIT_PIN)) }
            .map {
                when (it) {
                    is Result.Progress -> TransactionContract.PartialState.NoChange
                    is Result.Success -> TransactionContract.PartialState.SetIsFourDigitPin(it.value.toBoolean())
                    is Result.Failure -> TransactionContract.PartialState.NoChange
                }
            }
    }

    private fun loadMerchantPref(): Observable<TransactionContract.PartialState> {
        return intent<TransactionContract.Intent.Resume>()
            .switchMap { UseCase.wrapSingle(merchantPrefSyncStatus.get().checkMerchantPrefSync()) }
            .map {
                when (it) {
                    is Result.Progress -> TransactionContract.PartialState.NoChange
                    is Result.Success -> TransactionContract.PartialState.SetIsMerchantSync(it.value)
                    is Result.Failure -> TransactionContract.PartialState.NoChange
                }
            }
    }

    private fun updatePin(): Observable<TransactionContract.PartialState>? {
        return intent<TransactionContract.Intent.UpdatePin>()
            .map {
                navigator.showUpdatePinScreen()
                TransactionContract.PartialState.NoChange
            }
    }

    private fun setNewPin(): Observable<TransactionContract.PartialState>? {
        return intent<TransactionContract.Intent.SetNewPin>()
            .map {
                navigator.goToSetPinScreen()
                TransactionContract.PartialState.NoChange
            }
    }

    private fun checkIsFourDigitPinSet(): Observable<TransactionContract.PartialState> {
        return intent<TransactionContract.Intent.CheckIsFourdigitPinSet>()
            .switchMap {
                UseCase.wrapSingle(getMerchantPreference.execute(PreferenceKey.FOUR_DIGIT_PIN).firstOrError())
            }.map {
                when (it) {
                    is Result.Progress -> TransactionContract.PartialState.NoChange
                    is Result.Success -> {
                        navigator.handleFourDigitPin(it.value.toBoolean())
                        TransactionContract.PartialState.SetIsFourDigitPin(it.value.toBoolean())
                    }
                    is Result.Failure -> {
                        if (isInternetIssue(it.error)) TransactionContract.PartialState.SetNetworkError(true)
                        else TransactionContract.PartialState.ErrorState
                    }
                }
            }
    }

    private fun syncMerchantPref(): Observable<TransactionContract.PartialState> {
        return intent<TransactionContract.Intent.SyncMerchantPref>()
            .switchMap {
                UseCase.wrapCompletable(merchantPrefSyncStatus.get().execute())
            }.map {
                when (it) {
                    is Result.Progress -> TransactionContract.PartialState.NoChange
                    is Result.Success -> {
                        navigator.syncDone()
                        TransactionContract.PartialState.SetIsMerchantSync(true)
                    }
                    is Result.Failure -> {
                        if (isInternetIssue(it.error)) TransactionContract.PartialState.SetNetworkError(true)
                        else TransactionContract.PartialState.ErrorState
                    }
                }
            }
    }

    private fun isSingleListEnabled(): Observable<TransactionContract.PartialState>? {
        return intent<TransactionContract.Intent.Load>()
            .switchMap { ab.get().isFeatureEnabled(AbFeatures.SINGLE_LIST) }
            .map {
                TransactionContract.PartialState.IsSingleListEnabled(it)
            }
    }

    private fun showDeleteEducationObservable(): Observable<TransactionContract.PartialState>? {
        return intent<TransactionContract.Intent.ShowDeleteTxnEducation>()
            .switchMap {
                UseCase.wrapSingle(
                    rxSharedPreference.get().getBoolean(RxSharedPrefValues.IS_DELETE_TXN_EDUCATION_SHOWN, Scope.Individual)
                        .asObservable().firstOrError()
                )
            }
            .flatMap {
                if (it is Result.Success && it.value.not()) {
                    navigator.showDeleteTxnConfirmationDialog()
                    rxCompletable {
                        rxSharedPreference.get().set(RxSharedPrefValues.IS_DELETE_TXN_EDUCATION_SHOWN, true, Scope.Individual)
                    }.andThen(Observable.just(TransactionContract.PartialState.NoChange))
                } else {
                    navigator.goToDeletePage(transactionId!!)
                    Observable.just(TransactionContract.PartialState.NoChange)
                }
            }
    }

    private fun editAmountEducationObservable(): Observable<TransactionContract.PartialState>? {
        return intent<TransactionContract.Intent.Load>()
            .switchMap {
                UseCase.wrapSingle(
                    rxSharedPreference.get().getBoolean(RxSharedPrefValues.IS_EDIT_AMOUNT_EDUCATION_SHOWN, Scope.Individual)
                        .asObservable().firstOrError()
                )
            }.map {
                when (it) {
                    is Result.Progress -> TransactionContract.PartialState.NoChange
                    is Result.Success -> {
                        if (it.value.not()) {
                            TransactionContract.PartialState.IsEditAmountEducationShown(it.value)
                        } else {
                            TransactionContract.PartialState.NoChange
                        }
                    }
                    is Result.Failure -> TransactionContract.PartialState.NoChange
                }
            }
    }

    private fun isEditTransactionAmountEnabled(): Observable<TransactionContract.PartialState>? {
        return intent<TransactionContract.Intent.Load>()
            .switchMap { UseCase.wrapSingle(isEditTransactionAmountEnabled.get().execute(transactionId!!)) }
            .map {
                when (it) {
                    is Result.Progress -> TransactionContract.PartialState.NoChange
                    is Result.Success -> {
                        TransactionContract.PartialState.IsEditTransactionAmountEnabled(it.value)
                    }
                    is Result.Failure -> TransactionContract.PartialState.NoChange
                }
            }
    }

    private fun getTransactionAmountHistoryObservable(): Observable<TransactionContract.PartialState>? {
        return getTxnAmountHistoryPublishSubject
            .switchMap { UseCase.wrapSingle(getTransactionAmountHistory.get().execute(transactionId!!)) }
            .map {
                when (it) {
                    is Result.Progress -> TransactionContract.PartialState.ShowViewHistoryLoader(true)
                    is Result.Success -> {
                        TransactionContract.PartialState.SetTransactionAmountHistory(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                navigator.gotoLogin()
                                TransactionContract.PartialState.ShowViewHistoryLoader(false)
                            }
                            it.error is GetTransactionAmountHistory.TransactionHistoryNotFountException -> {
                                TransactionContract.PartialState.ShowViewHistoryLoader(false)
                            }
                            isInternetIssue(it.error) -> TransactionContract.PartialState.SetNetworkError(true)
                            else -> TransactionContract.PartialState.ShowViewHistoryLoader(false)
                        }
                    }
                }
            }
    }

    private fun getCustomerSupportType(): Observable<TransactionContract.PartialState> {
        return intent<TransactionContract.Intent.Load>()
            .switchMap {
                wrap(getCustomerSupportType.get().execute())
            }
            .map {
                if (it is Result.Success) {
                    TransactionContract.PartialState.SetSupportType(it.value)
                } else
                    TransactionContract.PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: TransactionContract.State,
        partialState: TransactionContract.PartialState,
    ): TransactionContract.State {
        return when (partialState) {
            is TransactionContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is TransactionContract.PartialState.SetTransactionDetails -> currentState.copy(
                isLoading = false,
                transaction = partialState.transaction,
                deleteStatus = partialState.deleteStatus
            )
            is TransactionContract.PartialState.SetCustomerDetails -> currentState.copy(
                isLoading = false,
                customer = partialState.customer
            )
            is TransactionContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is TransactionContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is TransactionContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is TransactionContract.PartialState.SmsSent -> currentState.copy(isSmsSent = true)
            is TransactionContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                showViewHistoryLoader = false
            )
            is TransactionContract.PartialState.SetCollection -> currentState.copy(collection = partialState.collection)
            is TransactionContract.PartialState.NoChange -> currentState
            is TransactionContract.PartialState.NoteEditorState -> currentState.copy(canOpenNoteEditor = partialState.canOpenNoteEditor)
            is TransactionContract.PartialState.SetDeleteStatus -> currentState.copy(deleteStatus = partialState.deleteStatus)
            is TransactionContract.PartialState.SetBusiness -> currentState.copy(business = partialState.business)
            is TransactionContract.PartialState.SetReferralId -> currentState.copy(referralId = partialState.referralId)
            is TransactionContract.PartialState.SetTransactionAmountHistory -> currentState.copy(
                transactionAmountHistory = partialState.transactionAmountHistory,
                showViewHistoryLoader = false
            )
            is TransactionContract.PartialState.IsEditTransactionAmountEnabled -> currentState.copy(
                isEditTxnAmountEnabled = partialState.isEditTxnAmountEnabled
            )
            is TransactionContract.PartialState.IsTxnViewExpanded -> currentState.copy(
                isTxnViewExpanded = partialState.isTxnViewExpanded
            )
            is TransactionContract.PartialState.IsEditAmountEducationShown -> currentState.copy(
                isEditAmountEducationShown = partialState.isEditAmountEducationShown
            )
            is TransactionContract.PartialState.ShowViewHistoryLoader -> currentState.copy(
                showViewHistoryLoader = partialState.showViewHistoryLoader
            )
            is TransactionContract.PartialState.IsSingleListEnabled -> currentState.copy(
                isSingleListEnabled = partialState.isSingleListEnabled
            )

            is TransactionContract.PartialState.SetIsPasswordSet -> currentState.copy(
                isPasswordSet = partialState.isPasswordSet
            )
            is TransactionContract.PartialState.SetIsFourDigitPin -> currentState.copy(
                isFourDigitPin = partialState.isFourDigitPin
            )
            is TransactionContract.PartialState.SetIsMerchantSync -> currentState.copy(
                isMerchantSync = partialState.isMerchantSync
            )
            is TransactionContract.PartialState.SubscriptionName -> currentState.copy(
                showSubscription = true,
                subscriptionName = partialState.name
            )
            is TransactionContract.PartialState.SetContextualHelpIds -> currentState.copy(contextualHelpIds = partialState.helpIds)
            is TransactionContract.PartialState.SetSupportType -> currentState.copy(supportType = partialState.type)
        }
    }
}
