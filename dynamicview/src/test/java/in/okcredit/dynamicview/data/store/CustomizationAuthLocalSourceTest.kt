package `in`.okcredit.dynamicview.data.store

import `in`.okcredit.dynamicview.R
import `in`.okcredit.dynamicview.data.store.database.CustomizationDatabaseDao
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ResourceUtils

class CustomizationAuthLocalSourceTest {

    private val testHelper = CustomizationTestHelper()

    private val resourceUtils: ResourceUtils = mock()
    private val customizationDatabaseDao: CustomizationDatabaseDao = mock()
    private val schedulerProvider: SchedulerProvider = mock()
    private val store =
        CustomizationStore(testHelper.moshi, resourceUtils, customizationDatabaseDao)
    private val businessId = "business-id"

    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit
    }

    @Test
    fun `should convert list into json and save it to preference`() {
        runBlocking {
            val customizations = testHelper.getDummyCustomizations()

            // When
            store.saveCustomizations(customizations, businessId)

            // Then
            verify(customizationDatabaseDao).insert(*testHelper.getDummyCustomizationEntities().toTypedArray())
        }
    }

    @Test
    fun `should return customizations list when it is present in preferences and is not blank`() {
        runBlocking {
            // Given
            whenever(customizationDatabaseDao.getCustomizations(businessId)).thenReturn(
                Observable.just(testHelper.getDummyCustomizationEntities())
            )

            // When
            val result = store.getCustomizations(businessId).test()

            // Then
            result.assertValue(testHelper.getDummyCustomizations())
        }
    }

    @Test
    fun `should return default customizations list when it is not present in preferences or is blank`() {
        runBlocking {
            // Given
            whenever(customizationDatabaseDao.getCustomizations(businessId)).thenReturn(Observable.just(emptyList()))
            whenever(resourceUtils.getRawResource(R.raw.customization_fallback))
                .thenReturn(testHelper.getDummyCustomizationsJson())

            // When
            val result = store.getCustomizations(businessId).test()

            // Then
            result.assertValue(testHelper.getDummyCustomizations())
        }
    }

    @Test
    fun `should clear customizations`() {
        runBlocking {
            // Given
            whenever(schedulerProvider.io()).thenReturn(Schedulers.trampoline())

            // When
            store.clearCustomizations(businessId)

            // Then
            verify(customizationDatabaseDao, times(1)).clearCustomizations(businessId)
        }
    }

    @Test
    fun `should clear all customizations`() {
        runBlocking {
            // Given
            whenever(schedulerProvider.io()).thenReturn(Schedulers.trampoline())

            // When
            store.clearAllCustomizations()

            // Then
            verify(customizationDatabaseDao, times(1)).clearAllCustomizations()
        }
    }
}
