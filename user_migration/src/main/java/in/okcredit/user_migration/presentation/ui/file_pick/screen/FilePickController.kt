package `in`.okcredit.user_migration.presentation.ui.file_pick.screen

import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.views.ItemViewFile.ItemViewFileListener
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.views.itemViewFile
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.views.uploadFileShimmerLoader
import com.airbnb.epoxy.TypedEpoxyController
import dagger.Lazy
import javax.inject.Inject

class FilePickController @Inject constructor(
    private val ItemViewFileListener: Lazy<ItemViewFileListener>
) : TypedEpoxyController<FilePickContract.State>() {

    override fun buildModels(data: FilePickContract.State?) {
        if (data?.deviceLocalFiles.isNullOrEmpty()) {
            uploadFileShimmerLoader {
                id("loading")
            }
        } else {
            data?.deviceLocalFiles?.forEach { filePath ->
                itemViewFile {
                    id(filePath)
                    listener(ItemViewFileListener.get())
                    SetFileName(filePath)
                    data.selectedLocalFiles.filePaths.any { it == filePath }.let { localFileSelected(it) }
                }
            }
        }
    }
}
