package `in`.okcredit.ui.supplier_profile._di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.ui.supplier_profile.SupplierProfileActivity
import `in`.okcredit.ui.supplier_profile.SupplierProfileContract
import `in`.okcredit.ui.supplier_profile.SupplierProfilePresenter
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.ActivityScope

@Module
abstract class SupplierProfileActivityModule {

    @Binds
    @ActivityScope
    abstract fun presenter(presenter: SupplierProfilePresenter): SupplierProfileContract.Presenter

    companion object {

        @Provides
        @ActivityScope
        @ViewModelParam(MainActivity.ARG_SUPPLIER_ID)
        fun customerId(activity: SupplierProfileActivity): String {
            return activity.intent.getStringExtra(SupplierProfileActivity.EXTRA_SUPPLIER_ID)
        }

        @Provides
        @ActivityScope
        @ViewModelParam("is_edit_mobile")
        fun isEditMobile(activity: SupplierProfileActivity): Boolean {
            return activity.intent.getBooleanExtra(SupplierProfileActivity.IS_EDIT_MOBILE, false)
        }
    }
}
