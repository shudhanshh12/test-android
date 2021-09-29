package `in`.okcredit.user_migration.presentation.ui.display_parsed_data.views

import `in`.okcredit.user_migration.databinding.ItemViewFileContentBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ItemViewFileName @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ItemViewFileContentBinding =
        ItemViewFileContentBinding.inflate(LayoutInflater.from(context), this, true)

    private var listener: ItemViewFileListener? = null
    private var fileName: String = ""

    init {
        binding.tvOpenPdf.setOnClickListener {
            listener?.onPdfLinkClicked(fileName)
        }
    }

    @ModelProp
    fun setFileName(fileName: String) {
        binding.tvFileName.text = fileName
        this.fileName = fileName
    }

    interface ItemViewFileListener {
        fun onPdfLinkClicked(fileName: String)
    }

    @CallbackProp
    fun setListener(listener: ItemViewFileListener?) {
        this.listener = listener
    }
}
