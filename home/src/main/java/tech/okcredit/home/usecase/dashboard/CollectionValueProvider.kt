package tech.okcredit.home.usecase.dashboard

import `in`.okcredit.collection.contract.CreditGraphicalDataProvider
import `in`.okcredit.collection.contract.CreditGraphicalDataProvider.GraphDuration
import android.content.Context
import androidx.annotation.StringRes
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.home.usecase.dashboard.DashboardValueProvider.Response
import javax.inject.Inject

class CollectionValueProvider @Inject constructor(
    private val creditGraphicalDataProvider: Lazy<CreditGraphicalDataProvider>,
    private val context: Lazy<Context>,
) : DashboardValueProvider {

    companion object {
        const val COLLECTION = "collection"
    }

    override fun getValue(request: DashboardValueProvider.Request?): Observable<Response> {
        return creditGraphicalDataProvider.get().execute(GraphDuration.fromDurationCode(request?.input))
            .map {
                CollectionDashboardValue(
                    value = getAmount(it),
                    string = getString(it.graphDuration.stringId)
                )
            }
    }

    private fun getAmount(value: CreditGraphicalDataProvider.GraphResponse): Long {
        return value.offlineCollection + value.onlineCollection
    }

    private fun getString(@StringRes id: Int): String {
        return context.get().getString(id)
    }

    class CollectionDashboardValue(
        override val exclude: Boolean = false,
        val value: Long? = null,
        val string: String? = null
    ) : DashboardValueProvider.Response(exclude)
}
