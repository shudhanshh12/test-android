package tech.okcredit.home.ui.add_transaction_home_search.di

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.home.ui.add_transaction_home_search.AddTransactionShortcutSearchFragment
import tech.okcredit.home.ui.add_transaction_home_search.AddTxnShortcutSearchActivity

@Module
abstract class AddTxnShortcutSearchActivityModule {

    @Binds
    abstract fun activity(activity: AddTxnShortcutSearchActivity): AppCompatActivity

    @ContributesAndroidInjector(modules = [AddTransactionHomeSearchModule::class])
    abstract fun addTransactionHomeSearchScreen(): AddTransactionShortcutSearchFragment
}
