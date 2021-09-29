package `in`.okcredit.voice_first.ui.bulk_add.edit_draft

import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import `in`.okcredit.voice_first.ui.bulk_add.edit_draft.draft_merchants_list.DraftMerchantController
import `in`.okcredit.voice_first.ui.bulk_add.edit_draft.draft_merchants_list.DraftMerchantSelectedListener
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import `in`.okcredit.voice_first.databinding.BottomSheetQuickSelectMerchantBinding as BottomSheetBinding

class DraftMerchantsBottomSheet : BottomSheetDialogFragment() {

    private val binding: BottomSheetBinding by viewLifecycleScoped(BottomSheetBinding::bind)

    private val controller = DraftMerchantController()

    private lateinit var selectedId: String
    private lateinit var data: List<DraftMerchant>
    private lateinit var listener: DraftMerchantSelectedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetBinding.inflate(layoutInflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.merchantEpoxy.setController(controller)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener.onDismissed()
    }

    fun setData(selectedId: String, data: List<DraftMerchant>, listener: DraftMerchantSelectedListener) {
        this.selectedId = selectedId
        this.data = data
        this.listener = listener
        controller.setData(selectedId, data, listener)
    }

    companion object {
        val TAG: String = DraftMerchantsBottomSheet::class.java.simpleName
    }
}
