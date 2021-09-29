package `in`.okcredit.merchant.customer_ui.ui.delete

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.DeleteSubscriptionBottomSheetBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import tech.okcredit.android.base.extensions.getDrawableCompact

class DeleteItemBottomSheet : ExpandedBottomSheetDialogFragment() {

    private lateinit var binding: DeleteSubscriptionBottomSheetBinding

    private var listener: DeleteConfirmListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.BottomSheetMaterialDialogStyle)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DeleteSubscriptionBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getString(ARG_TITLE) ?: getString(R.string.are_you_sure)
        val description = arguments?.getString(ARG_DESCRIPTION) ?: getString(R.string.msg_delete_confirmation)
        val iconRes = arguments?.getInt(ARG_ICON_RES, R.drawable.ic_delete_outline) ?: R.drawable.ic_delete_outline
        val primaryCtaText = arguments?.getString(ARG_PRIMARY_CTA) ?: getString(R.string.delete)

        binding.textDeleteTitle.text = title
        binding.textDeleteBody.text = description
        binding.imageDelete.setImageDrawable(getDrawableCompact(iconRes))
        binding.buttonSubmit.text = primaryCtaText
        binding.buttonSubmit.setOnClickListener {
            listener?.deleteConfirmed()
            dismiss()
        }

        binding.buttonCancel.setOnClickListener {
            listener?.deleteCancelled()
            dismiss()
        }
    }

    fun setListener(deleteConfirmListener: DeleteConfirmListener) {
        this.listener = deleteConfirmListener
    }

    companion object {
        @JvmStatic
        fun getInstance(
            title: String,
            description: String,
            @DrawableRes iconRes: Int = R.drawable.ic_delete_outline,
            primaryCtaText: String? = null
        ) = DeleteItemBottomSheet().apply {
            val bundle = Bundle()
            bundle.putString(ARG_TITLE, title)
            bundle.putString(ARG_DESCRIPTION, description)
            bundle.putInt(ARG_ICON_RES, iconRes)
            primaryCtaText?.let { bundle.putString(ARG_PRIMARY_CTA, primaryCtaText) }
            arguments = bundle
        }

        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_ICON_RES = "icon_res"
        private const val ARG_PRIMARY_CTA = "primary_cta"
    }

    interface DeleteConfirmListener {
        fun deleteCancelled()
        fun deleteConfirmed()
    }
}
