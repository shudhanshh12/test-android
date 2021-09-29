package tech.okcredit.bill_management_ui.enhance_image

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
import tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen.EnhanceImageFragment
import tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen._di.EnhanceImageFragmentModule

@Module
abstract class EnhanceImageModule {

    @Binds
    abstract fun activity(activity: EnhanceImageActivity): AppCompatActivity

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
    @ContributesAndroidInjector(modules = [EnhanceImageFragmentModule::class])
    abstract fun selectedImageScreen(): EnhanceImageFragment
}
