package `in`.okcredit.voice_first.ui.bulk_add

import `in`.okcredit.individual.contract.IndividualRepository
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.SyncBusinessData
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.utils.CommonUtils
import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftProcessingState
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftProcessingState.*
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_CUSTOMER
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_SUPPLIER
import `in`.okcredit.voice_first.data.bulk_add.entities.isComplete
import `in`.okcredit.voice_first.ui.bulk_add.BulkAddTransactionsContract.*
import `in`.okcredit.voice_first.ui.bulk_add.drafts_list.DraftsListItem
import `in`.okcredit.voice_first.usecase.bulk_add.ClearDraftTransactions
import `in`.okcredit.voice_first.usecase.bulk_add.CreateDraftTransaction
import `in`.okcredit.voice_first.usecase.bulk_add.GetDraftTransactions
import `in`.okcredit.voice_first.usecase.bulk_add.ParseVoiceTranscript
import `in`.okcredit.voice_first.usecase.bulk_add.SaveDraftsAsTransactions
import androidx.annotation.StringRes
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.DateTime
import tech.okcredit.android.auth.usecases.IsPasswordSet
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class BulkAddTransactionsViewModel @Inject constructor(
    initialState: Lazy<State>,
    private val analytics: Lazy<BulkAddVoiceTracker>,

    private val getDraftTransactions: Lazy<GetDraftTransactions>,
    private val createDraftTransaction: Lazy<CreateDraftTransaction>,
    private val parseVoiceTranscript: Lazy<ParseVoiceTranscript>,
    private val saveDraftsAsTransactions: Lazy<SaveDraftsAsTransactions>,
    private val clearDraftTransactions: Lazy<ClearDraftTransactions>,

    private val isPasswordSet: Lazy<IsPasswordSet>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val individualRepository: Lazy<IndividualRepository>,
    private val syncBusinessData: Lazy<SyncBusinessData>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {

    private var drafts: List<DraftTransaction> = emptyList()
    private val statuses: MutableMap<String, DraftProcessingState> = ConcurrentHashMap()

    private var passwordSetByMerchant: Boolean = false
    private var passwordEnabledByMerchant: Boolean = false

    private var merchantPrefSync: Boolean = false
    private var isFourDigitPin: Boolean = false

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            loadIsPasswordSet(),
            loadMerchantPassword(),

            loadCurrentDateTime(),
            loadDraftTransactions(),
            loadParseAllPendingDrafts(),

            handleCancel(),
            handleSave(),
            handleDateSet(),
            handleVoiceError(),
            handleVoiceTranscriptReady(),
            handlePinPasswordCheck(),

            observeMerchantPreferenceSynced(),

            triggerRequestDraftTransactionParse(),
        )
    }

    private fun loadIsPasswordSet() = intent<Intent.Load>()
        .switchMap { wrap(isPasswordSet.get().execute()) }
        .map {
            if (it is Result.Success) passwordSetByMerchant = it.value
            PartialState.NoChange
        }

    private fun loadMerchantPassword() = intent<Intent.Load>()
        .switchMapSingle {
            rxSingle {
                individualRepository.get().getPreference(PreferenceKey.PAYMENT_PASSWORD).first()
            }
        }
        .map {
            this.passwordEnabledByMerchant = it.toBoolean()
            PartialState.NoChange
        }

    private fun loadCurrentDateTime() = intent<Intent.Load>()
        .map { Calendar.getInstance().apply { timeInMillis = CommonUtils.currentDateTime().millis } }
        .map { PartialState.UpdateCalendar(it) }

    private fun loadDraftTransactions() = intent<Intent.Load>()
        .switchMap { getDraftTransactions.get().execute().asObservable() }
        .doOnNext { drafts = it }
        .map { PartialState.UpdateEntries(buildEntries()) }

    private fun loadParseAllPendingDrafts() = intent<Intent.Load>()
        .switchMapSingle { rxSingle { getDraftTransactions.get().execute().first() } }
        .map { drafts ->
            drafts
                .filter { !it.isParsed }
                .forEach { pushIntent(Intent.RequestDraftTransactionParse(it)) }
            PartialState.NoChange
        }

    private fun handleCancel() = intent<Intent.Cancel>()
        .doOnNext { analytics.get().logCancelConfirmed(drafts, statuses) }
        .switchMap { wrap(rxCompletable { clearDraftTransactions.get().execute() }) }
        .map {
            when (it) {
                is Result.Progress -> {
                    PartialState.Progress(true)
                }
                is Result.Success -> {
                    emitViewEvent(ViewEvent.ShowMessage(R.string.t_004_bulk_voice_txn_transaction_cancelled))
                    emitViewEvent(ViewEvent.EndActivity)
                    PartialState.Progress(false)
                }
                is Result.Failure -> {
                    emitViewEvent(ViewEvent.ShowMessage(parseError(it.error)))
                    PartialState.Progress(false)
                }
            }
        }

    private fun handleSave() = intent<Intent.Save>()
        .filter { intent ->
            if (!intent.checkIfPasswordRequired) return@filter true

            val isAnyDraftTypePayment = drafts.any { it.isComplete() && it.transactionType == "payment" }

            val needsPasswordCheck = isAnyDraftTypePayment && passwordEnabledByMerchant && passwordSetByMerchant
            if (needsPasswordCheck) pushIntent(Intent.CheckFourPinPasswordSet)

            return@filter !needsPasswordCheck
        }
        .switchMap {
            analytics.get().logSaveConfirmed(drafts, statuses)

            val billTime = getCurrentState().selectedCalendar?.let { DateTime(it.timeInMillis) } ?: DateTime()

            wrap(
                rxCompletable {
                    saveDraftsAsTransactions.get().execute(billTime, drafts)
                    clearDraftTransactions.get().execute()
                }
            )
        }
        .map {
            when (it) {
                is Result.Progress -> {
                    PartialState.Progress(true)
                }
                is Result.Success -> {
                    emitViewEvent(ViewEvent.ShowMessage(R.string.t_004_bulk_voice_txn_transaction_saved))
                    emitViewEvent(ViewEvent.EndActivity)
                    PartialState.Progress(false)
                }
                is Result.Failure -> {
                    emitViewEvent(ViewEvent.ShowMessage(parseError(it.error)))
                    PartialState.Progress(false)
                }
            }
        }

    private fun handleDateSet() = intent<Intent.OnDateSet>()
        .map { PartialState.UpdateCalendar(it.calendar) }

    private fun handleVoiceError() = intent<Intent.VoiceInputState>()
        .map { PartialState.NoChange }

    private fun handleVoiceTranscriptReady() = intent<Intent.VoiceTranscriptReady>()
        .switchMapSingle { rxSingle { createDraftTransaction.get().execute(it.voiceTranscript) } }
        .map {
            pushIntent(Intent.RequestDraftTransactionParse(it))
            PartialState.NoChange
        }

    private fun handlePinPasswordCheck() = intent<Intent.CheckFourPinPasswordSet>()
        .map {
            when {
                !merchantPrefSync -> pushIntent(Intent.SyncMerchantPref)
                isFourDigitPin -> emitViewEvent(ViewEvent.GoToEnterPassword)
                else -> emitViewEvent(ViewEvent.ShowUpdatePinDialog)
            }
            PartialState.NoChange
        }

    private fun observeMerchantPreferenceSynced() = intent<Intent.SyncMerchantPref>()
        .switchMap {
            wrap(
                syncBusinessData.get().execute().andThen(
                    rxSingle {
                        individualRepository.get()
                            .getPreference(PreferenceKey.FOUR_DIGIT_PIN).first()
                    }
                )
            )
        }.map {
            if (it is Result.Success) {
                merchantPrefSync = true
                isFourDigitPin = it.value.toBoolean()
                pushIntent(Intent.CheckFourPinPasswordSet)
            } else if (it is Result.Failure) {
                when {
                    isInternetIssue(it.error) -> emitViewEvent(ViewEvent.ShowMessage(R.string.interent_error))
                    else -> emitViewEvent(ViewEvent.ShowMessage(R.string.err_default))
                }
            }
            PartialState.NoChange
        }

    private val draftStatusesWriterMutex = Mutex()

    private fun triggerRequestDraftTransactionParse() = intent<Intent.RequestDraftTransactionParse>()
        .switchMap { req ->
            val draftId = req.draft.draftTransactionId

            flow<PartialState> {
                draftStatusesWriterMutex.withLock {
                    if (statuses[draftId] !in listOf(null, UNINITIALIZED, FAILED)) {
                        println(">>> Draft status is unacceptable for the parse request ${statuses[draftId]}")
                        return@flow
                    }

                    statuses[draftId] = PROCESSING
                    emit(PartialState.UpdateEntries(buildEntries()))
                }

                val currentBusinessId = getActiveBusinessId.get().execute().await()
                val parsed = parseVoiceTranscript.get().execute(currentBusinessId, req.draft)
                statuses[draftId] = when {
                    !parsed.isParsed -> FAILED
                    parsed.isComplete() -> COMPLETE
                    else -> INCOMPLETE
                }

                if (parsed.isParsed) {
                    analytics.get().logTransactionDraftAdded(parsed)
                }
                emit(PartialState.UpdateEntries(buildEntries()))
            }.asObservable()
        }

    @StringRes
    private fun parseError(throwable: Throwable): Int {
        return R.string.err_default
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.Progress -> currentState.copy(processing = partialState.shown)

            is PartialState.UpdateEntries -> currentState.copy(entries = partialState.drafts)
            is PartialState.UpdateCalendar -> currentState.copy(
                selectedCalendar = partialState.selectedCalendar,
                selectedCalendarString = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM)
                    .format(partialState.selectedCalendar.time)
            )
        }
    }

    private fun buildEntries() = if (drafts.isNotEmpty()) {
        val entries = ArrayList<DraftsListItem>(drafts.size + 1)

        val customerCount = drafts.count { it.draftMerchants?.firstOrNull()?.merchantType == MERCHANT_TYPE_CUSTOMER }
        val supplierCount = drafts.count { it.draftMerchants?.firstOrNull()?.merchantType == MERCHANT_TYPE_SUPPLIER }
        entries.add(DraftsListItem.ListSummary(customerCount, supplierCount))

        drafts.forEach {
            val status = statuses.getOrPut(it.draftTransactionId) {
                when {
                    !it.isParsed -> UNINITIALIZED
                    it.isComplete() -> COMPLETE
                    else -> INCOMPLETE
                }
            }
            entries.add(DraftsListItem.DraftItem(it, status))
        }

        entries
    } else {
        emptyList()
    }
}
