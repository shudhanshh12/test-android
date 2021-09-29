package `in`.okcredit.web_features.cash_counter._di

import `in`.okcredit.web_features.cash_counter.CashCounterFragment
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import tech.okcredit.base.dagger.di.UiThread
import tech.okcredit.base.dagger.di.scope.ActivityScope
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class CashCounterActivityModule {

    companion object {

        @Provides
        @ActivityScope
        @UiThread
        fun uiScheduler(): Scheduler = AndroidSchedulers.mainThread()
    }

    @FragmentScope
    @ContributesAndroidInjector(modules = [CashCounterModule::class])
    abstract fun cashCounterScreen(): CashCounterFragment
}
