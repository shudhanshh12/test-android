package `in`.okcredit.user_migration.presentation.ui.upload_status.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class ListFilesPath(
    val list: List<String> = emptyList()
) : Serializable
