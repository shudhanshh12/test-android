package `in`.okcredit.ui.delete_customer._di

import `in`.okcredit.ui.delete_customer.DeleteCustomerActivity
import `in`.okcredit.ui.delete_customer.DeleteCustomerContract
import `in`.okcredit.ui.delete_customer.DeleteCustomerPresenter
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.ActivityScope

@Module
abstract class DeleteCustomerActivityModule {

    @Binds
    @ActivityScope
    abstract fun viewModel(viewModel: DeleteCustomerPresenter): DeleteCustomerContract.Presenter

    companion object {

        @Provides
        @ActivityScope
        @ViewModelParam("customer_id")
        fun customerId(activity: DeleteCustomerActivity): String =
            activity.intent.getStringExtra(DeleteCustomerActivity.EXTRA_CUSTOMER_ID)
    }
}
