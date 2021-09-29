package `in`.okcredit.voice_first.ui.bulk_add.voice_parse

import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.databinding.BottomSheetBulkAddVoiceInputBinding
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieDrawable
import tech.okcredit.base.exceptions.ExceptionUtils
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission

// todo Saket: Optimize this class
class BulkAddVoiceInputBottomSheet : ExpandedBottomSheetDialogFragment() {

    private val RECORD_AUDIO_PERMISSION = 1
    var speechRecognizer: SpeechRecognizer? = null

    private var mSpeechInput: SpeechInput? = null

    private lateinit var binding: BottomSheetBulkAddVoiceInputBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetBulkAddVoiceInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RECORD_AUDIO_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onRecordAudioPermissionGranted()
                } else {
                    setDenyMode()
                }
            }
        }
    }

    private fun setDenyMode() {
        binding.processingVoiceInput.visibility = View.VISIBLE
        binding.errorVoiceInput.visibility = View.GONE
        binding.voiceInput.visibility = View.GONE
        binding.listeningTv.text = context?.getString(R.string.hello)
        binding.listeningTv.setTextColor(context?.resources!!.getColor(R.color.indigo_primary))
        binding.loaderLayout.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestRecordAudioPermission()

        binding.close.setOnClickListener {
            mSpeechInput?.stopListening()
            getListener()?.cancel()
        }

        binding.errorVoiceInput.setOnClickListener {
            reset()
            mSpeechInput?.startListening()
        }

        binding.processingVoiceInput.setOnClickListener {
            requestRecordAudioPermission()
        }
    }

    private fun reset() {
        getListener()?.voiceError(false)
        binding.inputVoice.text = ""
        binding.errorVoiceInput.visibility = View.GONE
        binding.loaderLayout.visibility = View.GONE
        binding.voiceInput.visibility = View.VISIBLE
        binding.listeningTv.text = context?.getString(R.string.listening_mic)
        binding.listeningTv.setTextColor(context?.resources!!.getColor(R.color.indigo_primary))
    }

    private fun requestRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.RECORD_AUDIO
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            setDenyMode()
            Permission.requestRecordAudioPermission(
                activity as AppCompatActivity,
                object : IPermissionListener {
                    override fun onPermissionGrantedFirstTime() {}

                    override fun onPermissionGranted() {
                        onRecordAudioPermissionGranted()
                    }

                    override fun onPermissionDenied() {}
                }
            )
        } else {
            onRecordAudioPermissionGranted()
        }
    }

    internal fun onRecordAudioPermissionGranted() {
        getListener()?.onVoiceTransactionStarted()
        setDefaultMode()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        if (mSpeechInput == null) {
            speechRecognizer?.let {
                mSpeechInput = SpeechInput(
                    it, requireActivity(),
                    object : SpeechInput.OnSpeechInputListener {
                        override fun showAlertMessage(alertMessage: String) {
                            canDisplayError(true)
                        }

                        override fun onTextResult(result: String) {
                            binding.inputVoice.text = result.quote()
                            binding.inputVoice.setTypeface(binding.inputVoice.getTypeface(), Typeface.BOLD)
                            binding.loaderLayout.visibility = View.VISIBLE
                            binding.listeningTv.text = "Processing"
                            binding.loaderLayout.playAnimation()
                            getListener()?.voiceTranscriptReady(result)
                        }

                        override fun startVoiceIconAnimation() {
                            binding.voiceAnimation.visibility = View.VISIBLE
                            binding.voiceAnimation.playAnimation()
                            binding.inputVoice.visibility = View.GONE
                        }

                        override fun stopVoiceIconAnimation() {
                            binding.voiceAnimation.visibility = View.GONE
                            binding.voiceAnimation.cancelAnimation()
                            binding.inputVoice.visibility = View.VISIBLE
                        }
                    }
                )
            }
        }

        try {
            if (mSpeechInput!!.isUserSpeaking) {
                mSpeechInput!!.stopListening()
            } else {
                mSpeechInput!!.startListening()
            }
        } catch (e: Exception) {
            if (e is GoogleVoiceTypingDisabledException)
                ExceptionUtils.logException(GoogleVoiceTypingDisabledException())
        }
    }

    private fun stopListening() {
        mSpeechInput?.stopListening()
        reset()
        mSpeechInput = null
        speechRecognizer = null
    }

    override fun onDetach() {
        stopListening()
        super.onDetach()
    }

    override fun dismiss() {
        stopListening()
        super.dismiss()
    }

    private fun setDefaultMode() {
        binding.processingVoiceInput.visibility = View.GONE
        binding.errorVoiceInput.visibility = View.GONE
        binding.voiceInput.visibility = View.VISIBLE
        binding.loaderLayout.visibility = View.GONE
        binding.voiceAnimation.repeatCount = LottieDrawable.INFINITE
        binding.listeningTv.text = context?.getString(R.string.listening_mic)
        binding.listeningTv.setTextColor(context?.resources!!.getColor(R.color.indigo_primary))
        binding.voiceAnimation.playAnimation()
    }

    private fun getListener(): VoiceInputListener? {
        return when {
            activity is VoiceInputListener -> activity as VoiceInputListener
            parentFragment is VoiceInputListener -> parentFragment as VoiceInputListener
            else -> null
        } ?: run {
            ExceptionUtils.logException(IllegalStateException("No Right Parent"))
            null
        }
    }

    fun canDisplayError(canDisplayError: Boolean) {
        if (canDisplayError) {
            binding.inputVoice.text = context?.getString(R.string.t_004_speech_try_again_error_desc)
            binding.inputVoice.setTypeface(binding.inputVoice.getTypeface(), Typeface.ITALIC)
            binding.errorVoiceInput.visibility = View.VISIBLE
            binding.voiceInput.visibility = View.GONE
            binding.loaderLayout.visibility = View.GONE
            binding.listeningTv.text = context?.getString(R.string.try_again)

            // Note: Sometimes Context is missing, and using requiresContext causes a crash
            context?.also {
                binding.listeningTv.setTextColor(ContextCompat.getColor(it, R.color.red_primary))
            }
        }
    }

    fun showInternetError() {
        binding.inputVoice.text = context?.getString(R.string.no_internet_msg)
        binding.inputVoice.setTypeface(binding.inputVoice.getTypeface(), Typeface.ITALIC)
        binding.errorVoiceInput.visibility = View.VISIBLE
        binding.voiceInput.visibility = View.GONE
        binding.loaderLayout.visibility = View.GONE
        binding.listeningTv.text = context?.getString(R.string.try_again)

        // Note: Sometimes Context is missing, and using requiresContext causes a crash
        context?.also {
            binding.listeningTv.setTextColor(ContextCompat.getColor(it, R.color.red_primary))
        }
    }

    fun LinearLayout.playAnimation() {
        val dot1 = binding.dot1
        val dot2 = binding.dot2
        val dot3 = binding.dot3
        dot1.setColorFilter(ContextCompat.getColor(context, R.color.green_primary))
        dot2.setColorFilter(ContextCompat.getColor(context, R.color.green_primary))
        dot3.setColorFilter(ContextCompat.getColor(context, R.color.green_primary))
        val one_x = ObjectAnimator.ofFloat(dot1, "scaleX", 0f, 1.5f)
        val one_y = ObjectAnimator.ofFloat(dot1, "scaleY", 0f, 1.5f)
        one_x.repeatCount = Animation.INFINITE
        one_x.repeatMode = ValueAnimator.REVERSE
        one_x.duration = 500
        one_y.repeatCount = Animation.INFINITE
        one_y.repeatMode = ValueAnimator.REVERSE
        one_y.duration = 500

        val two_x = ObjectAnimator.ofFloat(dot2, "scaleX", 0f, 1.5f)
        val two_y = ObjectAnimator.ofFloat(dot2, "scaleY", 0f, 1.5f)
        two_x.repeatCount = Animation.INFINITE
        two_x.repeatCount = Animation.INFINITE
        two_x.duration = 500
        two_x.repeatMode = ValueAnimator.REVERSE
        two_x.startDelay = 250

        two_y.repeatCount = Animation.INFINITE
        two_y.repeatCount = Animation.INFINITE
        two_y.duration = 500
        two_y.repeatMode = ValueAnimator.REVERSE
        two_y.startDelay = 250

        val three_x = ObjectAnimator.ofFloat(dot3, "scaleX", 0f, 1.5f)
        val three_y = ObjectAnimator.ofFloat(dot3, "scaleY", 0f, 1.5f)
        three_x.repeatCount = Animation.INFINITE
        three_x.duration = 500
        three_x.repeatMode = ValueAnimator.REVERSE
        three_x.startDelay = 500

        three_y.repeatCount = Animation.INFINITE
        three_y.duration = 500
        three_y.repeatMode = ValueAnimator.REVERSE
        three_y.startDelay = 500

        val ani = AnimatorSet()
        ani.playTogether(one_x, one_y, two_x, two_y, three_x, three_y)
        ani.start()
    }

    /**
     *
     */
    interface VoiceInputListener {
        fun cancel()
        fun voiceTranscriptReady(text: String)
        fun voiceError(error: Boolean)
        fun onVoiceTransactionStarted()
    }

    companion object {

        val TAG: String = BulkAddVoiceInputBottomSheet::class.java.simpleName

        var instance: BulkAddVoiceInputBottomSheet? = null
        fun getVoiceInstance(): BulkAddVoiceInputBottomSheet {

            if (instance == null)
                instance = BulkAddVoiceInputBottomSheet()
            return instance as BulkAddVoiceInputBottomSheet
        }

        fun String.quote(): CharSequence = "\" $this \""
    }
}
