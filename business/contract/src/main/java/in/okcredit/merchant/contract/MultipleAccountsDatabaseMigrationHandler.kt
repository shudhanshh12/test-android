package `in`.okcredit.merchant.contract

import androidx.sqlite.db.SupportSQLiteDatabase

interface MultipleAccountsDatabaseMigrationHandler {
    fun execute(database: SupportSQLiteDatabase, tableName: String)
}
