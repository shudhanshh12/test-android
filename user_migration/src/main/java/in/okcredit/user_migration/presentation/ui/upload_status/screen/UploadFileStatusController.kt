package `in`.okcredit.user_migration.presentation.ui.upload_status.screen

import `in`.okcredit.user_migration.presentation.ui.upload_status.views.itemUploadingStatus
import `in`.okcredit.user_migration.presentation.ui.upload_status.views.uploadFileStatusShimmerLoader
import com.airbnb.epoxy.AsyncEpoxyController
import java.util.*
import javax.inject.Inject

class UploadFileStatusController @Inject constructor(private val fragment: UploadFileStatusFragment) :
    AsyncEpoxyController() {

    private var state: UploadFileStatusContract.State? = null

    fun setState(state: UploadFileStatusContract.State?) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        if (state?.uploadStatus.isNullOrEmpty()) {
            uploadFileStatusShimmerLoader {
                id(UUID.randomUUID().toString())
            }
        } else {
            state?.uploadStatus
                ?.filter { it.cancelled.not() }
                ?.forEach {
                    itemUploadingStatus {
                        id(it.id)
                        uploadStatus(it)
                        listener(fragment)
                    }
                }
        }
    }
}
