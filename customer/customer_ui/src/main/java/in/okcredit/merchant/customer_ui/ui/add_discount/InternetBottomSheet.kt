package `in`.okcredit.merchant.customer_ui.ui.add_discount

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.InternetBottomSheetBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InternetBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: InternetBottomSheetBinding
    private lateinit var internetSheetListener: InternetSheetListener

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    fun initialise(internetSheetListener: InternetSheetListener) {
        this.internetSheetListener = internetSheetListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = InternetBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.notNow.setOnClickListener {
            dismiss()
        }
        binding.tryAgain.setOnClickListener {
            dismiss()
            internetSheetListener.onTryAgainClicked()
        }
    }

    interface InternetSheetListener {
        fun onTryAgainClicked()
    }

    companion object {
        val TAG: String? = InternetBottomSheet::class.java.simpleName

        fun netInstance(): InternetBottomSheet {
            return InternetBottomSheet()
        }
    }
}
