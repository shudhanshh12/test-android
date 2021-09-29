package tech.okcredit.bill_management_ui.selected_bills

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import tech.okcredit.base.dagger.di.UiThread
import tech.okcredit.base.dagger.di.scope.ActivityScope
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.bill_management_ui.selected_bills.selectedimage.SelectedImageFragment
import tech.okcredit.bill_management_ui.selected_bills.selectedimage._di.SelectedImageModule

@Module
abstract class SelectedBillModule {

    @Binds
    abstract fun activity(activity: SelectedBillActivity): AppCompatActivity

    companion object {

        @Provides
        @ActivityScope
        @UiThread
        fun uiScheduler(): Scheduler = AndroidSchedulers.mainThread()
    }

    /****************************************************************
     * Fragments
     ****************************************************************/

    @FragmentScope
    @ContributesAndroidInjector(modules = [SelectedImageModule::class])
    abstract fun selectedImageScreen(): SelectedImageFragment
}
