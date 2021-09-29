package `in`.okcredit.merchant.rewards.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Flowable
import io.reactivex.Observable

@Dao
interface RewardsDataBaseDao {

    /*********************** Rewards ***********************/
    @Query("SELECT * FROM Rewards WHERE rewardType NOT IN (:filterOutTypes) ORDER BY createTime DESC")
    fun listRewards(filterOutTypes: List<String>): Flowable<List<Rewards>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(vararg rewards: Rewards)

    @Query("DELETE FROM Rewards")
    fun clearRewards()

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun setRewardClaimed(rewards: Rewards)

    @Query("SELECT * FROM Rewards WHERE rewardType IN (:types) ORDER BY createTime DESC")
    fun getRewards(types: List<String>): Observable<List<Rewards>>

    @Query("SELECT * FROM Rewards WHERE id = :id LIMIT 1")
    fun getRewardById(id: String): Observable<Rewards>

    fun getDistinctRewardById(id: String): Observable<Rewards> = getRewardById(id).distinctUntilChanged()

    fun getDistinctRewards(types: List<String>): Observable<List<Rewards>> = getRewards(types).distinctUntilChanged()
}
