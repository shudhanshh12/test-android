package tech.okcredit.bill_management_ui

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import tech.okcredit.base.dagger.di.UiThread
import tech.okcredit.base.dagger.di.scope.ActivityScope
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.bill_management_ui._di.BillFragmentModule
import tech.okcredit.bill_management_ui.billdetail.BillDetailFragment
import tech.okcredit.bill_management_ui.billdetail._di.BillDetailModule
import tech.okcredit.bill_management_ui.billintroductionbottomsheet.BillIntroductionBottomSheetScreen
import tech.okcredit.bill_management_ui.billintroductionbottomsheet._di.BillIntroductionBottomSheetModule
import tech.okcredit.bill_management_ui.edit_notes.EditNoteFragment
import tech.okcredit.bill_management_ui.edit_notes.EditNoteModule
import tech.okcredit.bills.GetAndUpdateBillSeenFirstTime
import tech.okcredit.use_case.GetAndUpdateBillSeenFirstTimeImpl

@Module
abstract class BillActivity_Module {

    @Binds
    abstract fun activity(activity: BillActivity): AppCompatActivity

    @Binds
    @Reusable
    abstract fun getAndUpdateBillSeenFirstTimeUseCase(
        getAndUpdateBillSeenFirstTimeImpl: GetAndUpdateBillSeenFirstTimeImpl
    ): GetAndUpdateBillSeenFirstTime

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
    @ContributesAndroidInjector(modules = [BillFragmentModule::class])
    abstract fun billScreen(): BillFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [BillIntroductionBottomSheetModule::class])
    abstract fun billIntroductionBottomSheetScreen(): BillIntroductionBottomSheetScreen

    @FragmentScope
    @ContributesAndroidInjector(modules = [EditNoteModule::class])
    abstract fun editNoteScreen(): EditNoteFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [BillDetailModule::class])
    abstract fun billDetailScreen(): BillDetailFragment
}
