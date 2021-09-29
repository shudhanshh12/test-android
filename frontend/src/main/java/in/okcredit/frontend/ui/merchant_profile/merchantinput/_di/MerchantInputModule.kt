package `in`.okcredit.frontend.ui.merchant_profile.merchantinput._di

import `in`.okcredit.frontend.ui.MainActivityTranslucentFullScreen
import `in`.okcredit.frontend.ui.merchant_profile.merchantinput.MerchantInputContract
import `in`.okcredit.frontend.ui.merchant_profile.merchantinput.MerchantInputFragment
import `in`.okcredit.frontend.ui.merchant_profile.merchantinput.MerchantInputViewModel
import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class MerchantInputModule {

    companion object {

        @Provides
        fun initialState(): MerchantInputContract.State = MerchantInputContract.State()

        @Provides
        @ViewModelParam(MainActivityTranslucentFullScreen.ARG_MERCHANT_INPUT_TYPE)
        fun inputType(activity: MainActivityTranslucentFullScreen): Int {
            return activity.intent.getIntExtra(
                MainActivityTranslucentFullScreen.ARG_MERCHANT_INPUT_TYPE,
                BusinessConstants.NONE
            )
        }

        @Provides
        @ViewModelParam(MainActivityTranslucentFullScreen.ARG_MERCHANT_INPUT_TITLE)
        fun inputTitle(activity: MainActivityTranslucentFullScreen): String {
            return activity.intent.getStringExtra(MainActivityTranslucentFullScreen.ARG_MERCHANT_INPUT_TITLE)
        }

        @Provides
        @ViewModelParam(MainActivityTranslucentFullScreen.ARG_MERCHANT_INPUT_VALUE)
        fun inputValue(activity: MainActivityTranslucentFullScreen): String {
            return activity.intent.getStringExtra(MainActivityTranslucentFullScreen.ARG_MERCHANT_INPUT_VALUE)
                ?.takeUnless { it.isBlank() }
                ?: ""
        }

        @Provides
        @ViewModelParam(MainActivityTranslucentFullScreen.ARG_CATEGORY_ID)
        fun selectedCategoryId(activity: MainActivityTranslucentFullScreen): String {
            return activity.intent.getStringExtra(MainActivityTranslucentFullScreen.ARG_CATEGORY_ID)
        }

        @Provides
        @ViewModelParam(MainActivityTranslucentFullScreen.ARG_LATITUDE)
        fun latitude(activity: MainActivityTranslucentFullScreen): Double {
            return activity.intent.getDoubleExtra(MainActivityTranslucentFullScreen.ARG_LATITUDE, 0.0)
        }

        @Provides
        @ViewModelParam(MainActivityTranslucentFullScreen.ARG_LONGITUDE)
        fun longitude(activity: MainActivityTranslucentFullScreen): Double {
            return activity.intent.getDoubleExtra(MainActivityTranslucentFullScreen.ARG_LONGITUDE, 0.0)
        }

        @Provides
        @ViewModelParam(MainActivityTranslucentFullScreen.ARG_GPS)
        fun gps(activity: MainActivityTranslucentFullScreen): Boolean {
            return activity.intent.getBooleanExtra(MainActivityTranslucentFullScreen.ARG_GPS, false)
        }

        @Provides
        @ViewModelParam(MainActivityTranslucentFullScreen.ARG_IS_SOURCE_IN_APP_NOTIFICATION)
        fun isSourceInAppNotification(activity: MainActivityTranslucentFullScreen): Boolean {
            return activity.intent.getBooleanExtra(
                MainActivityTranslucentFullScreen.ARG_IS_SOURCE_IN_APP_NOTIFICATION,
                false
            )
        }

        @Provides
        fun viewModel(
            fragment: MerchantInputFragment,
            viewModelProvider: Provider<MerchantInputViewModel>
        ): MviViewModel<MerchantInputContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
