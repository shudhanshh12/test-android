package `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models

import androidx.annotation.Keep
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils
import java.io.Serializable

@Keep
data class CustomerUiTemplate(
    val index: Int = 0,
    val isCheckedBoxChecked: Boolean = true,
    val customerId: String? = null,
    val phone: String? = null,
    val name: String?,
    val amount: Long? = 0,
    val type: Int?,
    val error: Boolean = false,
    val dueDate: DateTime? = DateTimeUtils.currentDateTime()
) : Serializable

sealed class ParsedDataModels {
    data class CustomerModel(
        val customer: CustomerUiTemplate
    ) : ParsedDataModels()

    data class FileModel(val fileName: String) : ParsedDataModels()

    object ParserErrorModel : ParsedDataModels()
}
