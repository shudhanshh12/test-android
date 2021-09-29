package tech.okcredit.applock.dialogs

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.applock.databinding.SetPinPromptBinding
import tech.okcredit.contract.OnSetPinClickListener

open class SetNewPin :
    ExpandedBottomSheetDialogFragment() {
    private val binding: SetPinPromptBinding by viewLifecycleScoped(SetPinPromptBinding::bind)

    private lateinit var mListener: OnSetPinClickListener
    private var requestCode: Int = -1
    private var flow: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = SetPinPromptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Analytics.track(
            SET_SECURITY_PIN_STARTED,
            EventProperties.create().with(PropertyKey.FLOW, flow)
                .with("type", "new")
        )
        initListeners()
    }

    fun init(listener: OnSetPinClickListener, requestCode: Int, sourceScreen: String) {
        mListener = listener
        this.requestCode = requestCode
        flow = sourceScreen
    }

    fun initListeners() {
        binding.cancelBtn.setOnClickListener {
            dialog?.dismiss()
            mListener.onDismissed()
            Analytics.track(
                SET_SECURITY_PIN_CANCEL,
                EventProperties.create().with(PropertyKey.FLOW, flow)
            )
        }
        binding.tvSetpin.setOnClickListener {
            dialog?.dismiss()
            mListener.onSetPinClicked(requestCode)
            Analytics.track(
                SET_PIN_CLICKED,
                EventProperties.create().with(PropertyKey.FLOW, flow)
            )
        }
    }

    companion object {
        fun newInstance(mListener: OnSetPinClickListener, requestCode: Int, sourceScreen: String): SetNewPin {
            val fragment = SetNewPin()
            fragment.init(mListener, requestCode, sourceScreen)
            return fragment
        }

        const val SET_SECURITY_PIN_STARTED = "Set Security PIN Started"
        const val SET_SECURITY_PIN_CANCEL = "Set Security PIN cancel"
        const val SET_PIN_CLICKED = "Set PIN clicked"
    }
}
