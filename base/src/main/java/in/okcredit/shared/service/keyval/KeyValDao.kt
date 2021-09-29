package `in`.okcredit.shared.service.keyval

import androidx.room.Dao
import androidx.room.Query

@Deprecated("Use shared preferences")
@Dao
interface KeyValDao {

    @Query("SELECT * FROM Entry")
    fun getAllData(): List<Entry>
}
