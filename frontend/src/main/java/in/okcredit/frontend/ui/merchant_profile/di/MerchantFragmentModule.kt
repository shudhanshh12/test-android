package `in`.okcredit.frontend.ui.merchant_profile.di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.merchant.profile.BusinessContract
import `in`.okcredit.merchant.profile.BusinessFragment
import `in`.okcredit.merchant.profile.BusinessViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class MerchantFragmentModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: BusinessFragment): BusinessContract.Navigator

    companion object {

        @Provides
        fun initialState(): BusinessContract.State = BusinessContract.State()

        @Provides
        @ViewModelParam(BusinessFragment.ARG_SETUP_PROFILE)
        fun setupProfile(activity: MainActivity): Boolean {
            return activity.intent.getBooleanExtra(BusinessFragment.ARG_SETUP_PROFILE, false)
        }

        @Provides
        @ViewModelParam(BusinessFragment.ARG_SHARE_BUSINESS_CARD)
        fun shareBusinessCard(activity: MainActivity): Boolean {
            return activity.intent.getBooleanExtra(BusinessFragment.ARG_SHARE_BUSINESS_CARD, false)
        }

        @Provides
        @ViewModelParam(BusinessFragment.ARG_SHOW_MERCHANT_PROFILE)
        fun showMerchantProfileImage(activity: MainActivity): Boolean {
            return activity.intent.getBooleanExtra(BusinessFragment.ARG_SHOW_MERCHANT_PROFILE, false)
        }

        @Provides
        @ViewModelParam(BusinessFragment.ARG_SHOW_MERCHANT_LOCATION)
        fun showMerchantLocation(activity: MainActivity): Boolean {
            return activity.intent.getBooleanExtra(BusinessFragment.ARG_SHOW_MERCHANT_LOCATION, false)
        }

        @Provides
        @ViewModelParam(BusinessFragment.ARG_SHOW_BUSINESS_TYPE_BOTTOM_SHEET)
        fun showBusinessTypeBottomSheet(activity: MainActivity): Boolean {
            return activity.intent.getBooleanExtra(BusinessFragment.ARG_SHOW_BUSINESS_TYPE_BOTTOM_SHEET, false)
        }

        @Provides
        @ViewModelParam(BusinessFragment.ARG_SHOW_CATEGORY_SCREEN)
        fun showCategoryScreen(activity: MainActivity): Boolean {
            return activity.intent.getBooleanExtra(BusinessFragment.ARG_SHOW_CATEGORY_SCREEN, false)
        }

        @Provides
        fun viewModel(
            fragment: BusinessFragment,
            viewModelProvider: Provider<BusinessViewModel>
        ): MviViewModel<BusinessContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
