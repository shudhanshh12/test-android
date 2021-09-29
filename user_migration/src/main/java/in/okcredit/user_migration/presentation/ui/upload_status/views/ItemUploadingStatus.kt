package `in`.okcredit.user_migration.presentation.ui.upload_status.views

import `in`.okcredit.fileupload.user_migration.domain.model.UploadStatus
import `in`.okcredit.fileupload.utils.AwsHelper
import `in`.okcredit.user_migration.R
import `in`.okcredit.user_migration.databinding.ItemUploadingFileBinding
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
class ItemUploadingStatus @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ItemUploadingFileBinding =
        ItemUploadingFileBinding.inflate(LayoutInflater.from(context), this, true)

    private var uploadStatus: UploadStatus? = null
    private var listener: FileUploadStatusListener? = null

    init {
        binding.ivCancelUpload.setOnClickListener {
            listener?.cancelUpload(uploadStatus)
        }

        binding.ivRetry.setOnClickListener {
            listener?.retryUpload(uploadStatus)
        }

        binding.pbPercentage.max = 100
    }

    @ModelProp
    fun uploadStatus(uploadStatus: UploadStatus) {
        this.uploadStatus = uploadStatus
        binding.apply {
            if (uploadStatus.percentage == 0) {
                pbPercentage.isIndeterminate = true
            } else {
                pbPercentage.isIndeterminate = false
                pbPercentage.progress = uploadStatus.percentage
            }
        }
        binding.uploadPercentage.text = "${uploadStatus.percentage}%"
        setFileName(File(uploadStatus.filePath).name)
        setStatusInfo(uploadStatus)
    }

    private fun setFileName(filePath: String?) {
        val file = File(filePath)
        binding.apply {
            fileName.text = file.name
        }
    }

    interface FileUploadStatusListener {
        fun cancelUpload(uploadStatus: UploadStatus?)
        fun retryUpload(uploadStatus: UploadStatus?)
    }

    @CallbackProp
    fun setListener(listener: FileUploadStatusListener?) {
        this.listener = listener
    }

    private fun setStatusInfo(uploadStatus: UploadStatus) {
        when (uploadStatus.status) {
            AwsHelper.IN_PROGRESS -> {
                binding.statusText.text = context.getString(R.string.uploading)
                binding.ivCompleted.gone()
                binding.ivRetry.gone()
                binding.pbPercentage.setProgressDrawableTiled(context.getDrawable(R.drawable.curved_progress_bar_green))
                // binding.pbPercentage.isIndeterminate = uploadStatus.percentage == 0
            }
            AwsHelper.FAILED -> {
                binding.statusText.text = context.getString(R.string.failed_text)
                binding.ivCompleted.gone()
                binding.ivRetry.visible()
                binding.pbPercentage.setProgressDrawableTiled(context.getDrawable(R.drawable.curved_progress_bar_orange))
                binding.pbPercentage.isIndeterminate = false
            }
            AwsHelper.COMPLETED -> {
                binding.statusText.text = context.getString(R.string.completed_text)
                binding.ivCompleted.visible()
                binding.ivRetry.gone()
                binding.pbPercentage.setProgressDrawableTiled(context.getDrawable(R.drawable.curved_progress_bar_green))
                binding.pbPercentage.isIndeterminate = false
            }
            AwsHelper.NO_NETWORK -> {
                binding.statusText.text = context.getString(R.string.no_network)
                binding.ivCompleted.gone()
                binding.ivRetry.visible()
                binding.pbPercentage.setProgressDrawableTiled(context.getDrawable(R.drawable.curved_progress_bar_orange))
                binding.pbPercentage.isIndeterminate = false
            }
        }
    }
}
