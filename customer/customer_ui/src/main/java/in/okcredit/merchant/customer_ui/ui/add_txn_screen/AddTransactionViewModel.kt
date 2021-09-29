package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen

import `in`.okcredit.backend._offline.usecase.AddTransaction
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionContract.*
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Companion.ADD_TRANSACTION_ROBOFLOW
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Companion.AMOUNT
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Companion.CUSTOMER_ID
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Companion.TRANSACTION_TYPE
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.controller.models.AddBillModel
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.controller.models.AddBillModel.*
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase.CollectVoiceSamplesFromNotes
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase.GetCalculatorEducationVisibility
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase.GetNoteTutorialVisibility
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase.ShouldShowTransactionSecurityEducation
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase.TransactionSuccessScreenVariant
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase.UpdateCalculatorEducationVisibility
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker.Companion.SUCCESS
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.models.RoboflowState
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase.HideAddBillToolTip
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase.RoboflowCanShowAddBillTooltip
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase.RoboflowUploadReceipt
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase.SetAmountAmended
import `in`.okcredit.merchant.customer_ui.usecase.IsSupplierCreditEnabledCustomer
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState.Partial
import `in`.okcredit.shared.usecase.GetConnectionStatus
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import tech.okcredit.android.auth.IncorrectPassword
import tech.okcredit.android.auth.InvalidPassword
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.android.base.extensions.itOrBlank
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.network.ApiError
import tech.okcredit.contract.MerchantPrefSyncStatus
import java.lang.IllegalArgumentException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AddTransactionViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam(CUSTOMER_ID) val customerId: String?,
    @ViewModelParam(TRANSACTION_TYPE) val txnTypeByDagger: Int,
    @ViewModelParam(ADD_TRANSACTION_ROBOFLOW) val roboflowFeatureEnabled: Boolean,
    @ViewModelParam(AMOUNT) val amountByDagger: Long,
    private val getCustomer: Lazy<GetCustomer>,
    private val isPasswordSet: Lazy<IsPasswordSet>,
    private val addTransaction: Lazy<AddTransaction>,
    private val getMerchantPreference: Lazy<GetMerchantPreference>,
    private val showTransactionSecurityEducation: Lazy<ShouldShowTransactionSecurityEducation>,
    private val getAddNoteTutorialVisibility: Lazy<GetNoteTutorialVisibility>,
    private val tracker: Lazy<CustomerEventTracker>,
    private val merchantPrefSyncStatus: Lazy<MerchantPrefSyncStatus>,
    private val getConnectionStatus: Lazy<GetConnectionStatus>,
    private val hideAddBillToolTip: Lazy<HideAddBillToolTip>,
    private val roboflowUploadReceipt: Lazy<RoboflowUploadReceipt>,
    private val roboflowCanShowAddBillTooltip: Lazy<RoboflowCanShowAddBillTooltip>,
    private val roboflowSetAmountAmended: Lazy<SetAmountAmended>,
    private val getCalculatorEducationVisibility: Lazy<GetCalculatorEducationVisibility>,
    private val roboflowTracker: Lazy<RoboflowEventTracker>,
    private val updateCalculatorEducationVisibility: Lazy<UpdateCalculatorEducationVisibility>,
    private var collectVoiceSamplesFromNotes: Lazy<CollectVoiceSamplesFromNotes>,
    private var isSupplierCreditEnabledCustomer: Lazy<IsSupplierCreditEnabledCustomer>,
) : BaseViewModel<State, PartialState, AddTxnViewEvent>(initialState.get()) {

    private var selectedDate = Calendar.getInstance()
    private val addBillModels by lazy { mutableListOf<AddBillModel>(AddBill) }

    private var note: String? = null

    // Todo(harshit) remove mutable values from here
    private var txnType: Int? = null
    private var showNoteTutorial = false
    private var passwordEnabledByMerchant: Boolean = false
    private var passwordSetByMerchant: Boolean = false
    private var skipRoboflowPrediction: Boolean = false

    override fun handle(): Observable<out Partial<State>> {
        return Observable.mergeArray(
            observeLoadIntent(),
            fetchCustomerData(),
            loadInitialDate(),
            observePasswordEnable(),
            observePasswordSet(),
            observeBillImagesAddedIntent(),
            observeBillDateAddedIntent(),
            observeSubmitClickedIntent(),
            observeBillImageIntent(),
            observeBillDateIntent(),
            observeFourPinPasswordEnabled(),
            observeAmountAddedIntent(),
            observeAddTransaction(),
            observeNoteTutorial(),
            observeAddTransactionThroughShortcut(),
            observeAmountProvideByDagger(),
            observeCustomerProfileClickedIntent(),
            observeMerchantPreferenceSynced(),
            observeUpdatePinIntent(),
            observeFourPinPasswordIntent(),
            observeMerchantPrefStatus(),
            observeRoboflowUploadReceiptIntent(),
            canShowRoboflowAddBillTooltip(),
            observeEditAmountIntent(),
            observeEnterAmountManuallyIntent(),
            observeRoboflowSetAmountAmendedIntent(),
            observeCalculatorEducationVisibility(),
            observeCreditPostTransactionEducation(),
            observeCalculatorEducationDismissed(),
            observeUploadAudioSampleIntent(),
            observeStartSpeechRecognitionIntent(),
            observeIsVoiceSamplesCollectionFromNotesEnabled(),
            observeOptOutFromVoiceSamplesCollection(),
            observeSetAudioSample(),
            observeSupplierCreditEnabled(),
        )
    }

    private fun observeSupplierCreditEnabled() = intent<Intent.Load>().switchMap {
        wrap(isSupplierCreditEnabledCustomer.get().execute(requireNotNull(customerId)))
    }.map {
        when (it) {
            is Result.Progress -> PartialState.NoChange
            is Result.Success -> {
                PartialState.SetSupplierCreditEnabledStatus(it.value)
            }
            is Result.Failure -> {
                PartialState.NoChange
            }
        }
    }

    private fun observeLoadIntent() = intent<Intent.Load>().map {
        pushIntent(Intent.CheckMerchantPrefSync)
        pushIntent(Intent.CheckCalculatorEducation)
        pushIntent(Intent.CheckRoboflowAddBillTooltip)
        pushIntent(Intent.CheckForAddReceiptVisibility)
        pushIntent(Intent.CheckForPasswordEnable)
        pushIntent(Intent.CheckForFourDigitPin)
        pushIntent(Intent.CheckForNoteTutorial)
        pushIntent(Intent.CheckForCollectVoiceSamplesFromNotes)
        pushIntent(Intent.CheckForPasswordSet)
        PartialState.NoChange
    }

    private fun observeCalculatorEducationDismissed() = intent<Intent.CalculatorEducationDismissed>().switchMap {
        wrap(updateCalculatorEducationVisibility.get().execute())
    }.map { PartialState.NoChange }

    private fun observeCalculatorEducationVisibility() = intent<Intent.CheckCalculatorEducation>()
        .switchMap {
            getCalculatorEducationVisibility.get().execute()
        }.map { show ->
            if (show) {
                emitViewEvent(AddTxnViewEvent.ShowCalculatorEducation)
            }
            PartialState.NoChange
        }

    private fun observeEditAmountIntent() = intent<Intent.EditAmount>()
        .map {
            PartialState.SetRoboflowState(RoboflowState.EditAmount)
        }

    private fun observeEnterAmountManuallyIntent() = intent<Intent.EnterAmountManuallyCancelUploadReceipt>()
        .map {
            skipRoboflowPrediction = true
            PartialState.SetRoboflowState(RoboflowState.EnterAmountManuallyCancelUploadReceipt)
        }

    private fun canShowRoboflowAddBillTooltip() = intent<Intent.CheckRoboflowAddBillTooltip>()
        .switchMap {
            if (roboflowFeatureEnabled) {
                emitViewEvent(AddTxnViewEvent.HideKeyboardWhenRoboflowEnabled)
            }
            wrap(roboflowCanShowAddBillTooltip.get().execute())
        }
        .map {
            if (it is Result.Success && it.value) PartialState.SetRoboflowState(RoboflowState.ShowAddBillToolTip)
            else PartialState.NoChange
        }

    private fun observeMerchantPreferenceSynced() = intent<Intent.SyncMerchantPref>()
        .switchMap {
            wrap(
                merchantPrefSyncStatus.get().execute()
                    .andThen(
                        getMerchantPreference.get().execute(PreferenceKey.FOUR_DIGIT_PIN)
                    )
            )
        }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    pushIntent(Intent.CheckFourPinPasswordSet(true, it.value.toBoolean()))
                    PartialState.SetIsMerchantPrefSync(true)
                }
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            emitViewEvent(AddTxnViewEvent.ShowError(R.string.interent_error))
                            PartialState.NoChange
                        }
                        else -> {
                            emitViewEvent(
                                AddTxnViewEvent.ShowError(
                                    (it.error as ApiError).code,
                                    R.string.err_default_with_error_code
                                )
                            )
                            PartialState.NoChange
                        }
                    }
                }
            }
        }

    private fun observeMerchantPrefStatus() = intent<Intent.CheckMerchantPrefSync>()
        .switchMap { wrap(merchantPrefSyncStatus.get().checkMerchantPrefSync()) }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> PartialState.SetIsMerchantPrefSync(it.value)
                is Result.Failure -> PartialState.NoChange
            }
        }

    private fun observeCustomerProfileClickedIntent() = intent<Intent.CustomerProfileClicked>()
        .map {
            emitViewEvent(AddTxnViewEvent.GoToCustomerProfile)
            PartialState.NoChange
        }

    private fun observeAmountProvideByDagger() = intent<Intent.Load>()
        .map {
            if (amountByDagger > 0) {
                PartialState.CalculatorData(
                    amountByDagger,
                    CurrencyUtil.formatV2(amountByDagger)
                )
            } else {
                PartialState.NoChange
            }
        }

    private fun observeAddTransactionThroughShortcut() =
        intent<Intent.AddTransactionThroughShortCut>()
            .map {
                this.note = it.note
                this.txnType = it.txnType
                PartialState.CheckForPassword
            }

    private fun observeRoboflowSetAmountAmendedIntent() = intent<Intent.RoboflowSetAmountAmended>()
        .switchMap {
            wrap(
                roboflowSetAmountAmended.get().execute(
                    customerId = it.customerId,
                    newAmount = it.newAmount,
                    transactionId = it.transaction.id
                )
            )
        }
        .map {
            when (it) {
                is Result.Success -> {
                    roboflowTracker.get().trackSetAmountAmended(SUCCESS)
                    emitViewEvent(AddTxnViewEvent.AddTransactionSuccess)
                    PartialState.NoChange
                }
                else -> PartialState.NoChange
            }
        }

    private fun loadInitialDate() = intent<Intent.Load>()
        .map {
            val currentCalendar = Calendar.getInstance()
            selectedDate = currentCalendar
            PartialState.BillDateAdded(currentCalendar)
        }

    private fun observePasswordEnable() = intent<Intent.CheckForPasswordEnable>()
        .switchMap { getMerchantPreference.get().execute(PreferenceKey.PAYMENT_PASSWORD) }
        .map {
            this.passwordEnabledByMerchant = it.toBoolean()
            PartialState.NoChange
        }

    private fun observeFourPinPasswordEnabled() = intent<Intent.CheckForFourDigitPin>()
        .switchMap { wrap(getMerchantPreference.get().execute(PreferenceKey.FOUR_DIGIT_PIN)) }
        .map {
            when (it) {
                is Result.Success -> {
                    PartialState.SetIsFourDigitPin(it.value.toBoolean())
                }
                else -> PartialState.NoChange
            }
        }

    private fun observeBillImagesAddedIntent() = intent<Intent.BillImagesAdded>()
        .map {
            // to remove duplicate image if the user added multiple image,
            // this will refactor when MultipleImageActivity Refactored
            if (it.isFinalList) {
                addBillModels.clear()
                addBillModels.add(AddBill)
            }
            addBillModels.addAll(it.imageList.map { image -> PictureView(image) })

            if (roboflowFeatureEnabled) {
                hideAddBillToolTip.get().execute()
                emitViewEvent(AddTxnViewEvent.TrackAddedImagesCount(addBillModels.size - 1))
            }

            PartialState.BillImagesAdded(it.imageList)
        }

    private fun observeBillDateAddedIntent() = intent<Intent.BillDateSelected>()
        .map {
            selectedDate = it.calendar
            PartialState.BillDateAdded(it.calendar)
        }

    private fun fetchCustomerData() = intent<Intent.Load>()
        .switchMap { getCustomer.get().executeObservable(customerId ?: "") }
        .map {
            when (it) {
                is Result.Success -> PartialState.LoadInitialData(it.value)
                else -> PartialState.NoChange
            }
        }

    private fun observeRoboflowUploadReceiptIntent() = intent<Intent.RoboflowUploadReceipt>()
        .switchMap { receipt ->
            getConnectionStatus.get().executeUnwrapped()
                .map { internetAvailable ->
                    receipt.takeIf { internetAvailable }
                        ?: error("Internet Is Not Available")
                }
        }
        .switchMap { wrap(roboflowUploadReceipt.get().execute(it.image)) }
        .map {
            when (it) {
                is Result.Progress -> RoboflowState.RoboflowFetchInProgress
                is Result.Success -> RoboflowState.RoboflowFetchSuccess(
                    it.value.width, it.value.height, it.value.amountBox
                )
                is Result.Failure -> RoboflowState.RoboflowFetchFailed
            }
        }
        .onErrorReturnItem(RoboflowState.InternetNotAvailable)
        .map { PartialState.SetRoboflowState(it) }

    private fun observeNoteTutorial() = intent<Intent.CheckForNoteTutorial>()
        .switchMap { wrap(getAddNoteTutorialVisibility.get().execute()) }
        .map {
            if (it is Result.Success) showNoteTutorial = it.value
            PartialState.NoChange
        }

    private fun observePasswordSet() = intent<Intent.CheckForPasswordSet>()
        .switchMap {
            wrap(isPasswordSet.get().execute())
        }
        .map {
            when (it) {
                is Result.Success -> {
                    this.passwordSetByMerchant = it.value
                    PartialState.NoChange
                }
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            // Don't show toast for this error
                            PartialState.NoChange
                        }
                        else -> {
                            emitViewEvent(
                                AddTxnViewEvent.ShowError(
                                    R.string.err_default_with_error_code,
                                    (it.error as ApiError).code
                                )
                            )
                            PartialState.NoChange
                        }
                    }
                }
                is Result.Progress -> PartialState.NoChange
            }
        }

    private fun observeCreditPostTransactionEducation() =
        intent<Intent.ShowCreditPostTransactionEducation>()
            .switchMap {
                showTransactionSecurityEducation.get().execute()
            }.map { variant ->
                if (variant == TransactionSuccessScreenVariant.NEW_DESIGN) {
                    emitViewEvent(AddTxnViewEvent.ShowNewSuccess)
                } else {
                    emitViewEvent(AddTxnViewEvent.AddTransactionSuccess)
                }
                PartialState.NoChange
            }

    private fun observeAmountAddedIntent() = intent<Intent.CalculatorData>()
        .map {
            PartialState.CalculatorData(it.amount, it.amountCalculation)
        }

    private fun observeSubmitClickedIntent() = intent<Intent.SubmitClicked>().map {
        this.note = it.note
        if (it.note.isNotBlank()) {
            emitViewEvent(AddTxnViewEvent.TrackAddNoteCompleted)
        }
        when {
            (it.amount ?: amountByDagger) > 0 -> {
                PartialState.CheckForPassword
            }
            showNoteTutorial -> {
                emitViewEvent(AddTxnViewEvent.ShowNoteTutorial)
                PartialState.NoChange
            }
            else -> {
                emitViewEvent(AddTxnViewEvent.InvalidAmountError(R.string.please_enter_amount_greater_than_zero))
                PartialState.NoChange
            }
        }
    }

    private fun observeBillImageIntent() = intent<Intent.AddBillImageClicked>()
        .map {
            emitViewEvent(AddTxnViewEvent.SelectBillImage)
            PartialState.NoChange
        }

    private fun observeBillDateIntent() = intent<Intent.SelectBillDateClicked>()
        .map {
            emitViewEvent(AddTxnViewEvent.ShowCalendar(selectedDate))
            PartialState.NoChange
        }

    private fun observeAddTransaction() = intent<Intent.AddTransaction>()
        .switchMap {
            val bilDate = DateTime(selectedDate.timeInMillis)
            tracker.get().trackTransactionDetails(
                amount = it.amount ?: amountByDagger,
                billDate = bilDate.toString(),
                customerId = customerId ?: ""
            )

            val images = addBillModels.mapNotNull { model ->
                when (model) {
                    is RoboflowPicture -> model.image
                    is PictureView -> model.image
                    is AddBill -> null
                }
            }

            wrap(
                addTransaction.get().execute(
                    type = txnType ?: txnTypeByDagger,
                    customerId = customerId ?: "",
                    amountv2 = it.amount ?: amountByDagger,
                    capturedImageList = images,
                    note = note,
                    isOnboarding = false,
                    password = it.password,
                    isVerifyPasswordRequired = it.shouldVerifyPassword,
                    billDate = bilDate,
                    inputType = null,
                    voiceId = null
                )
            )
        }.map {
            when (it) {
                is Result.Success -> {
                    PartialState.AddTransactionSuccess(it.value)
                }
                is Result.Progress -> {
                    emitViewEvent(AddTxnViewEvent.ShowBottomSheetLoader)
                    PartialState.NoChange
                }
                is Result.Failure -> {
                    emitViewEvent(AddTxnViewEvent.DismissBottomSheetLoader)
                    when {
                        (
                            it.error is IncorrectPassword ||
                                it.error.cause is IncorrectPassword ||
                                it.error is InvalidPassword ||
                                it.error.cause is InvalidPassword
                            ) -> {
                            emitViewEvent(AddTxnViewEvent.ShowError(R.string.txn_incorrect_password))
                        }
                        isInternetIssue(it.error) -> {
                            emitViewEvent(AddTxnViewEvent.ShowError(R.string.interent_error))
                        }
                        it.error is IllegalArgumentException -> {
                            emitViewEvent(AddTxnViewEvent.InvalidAmountError(R.string.please_enter_amount_greater_than_zero))
                        }
                        else -> {
                            emitViewEvent(
                                AddTxnViewEvent.ShowError(
                                    (it.error as ApiError).code,
                                    R.string.err_default_with_error_code
                                )
                            )
                        }
                    }
                    PartialState.NoChange
                }
            }
        }

    private fun observeUpdatePinIntent() = intent<Intent.UpdatePin>()
        .map {
            emitViewEvent(AddTxnViewEvent.ShowUpdatePinDialog)
            PartialState.NoChange
        }

    private fun observeFourPinPasswordIntent() = intent<Intent.CheckFourPinPasswordSet>()
        .map {
            if (it.merchantPrefSync) {
                if (it.isFourDigitPin) {
                    emitViewEvent(AddTxnViewEvent.GoToEnterPassword)
                } else pushIntent(Intent.UpdatePin)
            } else pushIntent(Intent.SyncMerchantPref)
            PartialState.NoChange
        }

    private fun observeUploadAudioSampleIntent() = intent<Intent.UploadAudioSample>()
        .switchMap { intent ->
            wrap(
                collectVoiceSamplesFromNotes.get().scheduleUpload(
                    uri = intent.uri,
                    transcribedText = intent.transcribedText,
                    noteText = intent.noteText,
                    transactionId = intent.transactionId
                )
            )
        }
        .map { PartialState.NoChange }

    private fun observeStartSpeechRecognitionIntent() = intent<Intent.StartSpeechRecognition>()
        .map {
            val intent = collectVoiceSamplesFromNotes.get().getSpeechRecognitionIntent()
            emitViewEvent(AddTxnViewEvent.StartSpeechRecognition(intent))
            PartialState.NoChange
        }

    private fun observeIsVoiceSamplesCollectionFromNotesEnabled() =
        intent<Intent.CheckForCollectVoiceSamplesFromNotes>()
            .switchMap { collectVoiceSamplesFromNotes.get().shouldCollectVoiceSamplesFromNotes() }
            .map { PartialState.SetCanCollectVoiceSamplesFromNotes(it) }

    private fun observeOptOutFromVoiceSamplesCollection() = intent<Intent.OptOutFromVoiceSamplesCollection>()
        .switchMap {
            tracker.get().trackOptOutFromVoiceSamplesCollection()
            wrap(collectVoiceSamplesFromNotes.get().optOut())
        }
        .map { PartialState.NoChange }

    private fun observeSetAudioSample() = intent<Intent.SetAudioSample>()
        .map { PartialState.SetAudioSample(it.uri, it.transcribedText) }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.LoadInitialData -> {
                currentState.copy(
                    customer = partialState.customer,
                    txType = (txnType ?: txnTypeByDagger),
                    addBillModels = addBillModels,
                )
            }
            is PartialState.CalculatorData -> currentState.copy(
                amount = partialState.amount,
                amountCalculation = partialState.amountCalculation,
            )
            is PartialState.BillDateAdded -> currentState.copy(
                billDate = SimpleDateFormat.getDateInstance(
                    DateFormat.MEDIUM
                ).format(partialState.calendar.time)
            )
            is PartialState.CheckForPassword -> {
                if (showEnterPassword()) {
                    pushIntent(
                        Intent.CheckFourPinPasswordSet(
                            currentState.merchantPrefSync,
                            currentState.isFourDigitPin
                        )
                    )
                } else {
                    pushIntent(
                        Intent.AddTransaction(
                            amount = currentState.amount,
                            shouldVerifyPassword = false,
                            txnType = txnType ?: txnTypeByDagger,
                        )
                    )
                }
                currentState
            }
            is PartialState.BillImagesAdded -> {
                if (canRoboflowPredictedData(currentState)) {
                    pushIntent(Intent.RoboflowUploadReceipt(partialState.imageList[0]))
                }
                currentState.copy(
                    canPredictedAmount = addBillModels.size > 2 && !currentState.amountAdded(),
                    isImageAdded = addBillModels.size >= 2,
                    addBillModels = addBillModels,
                    roboflowState = RoboflowState.MultipleReceiptAreAdded
                        .takeIf { roboflowFeatureEnabled && addBillModels.size > 2 }
                )
            }

            is PartialState.SetIsMerchantPrefSync -> currentState.copy(
                merchantPrefSync = partialState.synced
            )
            is PartialState.SetIsFourDigitPin -> currentState.copy(
                isFourDigitPin = partialState.isFourDigitPin
            )
            is PartialState.SetRoboflowState -> {
                emitViewEvent(AddTxnViewEvent.TrackRoboflowState(partialState.roboflowState))
                loadStateBasedOnRoboFlowState(currentState, partialState)
            }
            is PartialState.AddTransactionSuccess -> {
                if (isAmountAmended(currentState)) {
                    pushIntent(
                        Intent.RoboflowSetAmountAmended(
                            customerId ?: "",
                            currentState.amount ?: 0L,
                            partialState.transaction
                        )
                    )
                } else {
                    pushIntent(Intent.ShowCreditPostTransactionEducation)
                }
                pushIntentForUploadVoiceSampleIfPresent(partialState.transaction, currentState)
                currentState.copy(tx = partialState.transaction)
            }
            is PartialState.SetConnectionStatus -> currentState.copy(
                isInternetAvailable = partialState.status
            )
            is PartialState.SetCanCollectVoiceSamplesFromNotes -> currentState.copy(
                canCollectVoiceSamplesFromNotes = partialState.collect
            )
            is PartialState.SetAudioSample -> currentState.copy(
                voiceSamplesFromNotesInputUri = partialState.uri,
                transcribedTextFromNotes = partialState.transcribedText
            )
            is PartialState.SetSupplierCreditEnabledStatus -> currentState.copy(
                isSupplierCreditEnabledCustomer = partialState.isSupplierCreditEnabledCustomer
            )
        }
    }

    private fun loadStateBasedOnRoboFlowState(
        currentState: State,
        partialState: PartialState.SetRoboflowState,
    ): State {
        return when {
            skipRoboflowPrediction -> {
                currentState.copy(
                    roboflowState = RoboflowState.EnterAmountManuallyCancelUploadReceipt
                )
            }
            partialState.roboflowState is RoboflowState.RoboflowFetchSuccess -> {
                // Update models to include AmountBox
                val roboflowModels = currentState.addBillModels
                    .toMutableList()
                    .apply {
                        getOrNull(1)
                            ?.takeIf { currentState.addBillModels.size == 2 }
                            ?.let { it as? PictureView }
                            ?.also { pic ->
                                set(
                                    1,
                                    RoboflowPicture(
                                        pic.image,
                                        partialState.roboflowState.width,
                                        partialState.roboflowState.height,
                                        partialState.roboflowState.amountBox
                                    )
                                )
                            }
                    }

                currentState.copy(
                    canPredictedAmount = false,
                    addBillModels = roboflowModels,
                    roboflowState = partialState.roboflowState,
                    amount = partialState.roboflowState.amountBox.amount,
                    predictedAmount = partialState.roboflowState.amountBox.amount,
                    amountCalculation = CurrencyUtil.formatV2(partialState.roboflowState.amountBox.amount)
                )
            }
            else -> {
                currentState.copy(
                    canPredictedAmount = partialState.roboflowState !is RoboflowState.RoboflowFetchFailed,
                    roboflowState = partialState.roboflowState
                )
            }
        }
    }

    private fun pushIntentForUploadVoiceSampleIfPresent(transaction: Transaction, currentState: State) {
        currentState.voiceSamplesFromNotesInputUri?.let { uri ->
            val uploadSampleIntent = Intent.UploadAudioSample(
                uri = uri,
                transcribedText = currentState.transcribedTextFromNotes.itOrBlank(),
                noteText = transaction.note.itOrBlank(),
                transactionId = transaction.id
            )
            pushIntent(uploadSampleIntent)
        }
    }

    private fun showEnterPassword(): Boolean {
        return txnTypeByDagger == Transaction.PAYMENT &&
            passwordEnabledByMerchant &&
            passwordSetByMerchant
    }

    private fun canRoboflowPredictedData(currentState: State) = roboflowFeatureEnabled &&
        addBillModels.size == 2 &&
        !currentState.amountAdded() &&
        !skipRoboflowPrediction &&
        currentState.canPredictedAmount

    private fun isAmountAmended(currentState: State) = roboflowFeatureEnabled &&
        currentState.predictedAmount != null &&
        currentState.amount != currentState.predictedAmount
}
