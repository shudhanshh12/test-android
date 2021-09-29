package `in`.okcredit.frontend.ui.add_expense

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.add_expense.AddExpenseContract.*
import `in`.okcredit.frontend.ui.add_expense.views.ExpenseTypeController
import `in`.okcredit.frontend.ui.add_expense.views.ExpenseTypeView
import `in`.okcredit.frontend.utils.DecimalDigitsInputFilter
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.add_expense_fragment.*
import org.joda.time.DateTime
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.invisible
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.showSoftKeyboard
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.app_contract.LegacyNavigator
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddExpenseFragment :
    BaseFragment<State, ViewEvent, Intent>("AddExpenseScreen"),
    ExpenseTypeView.ExpenseTypeViewClick,
    DatePickerDialog.OnDateSetListener {

    private lateinit var linearLayout: LinearLayoutManager
    private val submitExpense: PublishSubject<AddExpenseViewModel.AddExpense> = PublishSubject.create()
    private val showDatePicker: PublishSubject<Unit> = PublishSubject.create()
    private val onChangeDate: PublishSubject<DateTime> = PublishSubject.create()
    private val filterSuggestions: PublishSubject<List<String?>> = PublishSubject.create()
    private val showFields: PublishSubject<Boolean> = PublishSubject.create()
    internal val canShowHandEducationSubject: PublishSubject<Boolean> = PublishSubject.create()

    private var isSuggestionClicked = false
    private var isExpenseTypeEdited = false
    private var player: SimpleExoPlayer? = null

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    private var controller = ExpenseTypeController(this)

    private var datePickerDialog: DatePickerDialog? = null
    internal var canShowHandEducation = false

    companion object {
        const val ADDED_EXPENSE = "added_expense"
    }

    private var handEducationTimer = object : CountDownTimer(2000, 1000) {
        override fun onFinish() {
            if (isStateInitialized() && getCurrentState().isFirstTransaction) {
                canShowHandEducation = true
                canShowHandEducationSubject.onNext(true)
            }
        }

        override fun onTick(millisUntilFinished: Long) {
        }
    }.start()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_expense_fragment, container, false)
    }

    private fun resetTimer() {
        canShowHandEducation = false
        canShowHandEducationSubject.onNext(false)
        handEducationTimer.cancel()
        handEducationTimer.start()
    }

    override fun loadIntent(): UserIntent {
        return AddExpenseContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            submitExpense.throttleLast(300, TimeUnit.MILLISECONDS)
                .map {
                    handEducationTimer.cancel()
                    Intent.SubmitExpense(it)
                },
            showDatePicker.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    resetTimer()
                    Intent.ShowDatePickerDialog
                },
            onChangeDate
                .map { Intent.OnChangeDate(it) },
            filterSuggestions.map {
                resetTimer()
                Intent.ShowSuggestions(it)
            },
            showFields.map {
                resetTimer()
                Intent.ShowSubmitCTA(it)
            },
            canShowHandEducationSubject.map {
                Intent.ShowHandEducationIntent(it)
            }
        )
    }

    override fun onBackPressed(): Boolean {
        hideSoftKeyboard()
        arguments?.let {
            if (it.getBoolean("deeplink", false)) {
                activity?.finish()
                return true
            }
        }
        return findNavController(this).popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expense_text.requestFocus()
        showSoftKeyboard(expense_text)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        submit_expense.setOnClickListener {
            if (getCurrentState().isLoading) {
                return@setOnClickListener
            }
            val expense = expense_text.text.toString()
            var expenseType = expense_type_input.text.toString()
            if (expenseType.trim().isEmpty()) {
                expenseType = "Others"
            }
            if (expense.isNotEmpty() && getCurrentState() != null) {
                val expense = AddExpenseViewModel.AddExpense(expense, expenseType, getCurrentState().date)
                submitExpense.onNext(expense)
                var method = ""
                if (isSuggestionClicked && isExpenseTypeEdited) {
                    method = "Fab & Suggestion"
                } else if (isSuggestionClicked) {
                    method = "Suggestion"
                } else {
                    method = "Fab"
                }
                tracker.trackEvents(
                    eventName = Event.ADD_EXPENSE_COMPLETED,
                    type = expenseType,
                    screen = PropertyValue.EXPENSE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add("Amount", expense.expense)
                )
                if (expenseType.isNotEmpty() && expenseType != "Others") {
                    tracker.trackEvents(
                        eventName = Event.ADD_EXPENSE_TYPE_COMPLETED,
                        screen = PropertyValue.EXPENSE_TX,
                        propertiesMap = PropertiesMap.create()
                            .add("Value", expense.expenseType)
                            .add("Method", method)
                    )
                }
            }
        }
        date.setOnClickListener {
            tracker.trackEvents(eventName = Event.SELECT_EXPENSE_DATE, screen = PropertyValue.EXPENSE_TX)
            showDatePicker.onNext(Unit)
        }

        expense_type_input.doOnTextChanged { text, _, _, _ ->
            if (isStateInitialized()) {
                val input = text?.trim() ?: ""
                var list = getCurrentState().userExpenseTypes
                if (input.isNotEmpty()) {
                    list =
                        getCurrentState().userExpenseTypes?.filter { it != null && it.toLowerCase().startsWith(input) }
                } else {
                    isSuggestionClicked = false
                    isExpenseTypeEdited = false
                }
                filterSuggestions.onNext(list ?: listOf())
            }
        }

        expense_type_input.doAfterTextChanged {
            val input = it?.trim() ?: ""
            val find = getCurrentState().userExpenseTypes?.find {
                return@find (it != null && it.equals(input.toString(), ignoreCase = true))
            }
            isExpenseTypeEdited = find.isNullOrEmpty()
        }
        expense_type_input.setOnClickListener {
            tracker.trackEvents(
                eventName = Event.ADD_EXPENSE_TYPE_STARTED,
                screen = PropertyValue.EXPENSE_TX,
                propertiesMap = PropertiesMap.create().add("Method", "Fab")
            )
        }

        expense_text.doAfterTextChanged {
            val s = if (!it.isNullOrEmpty() && !it.toString().equals(".")) it.toString().trim().toDouble() else 0.0

            if (s.compareTo(0) != 0 && isStateInitialized() && getCurrentState().hideDateTag) {
                date.visibility = View.GONE
            }
            tracker.trackEvents(
                eventName = Event.EXPENSE_AMOUNT_ENTERED,
                screen = PropertyValue.EXPENSE_TX,
                propertiesMap = PropertiesMap.create().add("Amount", it.toString())
            )
            showFields.onNext(!it.isNullOrEmpty() && s.compareTo(0) != 0)
        }
        cancel.setOnClickListener {
            tracker.trackEvents(
                eventName = Event.EXPENSE_AUDIO_TUTORIAL_CANCEL,
                screen = PropertyValue.EXPENSE_TX
            )
            cancelAudio()
        }
        expense_text.filters = arrayOf(DecimalDigitsInputFilter(7, 2))
        linearLayout = LinearLayoutManager(activity)
        linearLayout.orientation = LinearLayoutManager.HORIZONTAL
        rv_expense_type.layoutManager = linearLayout
        rv_expense_type.adapter = controller.adapter
        save_expense.visibility = View.GONE
        initializePlayer()
    }

    private fun showDatePickerDialog() {
        if (datePickerDialog == null) {
            val today = CommonUtils.currentDateTime()

            datePickerDialog = DatePickerDialog(
                requireContext(),
                this,
                today.year,
                today.monthOfYear.minus(1),
                today.dayOfMonth
            )
            if (datePickerDialog?.datePicker != null) {
                datePickerDialog?.datePicker?.maxDate = CommonUtils.currentDateTime().millis
            }

            datePickerDialog?.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ok), datePickerDialog)
            datePickerDialog?.setButton(
                DatePickerDialog.BUTTON_NEGATIVE,
                getString(R.string.cancel),
                datePickerDialog
            )
            datePickerDialog?.setOnCancelListener {
                tracker.trackEvents(
                    eventName = Event.CLEAR_CALENDAR,
                    screen = PropertyValue.EXPENSE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add("Method", "Button")
                )
                it.dismiss()
            }
        }
        datePickerDialog!!.show()
        tracker.trackEvents(eventName = Event.VIEW_CALENDAR, screen = PropertyValue.EXPENSE_TX)
    }

    fun cancelAudio() {
        player?.release()
        cancel.gone()
    }

    @SuppressLint("RestrictedApi")
    override fun render(state: AddExpenseContract.State) {
        controller.setStates(state)
        date?.postDelayed(
            {
                date?.setText(DateTimeUtils.formatDateOnly(state.date), TextView.BufferType.NORMAL)
            },
            400
        )

        if (state.showSubmitCTA) {
            cancelAudio()
            expense_type_layout.visibility = View.VISIBLE
            if (!state.hideDateTag && !state.isFirstTransaction) {
                date.visibility = View.VISIBLE
            } else {
                date.visibility = View.GONE
            }
            if (state.isFirstTransaction) {
                rv_expense_type.visibility = View.GONE
                if (canShowHandEducation && state.canShowHandEducation) {
                    save_expense.visibility = View.VISIBLE
                    AnimationUtils.upDownMotion(hand_icon)
                } else {
                    save_expense.visibility = View.GONE
                }
            } else {
                rv_expense_type.visibility = View.VISIBLE
                save_expense.visibility = View.GONE
            }
            submit_expense.isEnabled = true
        } else {
            expense_type_layout.visibility = View.GONE
            date.visibility = View.GONE
            rv_expense_type.visibility = View.GONE
            submit_expense.isEnabled = false
            if (state.isFirstTransaction && player?.playWhenReady == false) {
                player?.playWhenReady = true
                cancel.visible()
            }
        }
        if (state.networkError) {
            shortToast(R.string.home_no_internet_msg)
        }
        if (state.isLoading) {
            submit_expense.invisible()
            submit_loader.visible()
        } else {
            submit_expense.visible()
            submit_loader.gone()
        }
    }

    private fun buildMediaSource(): MediaSource {
        val defaultDataSourceFactory =
            DefaultDataSourceFactory(context, Util.getUserAgent(context, context?.packageName))
        return ExtractorMediaSource.Factory(defaultDataSourceFactory)
            .createMediaSource(Uri.parse(getString(R.string.expense_audio_url)))
    }

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(context),
                DefaultTrackSelector(),
                DefaultLoadControl()
            )
        }
        val mediaSource = buildMediaSource()
        player?.prepare(mediaSource, true, false)
        player?.seekTo(0L)
        player?.addListener(ExoListener())
    }

    private fun goBack() {
        hideSoftKeyboard()
        val navController = findNavController(this)
        navController.previousBackStackEntry?.savedStateHandle?.set(ADDED_EXPENSE, true)
        navController.popBackStack()
    }

    override fun onDestroyView() {
        player?.release()
        super.onDestroyView()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val newDate = DateTime(calendar.timeInMillis)
        val state = getCurrentState()
        if (state.date.dayOfMonth == newDate.dayOfMonth &&
            state.date.monthOfYear == newDate.monthOfYear &&
            state.date.year == newDate.year
        ) {
            tracker.trackEvents(
                eventName = Event.EXPENSE_DATE_UPDATED,
                screen = PropertyValue.EXPENSE_TX,
                propertiesMap = PropertiesMap.create()
                    .add("Value", DateTimeUtils.formatDateOnly(newDate))
                    .add("Default", true)
            )
        } else {
            tracker.trackEvents(
                eventName = Event.EXPENSE_DATE_UPDATED,
                screen = PropertyValue.EXPENSE_TX,
                propertiesMap = PropertiesMap.create()
                    .add("value", DateTimeUtils.formatDateOnly(newDate))
                    .add("Default", false)
            )
        }

        onChangeDate.onNext(newDate)
        datePickerDialog?.dismiss()
    }

    override fun onExpenseClicked(expenseType: String) {
        isSuggestionClicked = true
        val word = expense_type_input.text ?: ""
        tracker.trackEvents(
            eventName = Event.SUGGESTION_CLICK,
            screen = PropertyValue.EXPENSE_TX,
            propertiesMap = PropertiesMap.create()
                .add("Value", expenseType)
                .add("Word Entered", word)
        )
        expense_type_input.setText(expenseType, TextView.BufferType.EDITABLE)
        expense_type_input.setSelection(expenseType.length)
    }

    private inner class ExoListener : Player.EventListener {

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        }

        override fun onSeekProcessed() {
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            cancelAudio()
        }

        override fun onLoadingChanged(isLoading: Boolean) {
        }

        override fun onPositionDiscontinuity(reason: Int) {
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_ENDED)
                cancelAudio()
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.ShowDatePickerDialog -> showDatePickerDialog()
            ViewEvent.GoBack -> goBack()
        }
    }
}
