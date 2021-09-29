package `in`.okcredit.supplier.usecase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class IsSupplierCollectionEnabledImplTest {
    private val ab: AbRepository = mock()

    private val isSupplierCollectionEnabledImpl = IsSupplierCollectionEnabledImpl(Lazy { ab })

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `isSupplierCollectionEnabledImpl test`() {

        // given
        whenever(ab.isFeatureEnabled("supplier_collection")).thenReturn(Observable.just(true))

        // when
        val result = isSupplierCollectionEnabledImpl.execute().test()

        result.assertValue(
            true
        )
    }

    @Test
    fun `isSupplierCollectionEnabledImpl test false`() {

        // given
        whenever(ab.isFeatureEnabled("supplier_collection")).thenReturn(Observable.just(false))

        // when
        val result = isSupplierCollectionEnabledImpl.execute().test()

        result.assertValue(
            false
        )
    }

    @Test
    fun `isSupplierCollectionEnabledImpl return error`() {

        val mockError: Exception = mock()

        whenever(ab.isFeatureEnabled("supplier_collection")).thenReturn(Observable.error(mockError))

        // when
        val result = isSupplierCollectionEnabledImpl.execute().test()

        result.assertError(mockError)
    }
}
