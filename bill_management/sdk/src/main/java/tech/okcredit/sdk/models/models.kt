package tech.okcredit.sdk.models

import androidx.room.Embedded
import androidx.room.Relation
import org.joda.time.DateTime
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.sdk.store.database.DBBill
import tech.okcredit.sdk.store.database.DbBillDoc

data class RawBill(
    val note: String?,
    val imageList: ArrayList<CapturedImage>?,
    val billDate: DateTime
)

enum class Ordering(val label: Int) {
    UNKNOWN(0),
    UPDATE_TIME(1),
    BILL_DATE(2),
    CREATE_TIME(3),
}

enum class Path(val route: String) {
    DOCS("/docs"),
    BILLS("/bills")
}

enum class Mask(val field: String) {
    AMOUNT("amount"),
    NOTES("note")
}

enum class Type(val operationType: Int) {
    UNKNOWN(0),
    ADD(1),
    UPDATE(2),
    DELETE(3),
}

data class BillWithDocs(
    @Embedded val dbBill: DBBill,
    @Relation(parentColumn = "id", entityColumn = "billId") val billDocList: List<DbBillDoc>

)

data class SelectedDate(
    val startDate: DateTime? = null,
    val endDate: DateTime? = null,
    val selectedMode: SelectedDateMode
)

enum class SelectedDateMode {
    CUSTOM_DATE,
    LAST_MONTH,
    OVERALL,
    CURRENT,
    LAST_TO_LAST
}
