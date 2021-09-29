package `in`.okcredit.voice_first.ui.bulk_add

import `in`.okcredit.voice_first.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import `in`.okcredit.voice_first.databinding.BottomSheetSaveConfirmationBinding as BottomSheetBinding

typealias SaveConfirmationActionListener = () -> Unit

class SaveConfirmationBottomSheet : BottomSheetDialogFragment() {

    companion object {
        val TAG: String = SaveConfirmationBottomSheet::class.java.simpleName
    }

    private val binding: BottomSheetBinding by viewLifecycleScoped(BottomSheetBinding::bind)

    private lateinit var listener: SaveConfirmationActionListener
    private lateinit var dateString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetBinding.inflate(layoutInflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.save.setOnClickListener { listener.invoke() }
        binding.cancel.setOnClickListener { dismiss() }

        binding.date.text = requireContext().getString(
            R.string.t_004_bulk_voice_txn_save_bottomsheet_date_of_txn,
            dateString
        )
    }

    fun setOnConfirmationListener(listener: SaveConfirmationActionListener) {
        this.listener = listener
    }

    fun setDateString(dateString: String) {
        this.dateString = dateString
    }
}
