package `in`.okcredit.merchant.customer_ui.ui.staff_link.di

import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogScreen
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog._di.AddNumberDialogModule
import `in`.okcredit.merchant.customer_ui.ui.address.UpdateCustomerAddressBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.address.UpdateCustomerAddressModule
import `in`.okcredit.merchant.customer_ui.ui.delete.DeleteItemBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.staff_link.StaffLinkActivity
import `in`.okcredit.merchant.customer_ui.ui.staff_link.StaffLinkContract
import `in`.okcredit.merchant.customer_ui.ui.staff_link.StaffLinkViewModel
import `in`.okcredit.merchant.customer_ui.ui.staff_link.add.StaffLinkAddCustomerFragment
import `in`.okcredit.merchant.customer_ui.ui.staff_link.add.StaffLinkAddCustomerModule
import `in`.okcredit.merchant.customer_ui.ui.staff_link.edit.StaffLinkEditDetailsFragment
import `in`.okcredit.merchant.customer_ui.ui.staff_link.edit.StaffLinkEditDetailsModule
import `in`.okcredit.merchant.customer_ui.ui.staff_link.education.StaffLinkEducationFragment
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class StaffLinkActivityModule {

    @ContributesAndroidInjector(modules = [StaffLinkAddCustomerModule::class])
    abstract fun staffLinkAddCustomerFragment(): StaffLinkAddCustomerFragment

    @ContributesAndroidInjector(modules = [StaffLinkEditDetailsModule::class])
    abstract fun staffLinkEditDetailsFragment(): StaffLinkEditDetailsFragment

    @ContributesAndroidInjector(modules = [AddNumberDialogModule::class])
    abstract fun addMobileNumberDialog(): AddNumberDialogScreen

    @ContributesAndroidInjector(modules = [UpdateCustomerAddressModule::class])
    abstract fun updateCustomerAddressBottomSheet(): UpdateCustomerAddressBottomSheet

    @ContributesAndroidInjector
    abstract fun deleteSubscriptionBottomSheet(): DeleteItemBottomSheet

    @ContributesAndroidInjector
    abstract fun staffLinkEducationFragment(): StaffLinkEducationFragment

    companion object {

        @Provides
        fun initialState(): StaffLinkContract.State = StaffLinkContract.State()

        @Provides
        fun viewModel(
            fragment: StaffLinkActivity,
            viewModelProvider: Provider<StaffLinkViewModel>
        ): MviViewModel<StaffLinkContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
