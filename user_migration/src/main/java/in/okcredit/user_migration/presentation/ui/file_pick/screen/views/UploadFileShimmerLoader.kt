package `in`.okcredit.user_migration.presentation.ui.file_pick.screen.views

import `in`.okcredit.user_migration.databinding.FileListShimmerLoaderBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class UploadFileShimmerLoader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: FileListShimmerLoaderBinding =
        FileListShimmerLoaderBinding.inflate(LayoutInflater.from(context), this, true)
}
