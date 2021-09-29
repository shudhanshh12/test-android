package `in`.okcredit.merchant.collection.store.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectionDatabaseMigrationTest {

    companion object {
        const val TEST_DB_NAME = "collection_test.db"
    }

    private val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CollectionDataBase::class.java.name
    )

    @Test
    fun `test migration 9 to 10`() {
        System.setProperty("robolectric.logging.enabled", "true")
        migrationTestHelper.createDatabase(TEST_DB_NAME, 9).apply {
            close()
        }

        migrationTestHelper.runMigrationsAndValidate(TEST_DB_NAME, 10, true, CollectionDataBase.MIGRATION_9_10).apply {
            close()
        }
    }

    @Test
    fun `test migration 10 to 11`() {
        System.setProperty("robolectric.logging.enabled", "true")
        migrationTestHelper.createDatabase(TEST_DB_NAME, 10).apply {
            close()
        }

        migrationTestHelper.runMigrationsAndValidate(TEST_DB_NAME, 11, true, CollectionDataBase.MIGRATION_10_11).apply {
            close()
        }
    }

    @Test
    fun `test migration 9 to 11`() {
        System.setProperty("robolectric.logging.enabled", "true")
        migrationTestHelper.createDatabase(TEST_DB_NAME, 9).apply {
            close()
        }

        migrationTestHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            11,
            true,
            CollectionDataBase.MIGRATION_9_10,
            CollectionDataBase.MIGRATION_10_11
        ).apply {
            close()
        }
    }

    @Test
    fun `test migration 11 to 12`() {
        migrationTestHelper.createDatabase(TEST_DB_NAME, 11).apply {
            close()
        }

        migrationTestHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            12,
            true,
            CollectionDataBase.MIGRATION_11_12
        ).apply {
            close()
        }
    }

    @Test
    fun `test migration 12 to 13`() {
        migrationTestHelper.createDatabase(TEST_DB_NAME, 12).apply {
            close()
        }

        migrationTestHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            13,
            true,
            CollectionDataBase.MIGRATION_12_13
        ).apply {
            close()
        }
    }
}
