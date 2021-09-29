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

class IsAccountChatEnabledForSupplierImplTest {
    private val ab: AbRepository = mock()

    private val isAccountChatEnabledForSupplierImpl = IsAccountChatEnabledForSupplierImpl(Lazy { ab })

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `isAccountChatEnabledForSupplierImpl test`() {

        // given
        whenever(ab.isFeatureEnabled("accounts_chat")).thenReturn(Observable.just(true))

        // when
        val result = isAccountChatEnabledForSupplierImpl.execute().test()

        result.assertValue(
            true
        )
    }

    @Test
    fun `isAccountChatEnabledForSupplierImpl test false`() {

        // given
        whenever(ab.isFeatureEnabled("accounts_chat")).thenReturn(Observable.just(false))

        // when
        val result = isAccountChatEnabledForSupplierImpl.execute().test()

        result.assertValue(
            false
        )
    }

    @Test
    fun `isAccountChatEnabledForSupplierImpl returns error`() {

        val mockError: Exception = mock()

        whenever(ab.isFeatureEnabled("accounts_chat")).thenReturn(Observable.error(mockError))

        // when
        val result = isAccountChatEnabledForSupplierImpl.execute().test()

        result.assertError(mockError)
    }
}
