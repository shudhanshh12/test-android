package `in`.okcredit.frontend.di

import `in`.okcredit.frontend.accounting.analytics.AccountingEventTrackerImpl
import `in`.okcredit.frontend.contract.AccountingEventTracker
import `in`.okcredit.frontend.contract.CheckAppLockAuthentication
import `in`.okcredit.frontend.contract.LoginDataSyncer
import `in`.okcredit.frontend.ui.AnalyticsHandlerImpl
import `in`.okcredit.frontend.usecase.LoginDataSyncerImpl
import `in`.okcredit.frontend.usecase.onboarding.applock.CheckAppLockAuthenticationImpl
import `in`.okcredit.shared.base.AnalyticsHandler
import dagger.Binds
import dagger.Module
import tech.okcredit.android.base.di.AppScope

@Module
abstract class FrontendModule {

    @Binds
    @AppScope
    abstract fun analyticsHandler(analyticsHandler: AnalyticsHandlerImpl): AnalyticsHandler

    @Binds
    @AppScope
    abstract fun accountingEventTracker(accountingEventTracker: AccountingEventTrackerImpl): AccountingEventTracker

    @Binds
    @AppScope
    abstract fun loginDataSyncer(loginDataSyncer: LoginDataSyncerImpl): LoginDataSyncer

    @Binds
    abstract fun appResumeAuthentication(checkAppLockAuthentication: CheckAppLockAuthenticationImpl): CheckAppLockAuthentication
}
