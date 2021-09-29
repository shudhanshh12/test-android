package `in`.okcredit.frontend.usecase

import `in`.okcredit.backend._offline.usecase.SetMerchantPreference
import `in`.okcredit.individual.contract.PreferenceKey
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.junit.Test

class SetPaymentPasswordTest {
    private val setMerchantPreference: SetMerchantPreference = mock()
    private val setPaymentPassword = SetPaymentPassword(setMerchantPreference)

    companion object {
        val PAYMENT_PASSWORD = PreferenceKey.PAYMENT_PASSWORD

        val enable = true
    }

    @Test
    fun `when execute called`() {
        // given
        whenever(setMerchantPreference.execute(PAYMENT_PASSWORD, enable.toString(), false))
            .thenReturn(Completable.complete())

        // when
        val testObserver = setPaymentPassword.execute(SetPaymentPassword.Request(enable))
            .subscribeOn(Schedulers.trampoline()).test()

        // then
        testObserver.assertComplete()
        testObserver.dispose()
    }
}
