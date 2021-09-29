package tech.okcredit.android.base.preferences

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.*
import dagger.Lazy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SharedPreferencesMigrationHandlerTest {

    private val name = "sample"
    private val sharedPreference: SharedPreferences = mock()
    private val loggerLazy: Lazy<SharedPreferencesMigrationHandler.Logger> = mock()
    private val logger: SharedPreferencesMigrationHandler.Logger = mock()
    private val dispatcherProvider: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setup() {
        whenever(loggerLazy.get()).thenReturn(logger)
    }

    @Test
    fun `when no version updates then do not change pref version`() {
        // Given
        val version = 1
        val migrationList: List<SharedPreferencesMigration> = emptyList()
        val sharedPreferencesMigrationHelper =
            SharedPreferencesMigrationHandler(
                version,
                name,
                sharedPreference,
                migrationList,
                loggerLazy,
                dispatcherProvider
            )
        whenever(sharedPreference.getInt(":::::version", 0)).thenReturn(version)

        // When
        sharedPreferencesMigrationHelper.checkForMigrations()

        // Then
        verify(sharedPreference).getInt(":::::version", 0)
        verify(sharedPreference, times(0)).edit()
        verifyZeroInteractions(logger)
    }

    @Test
    fun `when single version update then call migrate and change pref version`() {
        // Given
        val fromVersion = 1
        val toVersion = 2
        val migration = mock<SharedPreferencesMigration>().apply {
            whenever(this.fromVersion).thenReturn(1)
            whenever(this.toVersion).thenReturn(2)
            whenever(this.migrate).thenReturn(mock())
        }
        val migrationList: List<SharedPreferencesMigration> = listOf(migration)
        val sharedPreferencesMigrationHelper =
            SharedPreferencesMigrationHandler(
                toVersion,
                name,
                sharedPreference,
                migrationList,
                loggerLazy,
                dispatcherProvider
            )
        whenever(sharedPreference.getInt(":::::version", 0)).thenReturn(fromVersion)
        val sharedPrefEditor = mock<SharedPreferences.Editor>()
        whenever(sharedPreference.edit()).thenReturn(sharedPrefEditor)
        whenever(sharedPrefEditor.putInt(any(), any())).thenReturn(sharedPrefEditor)

        // When
        sharedPreferencesMigrationHelper.checkForMigrations()

        // Then
        verify(sharedPreference, times(2)).getInt(":::::version", 0)
        verify(sharedPreference).edit()
        verify(sharedPrefEditor).putInt(":::::version", toVersion)
        verify(sharedPrefEditor).commit()
        verify(migration).migrate
        verify(logger).logSharedPreferencesMigrationSuccess(name, 1, 2)
    }

    @Test
    fun `when multiple version updates then call migrate and change pref version for each update`() {
        // Given
        val fromVersion = 2
        val toVersion = 5
        val migration2To3 = mock<SharedPreferencesMigration>().apply {
            whenever(this.fromVersion).thenReturn(2)
            whenever(this.toVersion).thenReturn(3)
            whenever(this.migrate).thenReturn(mock())
        }
        val migration3To4 = mock<SharedPreferencesMigration>().apply {
            whenever(this.fromVersion).thenReturn(3)
            whenever(this.toVersion).thenReturn(4)
            whenever(this.migrate).thenReturn(mock())
        }
        val migration4To5 = mock<SharedPreferencesMigration>().apply {
            whenever(this.fromVersion).thenReturn(4)
            whenever(this.toVersion).thenReturn(5)
            whenever(this.migrate).thenReturn(mock())
        }
        val migrationList: List<SharedPreferencesMigration> = listOf(migration2To3, migration3To4, migration4To5)
        val sharedPreferencesMigrationHelper =
            SharedPreferencesMigrationHandler(
                toVersion,
                name,
                sharedPreference,
                migrationList,
                loggerLazy,
                dispatcherProvider
            )
        whenever(sharedPreference.getInt(":::::version", 0)).thenReturn(fromVersion)
        val sharedPrefEditor = mock<SharedPreferences.Editor>()
        whenever(sharedPreference.edit()).thenReturn(sharedPrefEditor)
        whenever(sharedPrefEditor.putInt(any(), any())).thenReturn(sharedPrefEditor)

        // When
        sharedPreferencesMigrationHelper.checkForMigrations()

        // Then
        verify(sharedPreference, times(2)).getInt(":::::version", 0)
        verify(sharedPreference, times(toVersion - fromVersion)).edit()
        verify(sharedPrefEditor, times(toVersion - fromVersion)).commit()
        listOf(3, 4, 5).forEach {
            verify(sharedPrefEditor).putInt(":::::version", it)
            verify(logger).logSharedPreferencesMigrationSuccess(name, it - 1, it)
        }
        listOf(migration2To3, migration3To4, migration4To5).forEach {
            verify(it).migrate
        }
    }

    @Test
    fun `when version downgrade then throw exception`() {
        // Given
        val fromVersion = 6
        val toVersion = 5
        val migrationList: List<SharedPreferencesMigration> = emptyList()
        val sharedPreferencesMigrationHelper =
            SharedPreferencesMigrationHandler(
                toVersion,
                name,
                sharedPreference,
                migrationList,
                loggerLazy,
                dispatcherProvider
            )
        whenever(sharedPreference.getInt(":::::version", 0)).thenReturn(fromVersion)

        // When
        try {
            sharedPreferencesMigrationHelper.checkForMigrations()
            fail("should have gone into catch block")
        } catch (e: Exception) {
            // Then
            verify(sharedPreference, times(2)).getInt(":::::version", 0)
            verify(sharedPreference, times(0)).edit()
            logger.logSharedPreferencesMigrationError(name, e)
            assertEquals("Version downgrade (6 -> 5) is not supported", e.message)
        }
    }

    @Test
    fun `when version update without migration then throw exception`() {
        // Given
        val fromVersion = 5
        val toVersion = 6
        val migrationList: List<SharedPreferencesMigration> = emptyList()
        val sharedPreferencesMigrationHelper =
            SharedPreferencesMigrationHandler(
                toVersion,
                name,
                sharedPreference,
                migrationList,
                loggerLazy,
                dispatcherProvider
            )
        whenever(sharedPreference.getInt(":::::version", 0)).thenReturn(fromVersion)

        // When
        try {
            sharedPreferencesMigrationHelper.checkForMigrations()
            fail("should have gone into catch block")
        } catch (e: Exception) {
            // Then
            verify(sharedPreference, times(2)).getInt(":::::version", 0)
            verify(sharedPreference, times(0)).edit()
            logger.logSharedPreferencesMigrationError(name, e)
            assertEquals("No migration provided for upgrading from version 5 to 6", e.message)
        }
    }

    @Test
    fun `when multiple threads running & single version update then call migrate and change pref version only once`() {
        // Given
        val threadCount = 2

        val fromVersion = 1
        val toVersion = 2
        val migration = mock<SharedPreferencesMigration>().apply {
            whenever(this.fromVersion).thenReturn(1)
            whenever(this.toVersion).thenReturn(2)
            whenever(this.migrate).thenReturn(mock())
        }
        val migrationList: List<SharedPreferencesMigration> = listOf(migration)

        var versionUpdatedByThread1 = 1
        whenever(sharedPreference.getInt(":::::version", 0))
            .thenAnswer { fromVersion } // thread-1 before synchronized {}
            .thenAnswer { fromVersion } // thread-2 before synchronized {}
            .thenAnswer { fromVersion } // thread-1 inside synchronized {}
            .thenAnswer { versionUpdatedByThread1 } // thread-2 inside synchronized {}

        val sharedPrefEditor = mock<SharedPreferences.Editor>()
        whenever(sharedPreference.edit()).doAnswer {
            versionUpdatedByThread1 = toVersion
            sharedPrefEditor
        }
        whenever(sharedPrefEditor.putInt(any(), any())).thenReturn(sharedPrefEditor)
        val executorService: ExecutorService = Executors.newFixedThreadPool(threadCount)

        // When
        repeat(threadCount) { // trigger checkForMigrations on each thread - on separate instances of SharedPreferencesMigrationHandler
            executorService.submit {
                SharedPreferencesMigrationHandler(
                    toVersion,
                    name,
                    sharedPreference,
                    migrationList,
                    loggerLazy,
                    dispatcherProvider
                ).checkForMigrations()
            }
        }

        executorService.shutdown()
        executorService.awaitTermination(2, TimeUnit.SECONDS)

        // Then
        verify(sharedPreference, times(threadCount * 2)).getInt(":::::version", 0)
        verify(sharedPreference).edit()
        verify(sharedPrefEditor).putInt(":::::version", toVersion)
        verify(sharedPrefEditor).commit()
        verify(migration).migrate
        verify(logger).logSharedPreferencesMigrationSuccess(name, 1, 2)
    }
}
