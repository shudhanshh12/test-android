package `in`.okcredit.collection_ui.dialogs

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.OnlineCollectionOptionsDialogBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class OnlineCollectionsOptionsDialog : ExpandedBottomSheetDialogFragment() {

    private var listener: CollectionsOptionsListener? = null

    private val binding: OnlineCollectionOptionsDialogBinding by viewLifecycleScoped(
        OnlineCollectionOptionsDialogBinding::bind
    )

    companion object {
        const val TAG = "OnlineCollectionsOptionsDialog"

        const val ARG_LIVE_SALES_ACTIVE = "live_sales_active"
        fun newInstance(liveSalesActive: Boolean): OnlineCollectionsOptionsDialog {
            return OnlineCollectionsOptionsDialog().apply {
                val bundle = Bundle()
                bundle.putBoolean(ARG_LIVE_SALES_ACTIVE, liveSalesActive)
                arguments = bundle
            }
        }
    }

    interface CollectionsOptionsListener {
        fun onUpdateAccount()

        fun onDelete()

        fun onShare()

        fun onSave()

        fun goToOnlineStatementActivity()

        fun onClose()
    }

    fun initialize(listener: CollectionsOptionsListener) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return OnlineCollectionOptionsDialogBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        render()
    }

    private fun render() {
        binding.apply {
            val shareClickListener = View.OnClickListener {
                dismiss()
                listener?.onShare()
            }
            ivShareQr.setOnClickListener(shareClickListener)
            tvShareQr.setOnClickListener(shareClickListener)

            val liveSalesActive = arguments?.getBoolean(ARG_LIVE_SALES_ACTIVE, false) ?: false
            tvShareQr.text = if (liveSalesActive) {
                getString(R.string.share_qr_and_link)
            } else {
                getString(R.string.share_qr)
            }
            val saveClickListener = View.OnClickListener {
                dismiss()
                listener?.onSave()
            }
            ivSaveQr.setOnClickListener(saveClickListener)
            tvSaveQr.setOnClickListener(saveClickListener)

            val updateClickListener = View.OnClickListener {
                dismiss()
                listener?.onUpdateAccount()
            }
            ivUpdateAccount.setOnClickListener(updateClickListener)
            tvUpdateAccount.setOnClickListener(updateClickListener)

            val deleteClickListener = View.OnClickListener {
                dismiss()
                listener?.onDelete()
            }
            ivDeleteAccount.setOnClickListener(deleteClickListener)
            tvDeleteAccount.setOnClickListener(deleteClickListener)

            val onlineCollectionClickListener = View.OnClickListener {
                dismiss()
                listener?.goToOnlineStatementActivity()
            }
            ivOnlineCollection.setOnClickListener(onlineCollectionClickListener)
            tvOnlineCollection.setOnClickListener(onlineCollectionClickListener)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onClose()
    }

//    override fun onCancel(dialog: DialogInterface) {
//        super.onCancel(dialog)
//        listener?.onClose()
//    }
}
