package merchant.android.okstream.sdk.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface OkStreamDataBaseDao {

    @Query("DELETE FROM PublishMessage")
    fun deleteAllMessages(): Completable

    @Query("DELETE FROM PublishMessage WHERE id= :messageId")
    fun deleteMessage(messageId: String): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(vararg publishMessageEntity: PublishMessage): Completable

    @Query("SELECT * FROM PublishMessage")
    fun getMessages(): Flowable<List<PublishMessage>>
}
