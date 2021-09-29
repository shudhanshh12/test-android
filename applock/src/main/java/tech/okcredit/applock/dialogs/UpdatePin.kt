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
import tech.okcredit.applock.databinding.UpdateToFourDigitPinPromptBinding
import tech.okcredit.contract.OnUpdatePinClickListener

class UpdatePin :
    ExpandedBottomSheetDialogFragment() {
    private val binding: UpdateToFourDigitPinPromptBinding by viewLifecycleScoped(UpdateToFourDigitPinPromptBinding::bind)
    private var flow: String = ""
    lateinit var mListener: OnUpdatePinClickListener
    var requestCode: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = UpdateToFourDigitPinPromptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Analytics.track(
            SET_SECURITY_PIN_STARTED,
            EventProperties.create().with(PropertyKey.FLOW, flow)
                .with("type", "old")
        )
        initListeners()
    }

    fun init(listener: OnUpdatePinClickListener, requestCode: Int, flow: String) {
        mListener = listener
        this.requestCode = requestCode
        this.flow = flow
    }

    fun initListeners() {
        binding.setNewPinBtn.setOnClickListener {
            dialog?.dismiss()
            mListener.onSetNewPinClicked(requestCode)
            Analytics.track(
                SET_PIN_CLICKED,
                EventProperties.create().with(PropertyKey.FLOW, flow)
                    .with("type", "old")
            )
        }
    }

    companion object {
        fun newInstance(mListener: OnUpdatePinClickListener, requestCode: Int, flow: String): UpdatePin {
            val fragment = UpdatePin()
            fragment.init(mListener, requestCode, flow)
            return fragment
        }

        const val SET_SECURITY_PIN_STARTED = "Set Security Pin Started"
        const val SET_PIN_CLICKED = "Set PIN clicked"
    }
}
