package tech.okcredit.contacts.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Observable

@Dao
interface ContactsDataBaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContacts(contacts: List<Contact>)

    @Query("SELECT * FROM Contact order by name")
    fun getContacts(): Observable<List<Contact>>

    @Query("SELECT * FROM Contact WHERE name LIKE :searchQuery OR mobile LIKE :searchQuery ORDER BY name")
    fun getFilteredContacts(searchQuery: String): Observable<List<Contact>>

    @Query("SELECT * FROM Contact WHERE synced = 0 ORDER BY timestamp ASC LIMIT :pagingLimit")
    suspend fun getUnsyncedContacts(pagingLimit: Int): List<Contact>

    @Query("UPDATE Contact SET synced = :synced WHERE mobile IN (:mobileList)")
    suspend fun updateSyncStatus(mobileList: List<String>, synced: Boolean)

    @Query("UPDATE Contact SET found = :found, type = :type, synced = :synced WHERE mobile = :mobile")
    suspend fun updateFoundStatusAndType(mobile: String, found: Boolean, type: Int, synced: Boolean = true)

    @Query("SELECT timestamp FROM Contact ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastUpdatedTimestamp(): Long?

    @Query("SELECT mobile from Contact")
    suspend fun getContactMobileNumbers(): List<String>

    @Query("DELETE FROM Contact WHERE mobile IN (:mobileList)")
    suspend fun deleteContacts(mobileList: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(vararg contactEntity: Contact)
}
