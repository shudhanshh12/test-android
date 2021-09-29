package `in`.okcredit.voice_first.ui.bulk_add

import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import `in`.okcredit.shared.utils.exhaustive
import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import `in`.okcredit.voice_first.databinding.ActivityBulkAddTransactionsBinding
import `in`.okcredit.voice_first.ui.bulk_add.BulkAddTransactionsContract.*
import `in`.okcredit.voice_first.ui.bulk_add.drafts_list.DraftsListController
import `in`.okcredit.voice_first.ui.bulk_add.drafts_list.EditableItemView
import `in`.okcredit.voice_first.ui.bulk_add.drafts_list.RetryableIemView
import `in`.okcredit.voice_first.ui.bulk_add.edit_draft.EditDraftTransactionActivity
import `in`.okcredit.voice_first.ui.bulk_add.voice_parse.BulkAddVoiceInputBottomSheet
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.delay
import tech.okcredit.android.base.extensions.*
import tech.okcredit.contract.AppLock
import tech.okcredit.contract.Constants
import tech.okcredit.contract.OnUpdatePinClickListener
import java.util.*
import javax.inject.Inject
import android.content.Intent as AndroidIntent

class BulkAddTransactionsActivity :
    BaseActivity<State, ViewEvent, Intent>("BulkAddTransactionActivity"),
    DatePickerDialog.OnDateSetListener,
    BulkAddVoiceInputBottomSheet.VoiceInputListener,
    EditableItemView.Listener,
    RetryableIemView.Listener,
    OnUpdatePinClickListener {

    @Inject
    lateinit var analytics: Lazy<BulkAddVoiceTracker>

    @Inject
    lateinit var appLock: Lazy<AppLock>

    lateinit var controller: DraftsListController

    private var voiceBottomSheet: BulkAddVoiceInputBottomSheet? = null
    private var saveBottomSheet: SaveConfirmationBottomSheet? = null

    private val binding by viewLifecycleScoped(ActivityBulkAddTransactionsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_OKCTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initUi()
    }

    private fun initUi() {
        controller = DraftsListController(retryActionListener = this, editActionListener = this)
        binding.epoxyBulkAdd.setController(controller)

        binding.cancel.setOnClickListener {
            analytics.get().logCancelClicked()
            pushIntent(Intent.Cancel)
        }
        binding.save.setOnClickListener {
            analytics.get().logSaveClicked()
            openSaveBottomSheet()
        }

        binding.dateTextNew.setOnClickListener { showCalendar() }
        binding.newVoiceDraft.setOnClickListener { openVoiceBottomSheet() }
    }

    override fun loadIntent() = Intent.Load

    override fun userIntents(): Observable<UserIntent> = Observable.empty()

    override fun render(state: State) {
        if (state.entries.isEmpty()) {
            binding.emptyStateContainer.visible()
            binding.epoxyBulkAdd.gone()
            binding.save.gone()
            binding.cancel.gone()
        } else {
            binding.emptyStateContainer.gone()
            binding.epoxyBulkAdd.visible()
            binding.save.visible()
            binding.cancel.visible()
        }
        if (state.selectedCalendarString.isNotNullOrBlank()) {
            binding.dateTextNew.setText(state.selectedCalendarString, TextView.BufferType.NORMAL)
        }

        controller.setData(state.entries)
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.EndActivity -> finish()
            is ViewEvent.ShowMessage -> shortToast(event.message)
            is ViewEvent.GoToEnterPassword -> goToEnterPassword()
            is ViewEvent.ShowUpdatePinDialog -> showUpdatePinDialog()
        }.exhaustive
    }

    override fun onEditDraftClicked(draftTransaction: DraftTransaction) {
        startActivity(EditDraftTransactionActivity.getIntent(this, draftTransaction.draftTransactionId))
    }

    override fun onRetryParseClicked(draftTransaction: DraftTransaction) {
        pushIntent(Intent.RequestDraftTransactionParse(draftTransaction))
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val selectedCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        lifecycleScope.launchWhenResumed { pushIntent(Intent.OnDateSet(selectedCalendar)) }
    }

    override fun cancel() {
        voiceBottomSheet?.dismiss()
        voiceBottomSheet = null
    }

    override fun voiceTranscriptReady(text: String) {
        analytics.get().logTransactionTranscriptShown(text)
        pushIntent(Intent.VoiceTranscriptReady(text))
        lifecycleScope.launchWhenResumed {
            delay(500)
            voiceBottomSheet?.dismiss()
            voiceBottomSheet = null
        }
    }

    override fun voiceError(error: Boolean) {
        pushIntent(Intent.VoiceInputState(error))
    }

    override fun onVoiceTransactionStarted() {
    }

    private fun showCalendar() {
        val selectedDate = getCurrentState().selectedCalendar
        val currentCalendar = Calendar.getInstance().apply { timeInMillis = CommonUtils.currentDateTime().millis }

        val year: Int = selectedDate?.get(Calendar.YEAR) ?: currentCalendar.get(Calendar.YEAR)
        val month: Int = selectedDate?.get(Calendar.MONTH) ?: currentCalendar.get(Calendar.MONTH)
        val dayOfMonth: Int = selectedDate?.get(Calendar.DAY_OF_MONTH) ?: currentCalendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            this,
            year,
            month,
            dayOfMonth,
        ).apply {
            datePicker.maxDate = currentCalendar.timeInMillis
            setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ok)) { di, b ->
                analytics.get().logDateSelectorOk(datePicker.dayOfMonth, datePicker.month + 1, datePicker.year)
                this.onClick(di, b)
            }
            setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { di, b ->
                analytics.get().logDateSelectorCancel(datePicker.dayOfMonth, datePicker.month + 1, datePicker.year)
                this.onClick(di, b)
            }
            analytics.get().logDateSelectorOpened(dayOfMonth, month + 1, year)
            show()
        }
    }

    private fun openVoiceBottomSheet() {
        analytics.get().logMicClicked()

        voiceBottomSheet = BulkAddVoiceInputBottomSheet.getVoiceInstance()
        supportFragmentManager.executePendingTransactions()
        if (voiceBottomSheet?.isAdded!!.not()) {
            voiceBottomSheet?.show(supportFragmentManager, BulkAddVoiceInputBottomSheet.TAG)
        }
    }

    private fun openSaveBottomSheet() {
        if (saveBottomSheet?.isVisible == true) return

        val sheet = SaveConfirmationBottomSheet()
        saveBottomSheet = sheet
        sheet.setDateString(binding.dateTextNew.text?.toString() ?: "")
        sheet.setOnConfirmationListener { pushIntent(Intent.Save(checkIfPasswordRequired = true)) }
        sheet.show(this.supportFragmentManager, SaveConfirmationBottomSheet.TAG)
    }

    private val verifyPaymentPin = registerForActivityResult(VerifyPaymentPinContract()) { isAuthenticated ->
        if (isAuthenticated) {
            // necessary, else intent is lost
            lifecycleScope.launchWhenResumed { pushIntent(Intent.Save(checkIfPasswordRequired = false)) }
        }
    }

    inner class VerifyPaymentPinContract : ActivityResultContract<String, Boolean>() {

        override fun createIntent(context: Context, deeplink: String): AndroidIntent {
            return appLock.get().appLock(deeplink, context, sourceScreen = this@BulkAddTransactionsActivity.label)
        }

        override fun parseResult(resultCode: Int, intent: AndroidIntent?): Boolean {
            if (intent == null) return false

            return intent.getBooleanExtra(Constants.IS_AUTHENTICATED, false)
        }
    }

    private fun goToEnterPassword() {
        hideSoftKeyboard()
        verifyPaymentPin.launch(getString(R.string.enterpin_screen_deeplink))
    }

    private fun showUpdatePinDialog() {
        appLock.get().showUpdatePin(supportFragmentManager, this, sourceScreen = this.label)
    }

    override fun onSetNewPinClicked(requestCode: Int) {
        verifyPaymentPin.launch(getString(R.string.changepin_screen_deeplink))
    }

    override fun onUpdateDialogDismissed() {
    }

    companion object {
        fun startIntent(context: Context): AndroidIntent {
            return AndroidIntent(context, BulkAddTransactionsActivity::class.java)
        }

        fun getIntent(context: Context) = AndroidIntent(context, BulkAddTransactionsActivity::class.java)
    }
}
