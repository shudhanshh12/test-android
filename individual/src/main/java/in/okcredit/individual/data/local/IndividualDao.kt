package `in`.okcredit.individual.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IndividualDao {

    @Query("SELECT * FROM Individual LIMIT 1")
    fun getIndividual(): Flow<Individual>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setIndividual(individual: Individual)

    @Query("DELETE FROM Individual")
    suspend fun deleteIndividual()
}
