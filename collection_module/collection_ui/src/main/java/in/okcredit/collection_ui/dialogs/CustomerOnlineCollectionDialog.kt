package `in`.okcredit.collection_ui.dialogs

import `in`.okcredit.collection.contract.CustomerOnlineEducationListener
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.CustomerOnlineEducationDialogBinding
import `in`.okcredit.shared.utils.CommonUtils
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.CompoundButtonCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class CustomerOnlineCollectionDialog : BottomSheetDialogFragment() {

    private var listener: CustomerOnlineEducationListener? = null
    private var event: EventListener? = null
    private var canSetupLater: Boolean = true
    private var title: String? = null
    private var description: String? = null
    private val binding: CustomerOnlineEducationDialogBinding by viewLifecycleScoped(
        CustomerOnlineEducationDialogBinding::bind
    )

    interface EventListener {
        fun setupCollection(screen: String = SCREEN)
        fun displayed(screen: String = SCREEN)
        fun cleared(screen: String = SCREEN)
        fun clicked(focal: Boolean, screen: String = SCREEN)
    }

    companion object {
        const val TAG = "CustomerOnlineCollectionDialog"
        private const val SCREEN = "SetupCollectionDialog"
        private const val CAN_SETUP_LATER = "can_setup_later"
        private const val TITLE = "title"
        private const val DESCRIPTION = "description"

        fun newInstance(
            canSetupLater: Boolean = true,
            title: String? = null,
            description: String? = null
        ): CustomerOnlineCollectionDialog {
            val fragment = CustomerOnlineCollectionDialog()
            val bundle = Bundle()
            bundle.putBoolean(CAN_SETUP_LATER, canSetupLater)
            title?.let {
                bundle.putString(TITLE, title)
            }
            description?.let {
                bundle.putString(DESCRIPTION, description)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    fun initialise(listener: CustomerOnlineEducationListener) {
        this.listener = listener
    }

    fun setEventListener(eventListener: EventListener) {
        event = eventListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
        arguments?.let {
            canSetupLater = it.getBoolean(CAN_SETUP_LATER, true)
            title = it.getString(TITLE)
            description = it.getString(DESCRIPTION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return CustomerOnlineEducationDialogBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleOutsideClick()
        render()
        binding.root.setOnClickListener {
            event?.clicked(true)
        }
    }

    fun render() {
        binding.apply {
            setupLater()
            setTitle()
            setDescription()
            skipAndSend.setOnClickListener {
                dismiss()
                listener?.skipAndSend(dontAskAgain.isChecked)
            }
            setupNow.setOnClickListener {
                dismiss()
                event?.setupCollection()
                listener?.setupNow(dontAskAgain.isChecked)
            }

            val colorList = CommonUtils.colorStateListOf(
                intArrayOf(android.R.attr.state_checked) to requireContext().getColorFromAttr(R.attr.colorPrimary),
                intArrayOf(-android.R.attr.state_checked) to ContextCompat.getColor(requireContext(), R.color.grey800)
            )
            CompoundButtonCompat.setButtonTintList(dontAskAgain, colorList)
        }
        event?.displayed()
    }

    private fun CustomerOnlineEducationDialogBinding.setTitle() {
        title?.let {
            titleTv.text = it
        }
    }

    private fun CustomerOnlineEducationDialogBinding.setDescription() {
        description?.let {
            descriptionTv.text = it
        }
    }

    private fun CustomerOnlineEducationDialogBinding.setupLater() {
        skipAndSend.isVisible = canSetupLater
        dontAskAgain.isVisible = canSetupLater
        if (canSetupLater.not()) {
            val lp = setupNow.layoutParams
            lp.width = RelativeLayout.LayoutParams.MATCH_PARENT
            setupNow.layoutParams = lp
            setupNow.requestLayout()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        event?.cleared()
        super.onCancel(dialog)
    }

    private fun handleOutsideClick() {
        val outsideView =
            dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.touch_outside)
        outsideView?.setOnClickListener {
            event?.clicked(false)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDismiss()
    }
}
