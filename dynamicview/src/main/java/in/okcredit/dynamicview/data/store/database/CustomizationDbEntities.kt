package `in`.okcredit.dynamicview.data.store.database

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["target", "businessId"], tableName = "CustomizationEntityV2")
data class CustomizationEntity(
    val target: String,
    @ColumnInfo(name = "component")
    val componentJsonString: String,
    val businessId: String,
)
