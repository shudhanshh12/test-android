package tech.okcredit.home.dialogs.customer_profile_dialog._di

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialog
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileTransparentActivity

@Module
abstract class CustomerProfileTransparentActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [CustomerProfileDialogModule::class])
    abstract fun customerProfileDialogDialog(): CustomerProfileDialog

    @Binds
    abstract fun activity(activity: CustomerProfileTransparentActivity): AppCompatActivity
}
