package tech.okcredit.sdk.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.sdk.models.SelectedDateMode
import tech.okcredit.sdk.store.BillLocalSource
import tech.okcredit.sdk.store.database.LocalBill
import java.util.*
import javax.inject.Inject

class GetBillsDateMapForAccount @Inject constructor(
    private val billLocalSource: Lazy<BillLocalSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<GetBillsDateMapForAccount.Request, GetBillsDateMapForAccount.Response> {
    override fun execute(req: Request): Observable<Result<Response>> {

        val startTimeInMs = if (req.startDate != null) {
            req.startDate!!.withTimeAtStartOfDay().millis.toString()
        } else "0"
        val endTimeInMs = if (req.endDate != null) {
            (req.endDate!!.withTimeAtStartOfDay().millis + 24 * 3600 * 1000 - 1).toString()
        } else DateTime.now().withTimeAtStartOfDay().plusDays(1).millis.toString()

        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                billLocalSource.get().getAllBillsForAccount(req.accountId, startTimeInMs, endTimeInMs, businessId).map {
                    val map = mutableMapOf<String, MutableList<LocalBill>>()
                    it.forEach {
                        val time = DateTimeUtils.getFormat4(DateTime(it.billDate!!.toLong()))
                        if (map.containsKey(time).not()) {
                            val list = mutableListOf<LocalBill>()
                            list.add(it)
                            map[time] = list
                        } else {
                            val list = map[time]!!
                            list.add(it)
                        }
                    }
                    var index = 0
                    var current: DateTime? = null
                    var last: DateTime? = null
                    var lastToLast: DateTime? = null
                    for (e in map) {
                        when (index) {
                            0 -> current = DateTimeUtils.stringToDateTime(e.key)
                            1 -> last = DateTimeUtils.stringToDateTime(e.key)
                            2 -> lastToLast = DateTimeUtils.stringToDateTime(e.key)
                        }
                        index++
                    }
                    return@map Response(map, current, last, lastToLast, req.selectedMode)
                }
            }

        )
    }

    data class Response(
        val map: MutableMap<String, MutableList<LocalBill>>,
        val currentMonth: DateTime?,
        val lastMonth: DateTime?,
        val lastToLastMonth: DateTime?,
        val selectedMode: SelectedDateMode,
    )

    data class Request(
        val accountId: String,
        var startDate: DateTime? = null,
        var endDate: DateTime? = null,
        val selectedMode: SelectedDateMode,
    )
}
