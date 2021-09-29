package `in`.okcredit.collection.contract

import androidx.annotation.StringRes
import com.github.mikephil.charting.data.BarDataSet
import io.reactivex.Observable

interface CreditGraphicalDataProvider {

    enum class GraphDuration(@StringRes val stringId: Int, val code: Int) {
        TODAY(R.string.today, 0),
        YESTERDAY(R.string.yesterday, 1),
        WEEK(R.string.last_seven_days, 7),
        MONTH(R.string.last_thirty_days, 30);

        companion object {
            internal val lookupMapByCode = values().associateBy(GraphDuration::code)
            fun fromDurationCode(code: Int?) = lookupMapByCode[code] ?: WEEK
        }
    }

    fun execute(graphDuration: GraphDuration): Observable<GraphResponse>

    data class GraphResponse(
        val barDataSet: BarDataSet?,
        val labelNames: MutableList<String>,
        val offlineCollection: Long,
        val givenCreditAmount: Long,
        val onlineCollection: Long,
        val selectedDateRange: String,
        val graphDuration: GraphDuration
    )
}
