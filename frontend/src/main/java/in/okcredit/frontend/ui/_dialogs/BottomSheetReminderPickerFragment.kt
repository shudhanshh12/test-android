package `in`.okcredit.frontend.ui._dialogs

import `in`.okcredit.frontend.R
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

class BottomSheetReminderPickerFragment : BottomSheetDialogFragment() {

    private var mListener: OnBottomSheetFragmentListner? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.reminder_mode_picker_sheet, container, false)

        val sms = v.findViewById<LinearLayout>(R.id.sms)
        val whatsapp = v.findViewById<LinearLayout>(R.id.whatsapp)
        val btnCancel = v.findViewById<ImageView>(R.id.btn_cancel)

        sms.setOnClickListener { v1 ->
            run {
                Timber.d("mListener mode selected")
                mListener?.onClickSms()
                dismiss()
            }
        }
        whatsapp.setOnClickListener { v1 ->
            run {
                Timber.d("mListener mode selected")
                mListener?.onClickWhatsapp()
                dismiss()
            }
        }
        btnCancel.setOnClickListener {
            dismiss()
        }

        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBottomSheetFragmentListner) {
            mListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnBottomSheetFragmentListner {
        fun onClickSms()
        fun onClickWhatsapp()
    }

    companion object {
        fun newInstance(): BottomSheetReminderPickerFragment {
            return BottomSheetReminderPickerFragment()
        }
    }
}
