package `in`.okcredit.merchant.migration

import `in`.okcredit.merchant.store.BusinessLocalSource
import `in`.okcredit.shared.usecase.MigrationEventLogger
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nhaarman.mockitokotlin2.*
import org.junit.Test

class MultipleAccountsDatabaseMigrationHandlerImplTest {

    private val database: SupportSQLiteDatabase = mock()
    private val tableName: String = "tableName"
    private val localSource: BusinessLocalSource = mock()
    private val migrationEventLogger: MigrationEventLogger = mock()

    private val multipleAccountsDatabaseMigrationHelperImpl =
        MultipleAccountsDatabaseMigrationHandlerImpl({ localSource }, { migrationEventLogger })

    @Test
    fun `when logged in then add column, index and update business id`() {
        // Given
        val businessId = "businessId"
        whenever(localSource.getBusinessIdForMultipleAccountsMigration()).thenReturn(businessId)

        // When
        multipleAccountsDatabaseMigrationHelperImpl.execute(database, tableName)

        // Then
        verify(database).execSQL("ALTER TABLE $tableName ADD COLUMN businessId TEXT NOT NULL DEFAULT ''")
        verify(database).execSQL("CREATE INDEX IF NOT EXISTS index_tablename_businessId ON $tableName (businessId)")
        verify(localSource).getBusinessIdForMultipleAccountsMigration()
        verify(database).update(eq(tableName), any(), any(), anyOrNull(), any())
    }

    @Test
    fun `when not logged in then add column, index`() {
        // Given
        val businessId: String? = null
        whenever(localSource.getBusinessIdForMultipleAccountsMigration()).thenReturn(businessId)

        // When
        multipleAccountsDatabaseMigrationHelperImpl.execute(database, tableName)

        // Then
        verify(database).execSQL("ALTER TABLE $tableName ADD COLUMN businessId TEXT NOT NULL DEFAULT ''")
        verify(database).execSQL("CREATE INDEX IF NOT EXISTS index_tablename_businessId ON $tableName (businessId)")
        verify(localSource).getBusinessIdForMultipleAccountsMigration()
        verify(database, times(0)).update(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }
}
