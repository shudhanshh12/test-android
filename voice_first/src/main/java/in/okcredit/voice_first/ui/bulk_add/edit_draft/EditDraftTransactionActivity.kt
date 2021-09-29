package `in`.okcredit.voice_first.ui.bulk_add.edit_draft

import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_CUSTOMER
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_SUPPLIER
import `in`.okcredit.voice_first.data.bulk_add.entities.TRANSACTION_TYPE_CREDIT
import `in`.okcredit.voice_first.data.bulk_add.entities.TRANSACTION_TYPE_PAYMENT
import `in`.okcredit.voice_first.databinding.ActivityEditDraftTransactionBinding
import `in`.okcredit.voice_first.ui._di.EditDraftTransactionActivityModule.Companion.DRAFT_TRANSACTION_KEY
import `in`.okcredit.voice_first.ui.bulk_add.edit_draft.EditDraftTransactionContract.*
import `in`.okcredit.voice_first.ui.bulk_add.edit_draft.draft_merchants_list.DraftMerchantSelectedListener
import `in`.okcredit.voice_first.ui.bulk_add.search_merchant.SearchMerchantActivityContract
import `in`.okcredit.voice_first.utils.CurrencyUtil.acceptOnlyCurrency
import `in`.okcredit.voice_first.utils.CurrencyUtil.renderAmount
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject
import kotlin.math.ceil
import android.content.Intent as AndroidIntent

class EditDraftTransactionActivity :
    BaseActivity<State, ViewEvent, Intent>("EditDraftTransactionActivity") {

    @Inject
    lateinit var analytics: Lazy<BulkAddVoiceTracker>

    private val binding by viewLifecycleScoped(ActivityEditDraftTransactionBinding::inflate)

    private val getMerchantFromAdvancedSearch = registerForActivityResult(
        SearchMerchantActivityContract()
    ) { merchant ->
        // necessary, else intent is lost
        lifecycleScope.launchWhenResumed {
            analytics.get().logSearchNameSelected(getCurrentState().draft?.draftTransactionId)
            merchant?.also { pushIntent(Intent.SelectDraftMerchant(it)) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_OKCTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initUi()
    }

    private fun initUi() {
        binding.backButton.setOnClickListener { onBackPressed() }
        binding.save.setOnClickListener { pushIntent(buildSaveIntentFromUi()) }
        binding.delete.setOnClickListener { pushIntent(Intent.Delete) }
        binding.merchant.setOnClickListener { showQuickDraftMerchantLookup() }

        binding.amount.acceptOnlyCurrency()
    }

    override fun onBackPressed() {
        handleEndState(EndState.EDIT_DISCARDED)
    }

    override fun loadIntent() = Intent.Load

    override fun userIntents(): Observable<UserIntent> = Observable.empty()

    override fun render(state: State) {
        val merchant = state.selectedDraftMerchant ?: state.draft?.draftMerchants?.getOrNull(0)
        binding.merchant.setText(merchant?.merchantName)

        merchant?.also {
            when (it.merchantType) {
                MERCHANT_TYPE_CUSTOMER -> {
                    binding.radioCredit.setText(R.string.t_004_bulk_voice_txn_edit_screen_customer_credit)
                    binding.radioPayment.setText(R.string.t_004_bulk_voice_txn_edit_screen_customer_payment)
                    binding.merchantIcon.setImageResource(R.drawable.ic_customer)
                }
                MERCHANT_TYPE_SUPPLIER -> {
                    binding.radioCredit.setText(R.string.t_004_bulk_voice_txn_edit_screen_supplier_credit)
                    binding.radioPayment.setText(R.string.t_004_bulk_voice_txn_edit_screen_supplier_payment)
                    binding.merchantIcon.setImageResource(R.drawable.ic_supplier)
                }
            }
        }
    }

    private fun prefillFromDraft(draft: DraftTransaction) = with(binding) {
        this.amount.renderAmount(draft.amount ?: 0L)
        this.note.setText(draft.note)
        this.transcript.text = draft.voiceTranscript
        when (draft.transactionType) {
            TRANSACTION_TYPE_CREDIT -> radioCredit.isChecked = true
            TRANSACTION_TYPE_PAYMENT -> radioPayment.isChecked = true
        }
    }

    private fun buildSaveIntentFromUi() = with(binding) {
        // nullable
        val amount = amount.text.toString()
            .filter { it.isDigit() || it == '.' }
            .takeIf { it.isNotNullOrBlank() }
            ?.let { ceil(it.toDouble() * 100).toLong() }
            ?: 0

        val type = if (radioCredit.isChecked) TRANSACTION_TYPE_CREDIT else TRANSACTION_TYPE_PAYMENT
        val note = note.text.toString()

        Intent.Save(type, amount, note)
    }

    private fun showQuickDraftMerchantLookup() {
        val draftMerchants = getCurrentState().draft?.draftMerchants
        val selectedMerchantId = getCurrentState().selectedDraftMerchant?.merchantId

        if (draftMerchants.isNullOrEmpty() || selectedMerchantId == null) {
            pushIntent(Intent.AdvancedSearchRequested)
            return
        }

        val sheet = DraftMerchantsBottomSheet()

        sheet.show(this.supportFragmentManager, DraftMerchantsBottomSheet.TAG)
        sheet.setData(
            selectedMerchantId,
            draftMerchants,
            object : DraftMerchantSelectedListener {
                override fun onSelected(draftMerchant: DraftMerchant) {
                    analytics.get().logTransactionSuggestedNameSelected(
                        getCurrentState().draft?.draftTransactionId,
                        draftMerchant.merchantId == selectedMerchantId
                    )
                    pushIntent(Intent.SelectDraftMerchant(draftMerchant))
                    sheet.dismiss()
                }

                override fun onExtendedSearch() {
                    analytics.get().logSearchNameOpened(
                        getCurrentState().draft?.draftTransactionId,
                        draftMerchants.size
                    )
                    pushIntent(Intent.AdvancedSearchRequested)
                    sheet.dismiss()
                }

                override fun onDismissed() {
                }
            }
        )
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.EndActivity -> handleEndState(event.state)
            is ViewEvent.OpenSearchActivity -> startSearchActivity()

            is ViewEvent.ShowError -> longToast(event.error)
            is ViewEvent.InitialDraftLoaded -> prefillFromDraft(event.draft)
        }
    }

    private fun handleEndState(state: EndState) {
        when (state) {
            EndState.SAVED -> {
                analytics.get().logTransactionUpdated(
                    getCurrentState().draft?.draftTransactionId,
                    getCurrentState().selectedDraftMerchant,
                    buildSaveIntentFromUi()
                )
                shortToast(R.string.t_004_bulk_voice_txn_edit_screen_draft_saved)
            }
            EndState.DELETED -> {
                analytics.get().logTransactionDeleted(getCurrentState().draft)
                shortToast(R.string.t_004_bulk_voice_txn_edit_screen_draft_deleted)
            }
            EndState.EDIT_DISCARDED -> {
                analytics.get().logTransactionEditClosed(
                    getCurrentState().draft?.draftTransactionId,
                    getCurrentState().selectedDraftMerchant,
                    buildSaveIntentFromUi()
                )
                shortToast(R.string.t_004_bulk_voice_txn_edit_screen_draft_edit_discarded)
            }
            EndState.LOAD_FAILED -> {
                shortToast(R.string.t_004_bulk_voice_txn_edit_screen_draft_load_failed)
            }
        }
        finish()
    }

    private fun startSearchActivity() = getMerchantFromAdvancedSearch.launch(null)

    companion object {

        fun getIntent(
            context: Context,
            draftTransactionId: String,
        ) = AndroidIntent(context, EditDraftTransactionActivity::class.java)
            .apply {
                this.putExtra(DRAFT_TRANSACTION_KEY, draftTransactionId)
            }
    }
}
