package tech.okcredit.bill_management_ui.editBill

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
import tech.okcredit.bill_management_ui.editBill._di.EditBillFragmentModule

@Module
abstract class EditBillModule {

    @Binds
    abstract fun activity(activity: EditBillActivity): AppCompatActivity

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
    @ContributesAndroidInjector(modules = [EditBillFragmentModule::class])
    abstract fun billImageDetailScreen(): EditBillFragment
}
