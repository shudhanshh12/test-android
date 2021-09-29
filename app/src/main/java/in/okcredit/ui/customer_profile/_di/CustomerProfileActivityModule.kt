package `in`.okcredit.ui.customer_profile._di

import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogScreen
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog._di.AddNumberDialogModule
import `in`.okcredit.ui.customer_profile.CustomerProfileActivity
import `in`.okcredit.ui.customer_profile.CustomerProfileContract
import `in`.okcredit.ui.customer_profile.CustomerProfilePresenter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import tech.okcredit.base.dagger.di.UiThread
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.ActivityScope
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class CustomerProfileActivityModule {

    @Binds
    @ActivityScope
    abstract fun viewModel(viewModel: CustomerProfilePresenter): CustomerProfileContract.Presenter

    @FragmentScope
    @ContributesAndroidInjector(modules = [AddNumberDialogModule::class])
    abstract fun addMobileNumberDialog(): AddNumberDialogScreen

    companion object {

        @Provides
        @ActivityScope
        @ViewModelParam(CustomerProfileActivity.EXTRA_CUSTOMER_ID)
        fun customerId(activity: CustomerProfileActivity): String {
            return activity.intent.getStringExtra(CustomerProfileActivity.EXTRA_CUSTOMER_ID)
        }

        @Provides
        @ActivityScope
        @ViewModelParam(CustomerProfileActivity.IS_EDIT_MOBILE)
        fun isEditMobile(activity: CustomerProfileActivity): Boolean {
            return activity.intent.getBooleanExtra(CustomerProfileActivity.IS_EDIT_MOBILE, false)
        }

        @Provides
        @ActivityScope
        fun stateScheduler(): Scheduler {
            return Schedulers.newThread()
        }

        @Provides
        @ActivityScope
        fun userIntentScheduler(): Scheduler {
            return Schedulers.newThread()
        }

        @Provides
        @ActivityScope
        @UiThread
        fun uiScheduler(): Scheduler {
            return AndroidSchedulers.mainThread()
        }
    }
}
