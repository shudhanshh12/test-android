package `in`.okcredit.analytics.di

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.IAnalyticsProvider
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerProperties
import dagger.Binds
import dagger.Provides
import tech.okcredit.android.base.di.AppScope

@dagger.Module
abstract class AnalyticsModule {

    @Binds
    @AppScope
    abstract fun bindProviders(providers: AnalyticsProvider): IAnalyticsProvider

    companion object {

        @Provides
        fun provideAppsFlyer(): AppsFlyerLib {
            return AppsFlyerLib.getInstance()
        }

        @Provides
        fun provideAppsFlyerProperties(): AppsFlyerProperties {
            return AppsFlyerProperties.getInstance()
        }
    }
}
