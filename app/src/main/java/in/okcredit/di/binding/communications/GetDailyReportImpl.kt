package `in`.okcredit.di.binding.communications

import `in`.okcredit.backend._offline.usecase.GetDailyReport
import io.reactivex.Single
import tech.okcredit.android.communication.DailyReportResponce
import tech.okcredit.android.communication.GetDailyReportDetailsBinding
import javax.inject.Inject

class GetDailyReportImpl @Inject constructor(private val getDailyReport: GetDailyReport) :
    GetDailyReportDetailsBinding {
    override fun getSendReminderBinding(): Single<DailyReportResponce> {
        return getDailyReport.execute().firstOrError().flatMap {
            Single.just(
                DailyReportResponce(
                    it.netBalance, it.netCreditAmount, it.netPaymentAmount
                )
            )
        }
    }
}
