package `in`.okcredit.frontend.ui

import `in`.okcredit.analytics.*
import `in`.okcredit.frontend.ui.account_statement.AccountStatementContract
import `in`.okcredit.frontend.ui.payment_password.PasswordEnableContract
import `in`.okcredit.shared.base.AnalyticsHandler
import `in`.okcredit.shared.base.UserIntent
import org.joda.time.Days
import tech.okcredit.android.base.utils.DateTimeUtils
import javax.inject.Inject

@Deprecated("OnboardingEvent should be handled by the the concrete viewModel")
class AnalyticsHandlerImpl @Inject constructor(private val tracker: Tracker) : AnalyticsHandler {
    override fun handleUserIntent(intent: UserIntent) {
        when (intent) {
            is AccountStatementContract.Intent.ChangeDateRange -> {
                val interval = Days.daysBetween(intent.startDate, intent.endDate).days
                val to = DateTimeUtils.formatDateOnly(intent.endDate)
                Analytics.track(
                    AnalyticsEvents.UPDATE_ACCOUNT_STATEMENT,
                    EventProperties
                        .create()
                        .with("interval", interval)
                        .with("to", to)
                )
            }

            is AccountStatementContract.Intent.DownloadStatement -> {
                val interval = Days.daysBetween(intent.startDate, intent.endDate).days
                val to = DateTimeUtils.formatDateOnly(intent.endDate)
                Analytics.track(
                    AnalyticsEvents.DOWNLOAD_ACCOUNT_STATEMENT,
                    EventProperties
                        .create()
                        .with("interval", interval)
                        .with("to", to)
                )
            }

            is PasswordEnableContract.Intent.OnForgotPasswordClicked -> {
                Analytics.track(
                    AnalyticsEvents.FORGOT_PASSWORD,
                    EventProperties
                        .create()
                        .with(PropertyKey.SOURCE, "payment_password_page")
                )
            }
        }
    }
}
