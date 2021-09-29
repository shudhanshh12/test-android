package `in`.okcredit.user_migration.presentation.ui.upload_status.views

import `in`.okcredit.user_migration.databinding.UploadFileStatusShimmerLoaderBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class UploadFileStatusShimmerLoader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: UploadFileStatusShimmerLoaderBinding =
        UploadFileStatusShimmerLoaderBinding.inflate(LayoutInflater.from(context), this, true)
}
