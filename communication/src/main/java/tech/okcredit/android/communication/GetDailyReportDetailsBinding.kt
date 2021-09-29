package tech.okcredit.android.communication

import io.reactivex.Single

interface GetDailyReportDetailsBinding {
    fun getSendReminderBinding(): Single<DailyReportResponce>
}
