package `in`.okcredit.merchant.migration

import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import `in`.okcredit.merchant.store.BusinessLocalSource
import `in`.okcredit.shared.usecase.MigrationEventLogger
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Lazy
import timber.log.Timber
import javax.inject.Inject

class MultipleAccountsDatabaseMigrationHandlerImpl @Inject constructor(
    // local source is used directly in usecase to avoid exposing deprecated method via repository
    private val localSource: Lazy<BusinessLocalSource>,
    private val migrationEventLogger: Lazy<MigrationEventLogger>,
) : MultipleAccountsDatabaseMigrationHandler {

    companion object {
        const val COLUMN_NAME = "businessId"
    }

    override fun execute(database: SupportSQLiteDatabase, tableName: String) {
        try {
            addColumn(database, tableName)
            addIndex(database, tableName)
            updateBusinessIdValue(database, tableName)
            Timber.i("MultipleAccountsDatabaseMigrationHelperImpl: Migration successful for $tableName")
        } catch (exception: Exception) { // log and relay exception
            migrationEventLogger.get().logDatabaseTableMigrationError(tableName, database.path, exception)
            throw exception
        }
    }

    private fun addColumn(database: SupportSQLiteDatabase, tableName: String) {
        database.execSQL("ALTER TABLE $tableName ADD COLUMN $COLUMN_NAME TEXT NOT NULL DEFAULT ''")
    }

    private fun addIndex(database: SupportSQLiteDatabase, tableName: String) {
        val lowerCaseTableName = tableName.replace("`", "").lowercase()
        val indexName = "index_${lowerCaseTableName}_$COLUMN_NAME"
        database.execSQL("CREATE INDEX IF NOT EXISTS $indexName ON $tableName ($COLUMN_NAME)")
    }

    private fun updateBusinessIdValue(database: SupportSQLiteDatabase, tableName: String) {
        val businessId = localSource.get().getBusinessIdForMultipleAccountsMigration() // non-reactive method call
        if (businessId != null) {
            val conflictAlgo = SQLiteDatabase.CONFLICT_REPLACE
            val contentValues = ContentValues().apply { put(COLUMN_NAME, businessId) }
            database.update(tableName, conflictAlgo, contentValues, null, emptyArray())
        } else {
            Timber.i("MultipleAccountsDatabaseMigrationHelperImpl: Not logged in ($tableName)")
        }
    }
}
