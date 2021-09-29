package tech.okcredit.android.base.preferences

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import tech.okcredit.android.base.preferences.SharedPreferencesMigration.Companion.convertToBusinessScopedKey

class SharedPreferencesMigrationTest {

    @Test
    fun `convertToIndividualScopedKey with valid businessScopedKey`() {
        val businessScopedKey = "0276eb52-c1e3-4be2-b07e-839f4eb9944c:::::should_show_filter_education"
        val individualScopedKey = SharedPreferencesMigration.convertToIndividualScopedKey(businessScopedKey)
        assertEquals("should_show_filter_education", individualScopedKey)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `convertToIndividualScopedKey with invalid businessScopedKey`() {
        val businessScopedKey = "should_show_filter_education"
        SharedPreferencesMigration.convertToIndividualScopedKey(businessScopedKey)
    }

    @Test
    fun convertToBusinessScopedKeyTest() {
        val businessId = "0276eb52-c1e3-4be2-b07e-839f4eb9944c"
        val individualScopedKey = "should_show_filter_education"
        val businessScopedKey = convertToBusinessScopedKey(individualScopedKey, businessId)
        assertEquals("0276eb52-c1e3-4be2-b07e-839f4eb9944c:::::should_show_filter_education", businessScopedKey)
    }

    @Test
    fun `changeKeyListScopeFromUserToBusinessScope when no matching keys present should not call put or remove`() {
        runBlocking {
            // Given
            val businessIdList = listOf("business-id-1", "business-id-2", "business-id-3")
            val prefs: SharedPreferences = mock()
            val keyList = listOf("key1", "key2")
            val sharedPreferencesEditor: SharedPreferences.Editor = mock()
            whenever(prefs.edit()).thenReturn(sharedPreferencesEditor)
            val allKeyValuePair = mapOf("key10" to "value10", "key11" to 2)
            whenever(prefs.all).thenReturn(allKeyValuePair)

            // When
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(prefs, keyList, businessIdList)

            // Then
            verify(sharedPreferencesEditor, times(0)).putInt(any(), any())
            verify(sharedPreferencesEditor, times(0)).putString(any(), any())
            verify(sharedPreferencesEditor, times(0)).remove(any())
        }
    }

    @Test
    fun `changeKeyListScopeFromUserToBusinessScope when 1 matching key present should call put and remove once`() {
        runBlocking {
            // Given
            val businessIdList = listOf("business-id-1", "business-id-2", "business-id-3")
            val prefs: SharedPreferences = mock()
            val keyList = listOf("key10", "key2")
            val sharedPreferencesEditor: SharedPreferences.Editor = mock()
            whenever(prefs.edit()).thenReturn(sharedPreferencesEditor)
            val allKeyValuePair = mapOf("key10" to "value10", "key11" to 2)
            whenever(prefs.all).thenReturn(allKeyValuePair)

            // When
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(prefs, keyList, businessIdList)

            // Then
            businessIdList.forEach { businessId ->
                val businessScopedKey = convertToBusinessScopedKey("key10", businessId)
                verify(sharedPreferencesEditor).putString(eq(businessScopedKey), any())
            }
            verify(sharedPreferencesEditor).remove(any())
            verify(sharedPreferencesEditor).remove("key10")
        }
    }

    @Test
    fun `changeKeyListScopeFromUserToBusinessScope when 5 matching key present should call put and remove 5 times`() {
        runBlocking {
            // Given
            val businessIdList = listOf("business-id-1", "business-id-2", "business-id-3")
            val prefs: SharedPreferences = mock()
            val keyList = listOf("key1", "key2", "key3", "key4", "key5")
            val sharedPreferencesEditor: SharedPreferences.Editor = mock()
            whenever(prefs.edit()).thenReturn(sharedPreferencesEditor)
            val allKeyValuePair = mapOf(
                "key1" to "value1",
                "key2" to 2,
                "key3" to 3f,
                "key4" to 4L,
                "key5" to false,
            )
            whenever(prefs.all).thenReturn(allKeyValuePair)

            // When
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(prefs, keyList, businessIdList)

            // Then
            val intersection = allKeyValuePair.keys.intersect(keyList.toSet())
            businessIdList.forEach { businessId ->
                val businessScopedKey = convertToBusinessScopedKey("key1", businessId)
                verify(sharedPreferencesEditor).putString(eq(businessScopedKey), any())
            }
            businessIdList.forEach { businessId ->
                val businessScopedKey = convertToBusinessScopedKey("key2", businessId)
                verify(sharedPreferencesEditor).putInt(eq(businessScopedKey), any())
            }
            businessIdList.forEach { businessId ->
                val businessScopedKey = convertToBusinessScopedKey("key3", businessId)
                verify(sharedPreferencesEditor).putFloat(eq(businessScopedKey), any())
            }
            businessIdList.forEach { businessId ->
                val businessScopedKey = convertToBusinessScopedKey("key4", businessId)
                verify(sharedPreferencesEditor).putLong(eq(businessScopedKey), any())
            }
            businessIdList.forEach { businessId ->
                val businessScopedKey = convertToBusinessScopedKey("key5", businessId)
                verify(sharedPreferencesEditor).putBoolean(eq(businessScopedKey), any())
            }
            intersection.forEach { key ->
                verify(sharedPreferencesEditor).remove(key)
            }
            verify(sharedPreferencesEditor, times(intersection.size)).remove(any())
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `changeKeyListScopeFromUserToBusinessScope when unsupported value type should throw exception`() {
        runBlocking {
            // Given
            val businessIdList = listOf("business-id-1", "business-id-2", "business-id-3")
            val prefs: SharedPreferences = mock()
            val keyList = listOf("key11")
            val sharedPreferencesEditor: SharedPreferences.Editor = mock()
            whenever(prefs.edit()).thenReturn(sharedPreferencesEditor)
            val allKeyValuePair = mapOf("key11" to "1".toBigDecimal())
            whenever(prefs.all).thenReturn(allKeyValuePair)

            // When
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(prefs, keyList, businessIdList)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `changeKeyListScopeFromUserToBusinessScope when incorrect key scope should throw exception`() {
        runBlocking {
            // Given
            val businessIdList = listOf("business-id-1", "business-id-2", "business-id-3")
            val prefs: SharedPreferences = mock()
            val keyList = listOf("business-id-1:::::key11")
            val sharedPreferencesEditor: SharedPreferences.Editor = mock()
            whenever(prefs.edit()).thenReturn(sharedPreferencesEditor)
            val allKeyValuePair = mapOf("business-id-1:::::key11" to 1)
            whenever(prefs.all).thenReturn(allKeyValuePair)

            // When
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(prefs, keyList, businessIdList)
        }
    }
}
