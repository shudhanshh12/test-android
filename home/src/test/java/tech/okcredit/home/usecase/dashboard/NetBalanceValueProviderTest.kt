package tech.okcredit.home.usecase.dashboard

import `in`.okcredit.backend._offline.usecase.GetAccountSummary
import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import tech.okcredit.home.R

class NetBalanceValueProviderTest {

    private val accountSummary = mock<GetAccountSummary>()
    private val context = mock<Context>()
    private val accountSummaryLazy: Lazy<GetAccountSummary> = Lazy { accountSummary }
    private val contextLazy: Lazy<Context> = Lazy { context }
    private val netBalanceValueProvider = NetBalanceValueProvider(accountSummaryLazy, contextLazy)

    private val positiveString = "Advance"
    private val negativeString = "Due"

    private val positiveBalance = 1000L
    private val negativeBalance = -1000L

    @Before
    fun setUp() {
        whenever(context.getString(R.string.advance)).thenReturn(positiveString)
        whenever(context.getString(R.string.due)).thenReturn(negativeString)
    }

    @Test
    fun `getValue() when balance is positive then provider return you pay string`() {
        val summary = mock<GetAccountSummary.AccountSummary>()
        whenever(summary.balance).thenReturn(positiveBalance)
        whenever(accountSummary.execute()).thenReturn(Observable.just(summary))

        val testObserver = netBalanceValueProvider.getValue(null).test()

        val result = testObserver.values().first() as NetBalanceValueProvider.NetBalanceDashboardValue
        Assert.assertTrue(result.value == positiveBalance)
        Assert.assertTrue(result.string == positiveString)
    }

    @Test
    fun `getValue() when balance is negative then provider return you get string`() {
        val summary = mock<GetAccountSummary.AccountSummary>()
        whenever(summary.balance).thenReturn(negativeBalance)
        whenever(accountSummary.execute()).thenReturn(Observable.just(summary))

        val testObserver = netBalanceValueProvider.getValue(null).test()

        val result = testObserver.values().first() as NetBalanceValueProvider.NetBalanceDashboardValue
        Assert.assertTrue(result.value == negativeBalance)
        Assert.assertTrue(result.string == negativeString)
    }
}
