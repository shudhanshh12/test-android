package `in`.okcredit.shared.dialogs.bottomsheetloader

import `in`.okcredit.shared.R
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.databinding.BottomSheetLoaderBinding
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderContract.*
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DimensionUtil
import java.util.concurrent.TimeUnit

class BottomSheetLoaderScreen : BaseBottomSheetWithViewEvents<State, ViewEvent, Intent>(
    "BottomSheetLoaderScreen"
) {

    companion object {
        const val TAG = "BottomSheetLoaderScreen"

        // loader is shown for minimum time(millis)
        private const val LOADER_DELAY = 500L

        @JvmStatic
        fun getInstance(description: String? = null) = BottomSheetLoaderScreen().apply {
            description?.let {
                val bundle = Bundle()
                bundle.putString("description", it)
                arguments = bundle
            }
        }
    }

    interface Listener {
        fun onRetry()
        fun onCancel()
        fun onSuccess()
    }

    private var listener: Listener? = null
    private var successBeforeStart: Boolean = false
    private var failedBeforeStart: Boolean = false
    private var failureMessage: String? = null
    private val dismissSubject: PublishSubject<Unit> = PublishSubject.create()

    lateinit var binding: BottomSheetLoaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetLoaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        dialog!!.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return@setOnKeyListener true
            }
            return@setOnKeyListener true
        }
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet!!)
            behavior.peekHeight = 0
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        dismiss()
                    }
                }
            })
        }
        binding.cancel.setOnClickListener {
            listener?.onCancel()
        }
        binding.retry.setOnClickListener {
            listener?.onRetry()
        }

        val description = arguments?.getString("description")
        description?.let { binding.description.text = it }
        loadingUI()
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun load() {
        pushIntent(Intent.Load)
    }

    fun success() {
        if (isResumed)
            pushIntent(Intent.Success)
        else
            successBeforeStart = true
    }

    fun failed(msg: String? = null) {
        failureMessage = msg
        if (isResumed)
            pushIntent(Intent.Fail)
        else
            failedBeforeStart = true
    }

    private fun reset() {
        listener = null
        successBeforeStart = false
        failedBeforeStart = false
        failureMessage = null
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            dismissSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.Dismiss
                }
        )
    }

    override fun render(state: State) {
        if (state.isLoading && successBeforeStart.not() && failedBeforeStart.not()) {
            loadingUI()
        } else {
            binding.root.postDelayed(
                {
                    activity?.runOnUiThread {
                        if (((state.result != null) && state.result) || successBeforeStart) {
                            successUI()
                        } else if (((state.result != null) && state.result.not()) || failedBeforeStart) {
                            failureUI()
                        }
                    }
                },
                LOADER_DELAY
            )
        }
    }

    private fun successUI() {
        binding.description.text = ""
        binding.failureLayout.gone()
        binding.loader.clearAnimation()
        binding.loader.animation = null
        binding.loader.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_success_green))
        dismissSubject.onNext(Unit)
    }

    private fun failureUI() {
        binding.failureLayout.visible()
        binding.description.text = failureMessage ?: getString(R.string.failed_to_save)
        binding.loader.clearAnimation()
        binding.loader.animation = null
        binding.loader.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning))
    }

    private fun loadingUI() {
        binding.failureLayout.gone()
        binding.loader.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_sync))
        binding.loader.animation = getAnim()
        binding.loader.animation.start()
    }

    override fun onDestroyView() {
        binding.loader.animation = null
        super.onDestroyView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        reset()
        super.onDismiss(dialog)
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.Dismiss -> listener?.onSuccess()
        }
    }

    private fun getAnim(): Animation {
        val anim = RotateAnimation(
            0f,
            360f,
            DimensionUtil.dp2px(requireContext(), 12.0f),
            DimensionUtil.dp2px(requireContext(), 12.0f)
        )
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = Animation.INFINITE
        anim.duration = 700
        return anim
    }
}
