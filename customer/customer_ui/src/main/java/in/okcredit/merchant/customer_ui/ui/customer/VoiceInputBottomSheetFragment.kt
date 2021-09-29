package `in`.okcredit.merchant.customer_ui.ui.customer

import `in`.okcredit.backend.utils.StringUtils
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.utils.GoogleVoiceTypingDisabledException
import `in`.okcredit.merchant.customer_ui.utils.SpeechInput
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
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
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieDrawable
import kotlinx.android.synthetic.main.bottom_sheet_fragment_voice_input.*
import kotlinx.android.synthetic.main.bottom_sheet_fragment_voice_input.view.*
import tech.okcredit.base.exceptions.ExceptionUtils
import java.lang.IllegalStateException
import java.lang.NullPointerException

class VoiceInputBottomSheetFragment : ExpandedBottomSheetDialogFragment() {

    private val RECORD_AUDIO_PERMISSION = 1
    var speechRecognizer: SpeechRecognizer? = null

    private var mSpeechInput: SpeechInput? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_fragment_voice_input, container, false)
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
        processing_voice_input?.visibility = View.VISIBLE
        error_voice_input?.visibility = View.GONE
        voice_input?.visibility = View.GONE
        listening_tv?.text = context?.getString(R.string.hello)
        listening_tv?.setTextColor(context?.resources!!.getColor(R.color.indigo_primary))
        loader_layout?.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestRecordAudioPermission()

        close.setOnClickListener {
            mSpeechInput?.stopListening()
            provideParentFragment()?.cancel()
        }

        error_voice_input.setOnClickListener {
            reset()
            mSpeechInput?.startListening()
        }

        processing_voice_input.setOnClickListener {
            requestRecordAudioPermission()
        }
    }

    private fun reset() {
        provideParentFragment()?.canShowVoiceError(false)
        input_voice?.text = ""
        error_voice_input?.visibility = View.GONE
        loader_layout?.visibility = View.GONE
        voice_input?.visibility = View.VISIBLE
        listening_tv?.text = context?.getString(R.string.listening_mic)
        listening_tv?.setTextColor(context?.resources!!.getColor(R.color.indigo_primary))
    }

    private fun requestRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.RECORD_AUDIO
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            setDenyMode()
            requestPermissions(
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION
            )
        } else {
            onRecordAudioPermissionGranted()
        }
    }

    private fun onRecordAudioPermissionGranted() {
        provideParentFragment()?.onVoiceTransactionStarted()
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
                            input_voice?.text = StringUtils.quote(result)
                            input_voice?.setTypeface(input_voice?.getTypeface(), Typeface.BOLD)
                            loader_layout?.visibility = View.VISIBLE
                            listening_tv?.text = "Processing"
                            loader_layout?.playAnimation()
                            provideParentFragment()?.sendVoiceInputText(result)
                        }

                        override fun startVoiceIconAnimation() {
                            voice_animation?.visibility = View.VISIBLE
                            voice_animation?.playAnimation()
                            input_voice?.visibility = View.GONE
                        }

                        override fun stopVoiceIconAnimation() {
                            voice_animation?.visibility = View.GONE
                            voice_animation?.cancelAnimation()
                            input_voice?.visibility = View.VISIBLE
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
        processing_voice_input?.visibility = View.GONE
        error_voice_input?.visibility = View.GONE
        voice_input?.visibility = View.VISIBLE
        loader_layout?.visibility = View.GONE
        voice_animation?.repeatCount = LottieDrawable.INFINITE
        listening_tv?.text = context?.getString(R.string.listening_mic)
        listening_tv?.setTextColor(context?.resources!!.getColor(R.color.indigo_primary))
        voice_animation?.playAnimation()
    }

    fun provideParentFragment(): VoiceInputListener? {
        when (parentFragment) {
            is VoiceInputListener -> {
                return parentFragment as VoiceInputListener
            }
            null -> {
                ExceptionUtils.logException(NullPointerException("parentFragment is null"))
            }
            !is VoiceInputListener -> {
                ExceptionUtils.logException(IllegalStateException("No Right Parent"))
            }
        }

        return null
    }

    fun canDisplayError(canDisplayError: Boolean) {
        if (canDisplayError) {
            input_voice?.text =
                context?.getString(R.string.sorry_the_process_was_interrupted_click_below_to_start_again)
            input_voice?.setTypeface(input_voice?.getTypeface(), Typeface.ITALIC)
            error_voice_input?.visibility = View.VISIBLE
            voice_input?.visibility = View.GONE
            loader_layout?.visibility = View.GONE
            listening_tv?.text = context?.getString(R.string.try_again)
            listening_tv?.setTextColor(context?.resources!!.getColor(R.color.red_primary))
        }
    }

    fun showInternetError() {
        input_voice?.text = context?.getString(R.string.no_internet_msg)
        input_voice?.setTypeface(input_voice?.getTypeface(), Typeface.ITALIC)
        error_voice_input?.visibility = View.VISIBLE
        voice_input?.visibility = View.GONE
        loader_layout?.visibility = View.GONE
        listening_tv?.text = context?.getString(R.string.try_again)
        listening_tv?.setTextColor(context?.resources!!.getColor(R.color.red_primary))
    }

    fun LinearLayout.playAnimation() {
        val dot1 = this.dot1
        val dot2 = this.dot2
        val dot3 = this.dot3
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
        fun sendVoiceInputText(text: String)
        fun canShowVoiceError(canShowVoiceError: Boolean)
        fun onVoiceTransactionStarted()
    }

    companion object {

        val TAG: String? = VoiceInputBottomSheetFragment::class.java.simpleName

        var instance: VoiceInputBottomSheetFragment? = null
        fun getVoiceInstance(): VoiceInputBottomSheetFragment {

            if (instance == null)
                instance = VoiceInputBottomSheetFragment()
            return instance as VoiceInputBottomSheetFragment
        }
    }
}
