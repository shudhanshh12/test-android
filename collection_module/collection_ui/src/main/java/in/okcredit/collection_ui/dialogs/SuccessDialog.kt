package `in`.okcredit.collection_ui.dialogs

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.DialogSuccessBinding
import `in`.okcredit.shared.utils.AutoDisposable
import `in`.okcredit.shared.utils.addTo
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Completable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.invisible
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.app_contract.LegacyNavigator
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SuccessDialog : BottomSheetDialogFragment() {

    lateinit var binding: DialogSuccessBinding

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private val autoDisposable = AutoDisposable()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetMaterialDialogStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            if (it is BottomSheetDialog) {
                val bottomSheet: FrameLayout? = it.findViewById(R.id.design_bottom_sheet)
                bottomSheet?.let {
                    val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogSuccessBinding.inflate(inflater, container, false)
        autoDisposable.bindTo(viewLifecycleOwner.lifecycle)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render()
    }

    private fun render() {
        arguments?.let {
            val msg = it.getString(MESSAGE)
            val resource = it.getInt(IMAGE)
            val customerId = it.getString(CUSTOMER_ID) ?: ""
            binding.textMessage.text = msg
            binding.img.setImageResource(resource)
            if (customerId.isNotEmpty()) {
                binding.divider.visible()
                binding.buttonLedger.visible()
                binding.buttonLedger.setOnClickListener {
                    legacyNavigator.get().goToCustomerScreen(requireContext(), customerId)
                    dismissAllowingStateLoss()
                }
            } else {
                binding.divider.invisible()
                binding.buttonLedger.gone()
            }
        }
        Completable.timer(2000, TimeUnit.MILLISECONDS)
            .subscribe {
                dismiss()
            }.addTo(autoDisposable)
    }

    companion object {
        const val TAG = "SuccessDialog"
        const val MESSAGE = "message"
        const val IMAGE = "image"
        const val CUSTOMER_ID = "customer_id"

        fun newInstance(
            msg: String,
            @DrawableRes img: Int = R.drawable.ic_check_circle,
            customerId: String? = null,
        ): SuccessDialog {
            val fragment = SuccessDialog()
            val args = Bundle()
            args.putString(MESSAGE, msg)
            args.putInt(IMAGE, img)
            if (customerId.isNotNullOrBlank()) {
                args.putString(CUSTOMER_ID, customerId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
