package `in`.okcredit.user_migration.presentation.ui.file_pick.screen.views

import `in`.okcredit.user_migration.databinding.ItemFileManagerBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import java.io.File

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ItemViewFile @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ItemFileManagerBinding =
        ItemFileManagerBinding.inflate(LayoutInflater.from(context), this, true)

    private var listener: ItemViewFileListener? = null
    private var filePath: String? = null

    init {
        binding.rootView.setOnClickListener {
            listener?.onFileSelected(filePath = filePath!!)
        }
    }

    @ModelProp
    fun SetFileName(fileModel: String) {
        val filename = File(fileModel).name
        val filePath = File(fileModel).absolutePath
        binding.fileName.text = filename
        this.filePath = filePath
    }

    @ModelProp
    fun setLocalFileSelected(selected: Boolean) {
        if (selected) {
            binding.selectedIv.visible()
        } else {
            binding.selectedIv.gone()
        }
    }

    interface ItemViewFileListener {
        fun onFileSelected(filePath: String)
    }

    @CallbackProp
    fun setListener(listener: ItemViewFileListener?) {
        this.listener = listener
    }
}
