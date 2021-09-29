package tech.okcredit.home.usecase.dashboard

import `in`.okcredit.backend._offline.usecase.GetAccountSummary
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.home.R
import javax.inject.Inject

class NetBalanceValueProvider @Inject constructor(
    private val accountSummary: Lazy<GetAccountSummary>,
    private val context: Lazy<Context>
) : DashboardValueProvider {

    companion object {
        const val NET_BALANCE = "net_balance"
    }

    override fun getValue(request: DashboardValueProvider.Request?): Observable<DashboardValueProvider.Response> {
        return accountSummary.get().execute()
            .map { NetBalanceDashboardValue(value = it.balance, string = getStringForAmount(it.balance)) }
    }

    private fun getStringForAmount(balance: Long): String {
        val id = if (balance >= 0) R.string.advance
        else R.string.due
        return context.get().getString(id)
    }

    class NetBalanceDashboardValue(override val exclude: Boolean = false, val value: Long, val string: String? = null) :
        DashboardValueProvider.Response(exclude)
}
