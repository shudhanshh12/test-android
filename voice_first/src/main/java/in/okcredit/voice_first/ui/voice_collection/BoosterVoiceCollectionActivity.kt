package `in`.okcredit.voice_first.ui.voice_collection

import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker
import `in`.okcredit.voice_first.databinding.ActivityBoosterVoiceCollectionBinding
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionContract.Intent.*
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionContract.RecorderState.*
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionContract.State
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionContract.ViewEvent
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionViewModel.Companion.FRC_KEY_OKPL_AUDIO_SAMPLE_MAXIMUM_DURATION_MILLIS
import `in`.okcredit.voice_first.usecase.EducationAudioPlayerForVoiceCollectionBooster
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.SystemClock
import android.widget.Chronometer
import androidx.core.content.ContextCompat
import dagger.Lazy
import dagger.android.AndroidInjection
import io.reactivex.Observable
import kotlinx.coroutines.Job
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import javax.inject.Inject

class BoosterVoiceCollectionActivity :
    BaseActivity<State, ViewEvent, BoosterVoiceCollectionContract.Intent>("BoosterVoiceCollectionActivity"),
    IPermissionListener {

    companion object {
        const val KEY_VOICE_MESSAGE_INPUT = "voice_message_input"

        fun startIntent(context: Context, voiceMessageInput: String): Intent {
            return Intent(context, BoosterVoiceCollectionActivity::class.java)
                .putExtra(KEY_VOICE_MESSAGE_INPUT, voiceMessageInput)
        }

        fun getIntent(context: Context) = Intent(context, BoosterVoiceCollectionActivity::class.java)
    }

    private val binding: ActivityBoosterVoiceCollectionBinding by viewLifecycleScoped(
        ActivityBoosterVoiceCollectionBinding::inflate
    )

    @Inject
    lateinit var boosterVoiceCollectionTracker: Lazy<BoosterVoiceCollectionTracker>

    @Inject
    lateinit var educationAudioPlayer: Lazy<EducationAudioPlayerForVoiceCollectionBooster>

    var readThisAnimator: ObjectAnimator? = null
    var recordingMicAnimator: ObjectAnimator? = null
    private var inAppNotificationJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_OKCTheme)
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initUi()
    }

    override fun onResume() {
        super.onResume()
        readThisAnimator = AnimationUtils.upDownMotion(binding.tvReadThis, 32f)
    }

    private fun initUi() {
        binding.ivCta.setOnClickListener {
            if (isStateInitialized()) {
                when (getCurrentState().recorderState) {
                    IDLE -> {
                        boosterVoiceCollectionTracker.get().micButtonClicked(IDLE.name)
                        pushIntent(StartRecording)
                    }
                    GREETING -> {
                        boosterVoiceCollectionTracker.get().micButtonClicked(GREETING.name)
                        stopGreetingEducation()
                        pushIntent(StartRecording)
                    }
                    else -> { // do nothing
                    }
                }
            }
        }
        binding.ivBack.setOnClickListener { finish() }
        binding.tvRecordingCancel.setOnClickListener {
            boosterVoiceCollectionTracker.get().cancelButtonClicked()
            pushIntent(CancelRecording)
        }
        binding.fabRecordingDone.setOnClickListener {
            val duration = binding.tvRecordingTimer.timeElapsed()
            val inputText = if (isStateInitialized()) getCurrentState().voiceMessageInput else ""
            pushIntent(StopRecording(duration.toInt(), inputText))
        }
        binding.tvRetry.setOnClickListener {
            boosterVoiceCollectionTracker.get().retryButtonClicked()
            pushIntent(RetryRecording)
        }
        binding.tvClose.setOnClickListener { closeScreenWithSuccess() }
        binding.tvRecordingTimer.setOnChronometerTickListener {
            val duration = binding.tvRecordingTimer.timeElapsed()
            val maxDurationAllowed =
                firebaseRemoteConfig.get().getLong(FRC_KEY_OKPL_AUDIO_SAMPLE_MAXIMUM_DURATION_MILLIS)
            if (duration > maxDurationAllowed) {
                val inputText = if (isStateInitialized()) getCurrentState().voiceMessageInput else ""
                pushIntent(StopRecording(-1, inputText))
            }
        }
        binding.retryVoiceBoosterText.setOnClickListener {
            pushIntent(Load)
        }
    }

    override fun onPause() {
        stopRecordingIfActive()
        readThisAnimator?.end()
        recordingMicAnimator?.end()
        inAppNotificationJob?.cancel()
        super.onPause()
    }

    private fun stopRecordingIfActive() {
        if (isStateInitialized()) {
            when (getCurrentState().recorderState) {
                RECORDING -> pushIntent(CancelRecording)
                GREETING -> {
                    stopGreetingEducation()
                    pushIntent(GreetingEducationCompleted)
                }
                else -> { // do nothing
                }
            }
        }
    }

    override fun loadIntent() = Load

    override fun userIntents(): Observable<UserIntent> = Observable.empty()

    override fun render(state: State) {
        binding.tvVoiceInput.text = state.voiceMessageInput
        if (state.isDeepLinkFlow) {
            setVoiceBoosterState(state)
            if (state.voiceBoosterLoading.not() && state.voiceBoosterError.not()) {
                setRecorderState(state.recorderState)
            }
        } else {
            setRecorderState(state.recorderState)
        }
    }

    private fun setRecorderState(recorderState: BoosterVoiceCollectionContract.RecorderState) {
        when (recorderState) {
            GREETING -> {
                binding.groupRecording.gone()
                binding.ivCta.visible()
            }
            RECORDING -> {
                binding.ivCta.gone()
                recordingMicAnimator = AnimationUtils.blink(binding.ivRecordingMic)
                binding.groupRecording.visible()
                binding.tvRecordingTimer.base = SystemClock.elapsedRealtime()
                binding.tvRecordingTimer.start()
            }
            IDLE -> {
                binding.groupRecording.gone()
                binding.ivCta.visible()
                binding.tvRetry.gone()
                binding.tvInvalidRecording.gone()
                stopRecordingTimer()
            }
            COMPLETED -> {
                binding.groupRecording.gone()
                binding.ivCta.gone()
                binding.viewBackground.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary)
                binding.tvClose.visible()
                binding.tvValidRecording.visible()
                stopRecordingTimer()
                setResult(RESULT_OK)
            }
            INVALID -> {
                binding.groupRecording.gone()
                binding.ivCta.gone()
                binding.tvRetry.visible()
                binding.tvInvalidRecording.visible()
                stopRecordingTimer()
            }
        }
    }

    private fun setVoiceBoosterState(state: State) {
        when {
            state.voiceBoosterLoading -> setVoiceBoosterTextLoading()
            state.voiceBoosterError -> setVoiceBoosterTextError()
            state.voiceBoosterError.not() && state.voiceBoosterLoading.not() -> {
                binding.voiceBoosterStateGrp.gone()
                binding.retryVoiceBoosterText.gone()
            }
        }
    }

    private fun setVoiceBoosterTextLoading() {
        binding.voiceBoosterStateImg.setImageResource(R.drawable.ic_pending)
        binding.voiceBoosterStateImg.imageTintList = null
        binding.voiceBoosterStateText.text = getString(R.string.loading)
        binding.retryVoiceBoosterText.gone()
        binding.voiceBoosterStateGrp.visible()
    }

    private fun setVoiceBoosterTextError() {
        binding.voiceBoosterStateImg.setImageResource(R.drawable.ic_cross)
        binding.voiceBoosterStateImg.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_1))
        binding.voiceBoosterStateText.text = getString(R.string.loading_failed)
        binding.retryVoiceBoosterText.visible()
        binding.voiceBoosterStateGrp.visible()
    }

    private fun stopRecordingTimer() {
        binding.tvRecordingTimer.base = SystemClock.elapsedRealtime()
        binding.tvRecordingTimer.stop()
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.TaskCompleted -> closeScreenWithSuccess()
            is ViewEvent.RequestAudioPermission -> requestAudioPermission()
            is ViewEvent.PlayGreetingEducation -> playGreetingEducation()
        }
    }

    private fun playGreetingEducation() {
        educationAudioPlayer.get().playGreetingEducation {
            pushIntent(GreetingEducationCompleted)
        }
    }

    private fun stopGreetingEducation() = educationAudioPlayer.get().stopPlayGreetingEducation()

    private fun closeScreenWithSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    private fun requestAudioPermission() {
        boosterVoiceCollectionTracker.get().recordAudioPermissionRequested()
        Permission.requestRecordAudioPermission(this, this)
    }

    override fun onPermissionGrantedFirstTime() {
        pushIntentWithDelay(StartRecording)
    }

    override fun onPermissionGranted() {
        boosterVoiceCollectionTracker.get().recordAudioPermissionGranted()
        pushIntentWithDelay(StartRecording)
    }

    override fun onPermissionDenied() {
        boosterVoiceCollectionTracker.get().recordAudioPermissionDenied()
        longToast(R.string.permission_needed)
    }

    private fun Chronometer.timeElapsed(): Long = SystemClock.elapsedRealtime() - base
}
