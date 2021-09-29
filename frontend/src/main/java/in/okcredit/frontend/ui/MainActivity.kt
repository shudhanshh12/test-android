package `in`.okcredit.frontend.ui

import `in`.okcredit.collection.contract.MerchantDestinationListener
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.contract.FrontendConstants
import `in`.okcredit.frontend.ui.expense_manager.ExpenseManagerFragment
import `in`.okcredit.frontend.ui.sync.SyncContract.Companion.KEY_SKIP_SELECT_BUSINESS_SCREEN
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerFragment
import `in`.okcredit.merchant.device.oreo
import `in`.okcredit.merchant.profile.BusinessFragment
import `in`.okcredit.onboarding.contract.OnboardingConstants
import `in`.okcredit.onboarding.language.LanguageSelectionActivity
import `in`.okcredit.onboarding.otp_verification.OtpArgs.ARG_FLAG
import `in`.okcredit.onboarding.social_validation.SocialValidationActivity
import `in`.okcredit.sales_ui.ui.list_sales.SalesOnCashFragment
import `in`.okcredit.shared.base.BaseScreen
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.BaseContextWrappingDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import tech.okcredit.android.base.extensions.className
import tech.okcredit.android.base.extensions.updateLanguage
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.app_contract.AppConstants
import tech.okcredit.help.HelpActivity
import tech.okcredit.home.ui.activity.HomeActivity
import timber.log.Timber
import javax.inject.Inject

@Deprecated(message = "Please NavigationActivity instead")
class MainActivity : AppCompatActivity(), HasAndroidInjector, MerchantDestinationListener {

    companion object {

        const val CUSTOMER_PROFILE_ACTIVITY_RESULT_CODE = 23232
        const val SUPPLIER_PROFILE_ACTIVITY_RESULT_CODE = 42342

        const val ARG_ADOPTION_TITLE = "adoption_title"
        const val TEMP_NEW_NUMBER = "temp_new_number"
        const val ARG_SCREEN = AppConstants.ARG_SCREEN
        const val ARG_CUSTOMER_ID = "customer_id"
        const val ARG_SUPPLIER_ID = "supplier_id"
        const val ARG_COLLECTION_ID = "collection_id"
        const val ARG_TRANSACTION_ID = "transaction_id"
        const val ARG_TX_TYPE = "transaction_type"
        const val ARG_TX_AMOUNT = "transaction_amount"
        const val ARG_TXN_ID = "txn_id"
        const val ARG_SOURCE = "source"
        const val ARG_REFERRAL_TARGETS = "referral targets"

        const val ARG_CUSTOMER_NAME = "customer_name"
        const val REACTIVATE = "reactivate"
        const val NAME = "name"
        const val FLOW = "flow"
        const val ARG_SIGN_OUT_ALL_DEVICES = "arg_sign_out_from_all_devices"
        const val ARG_ID = "id"
        const val ARG_ACCOUNT_TYPE = "account_type"
        const val ARG_REWARDS_AMOUNT = "rewards_amount"
        const val ARG_REDIRECT_TO_REWARDS_PAGE = "redirect_to_rewards_page"
        const val ARG_ACTION = "arg_action"
        const val HELP_ITEM_ID = "help_item_id"
        const val HELP_ID = "help_id"
        const val ARG_REQUEST_CODE = "arg_request_code"
        const val ARG_PAYMENT_METHOD_TYPE = "arg_payment_method_type"
        const val ARG_ADD_TRANSACTION_SHORTCUT_SOURCE = "add_transaction_shortcut_source"

        const val HOME_ACTIVITY = 0
        const val CUSTOMER_SCREEN = 1
        const val PASSWORD_ENABLE_SCREEN = 2
        const val ADD_TXN_SCREEN = 4
        const val PRIVACY_SCREEN = 7
        const val TRANSACTION_DETAIL_SCREEN = 10

        const val ENTER_MOBILE_PAGE = 16
        const val ENTER_OTP_PAGE = 17

        const val SUPPLIER_TRANSACTION_DETAIL_SCREEN = 23
        const val REWARDS_SCREEN = 27
        const val OTP_VERIFICATION_SCREEN = 28
        const val DUE_CUSTOMER_SCREEN = 29

        const val INFO_CHANGE_NUMBER = 33
        const val CHANGE_NUMBER = 34
        const val PHONE_NUMBER_CHANGE_CONFIRMATION = 35
        const val MERCHANT_SCREEN = 36
        const val MERCHANT_INPUT_SCREEN = 37
        const val SYNC_SCREEN = 38

        const val APP_LOCK = 42
        const val ONBOARDING_BUSINESS_NAME = 44

        const val LIVE_SALE_SCREEN = 51
        const val KNOW_MORE_SCREEN = 52
        const val HELP_V2 = 53
        const val HELP_MAIN = 90
        const val HELP__ITEM_SCREEN = 55

        const val CATEGORY_SCREEN = 62
        const val MOVE_TO_SUPPLIER_SCREEN = 63
        const val DISCOUNT_DETAIL_SCREEN = 64
        const val EXPENSE_MANAGER = 65
        const val ADD_DISCOUNT_SCREEN = 66
        const val ADD_EXPENSE = 67

        const val SALES_ON_CASH = 70
        const val ADD_SALE = 74
        const val FEEDBACK_SCREEN = 82

        const val SINGLE_LIST_CUSTOMER_DESTINATION_SCREEN = 85
        const val ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN = 89

        @JvmStatic
        fun startingIntent(context: Context, customerId: String, screen: Int): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_CUSTOMER_ID, customerId)
                .putExtra(ARG_SCREEN, screen)
        }

        @JvmStatic
        fun startCustomerIntent(
            context: Context,
            customerId: String,
            screen: Int,
            source: String,
        ): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_CUSTOMER_ID, customerId)
                .putExtra(ARG_SCREEN, screen)
                .putExtra(ARG_SOURCE, source)
        }

        @JvmStatic
        fun startingIntentForTransactionDetailsFragment(context: Context, txId: String): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_TRANSACTION_ID, txId)
                .putExtra(ARG_SCREEN, TRANSACTION_DETAIL_SCREEN)
        }

        @JvmStatic
        fun startingIntentForWelcomeLanguageSelectionScreen(context: Context): Intent {
            return LanguageSelectionActivity.getIntent(context)
        }

        @JvmStatic
        fun startingIntentForWelcomeSocialValidationScreen(context: Context): Intent {
            return SocialValidationActivity.getIntent(context)
        }

        @JvmStatic
        fun startingIntentForEnterMobileScreen(context: Context, flow: String, mobileNumber: String): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, ENTER_MOBILE_PAGE)
                .putExtra(FLOW, flow)
                .putExtra(OnboardingConstants.ARG_MOBILE, mobileNumber)
        }

        @JvmStatic
        fun startingIntentForAuthFailure(context: Context, flag: Int): Intent {
            return startingIntentForWelcomeLanguageSelectionScreen(context)
        }

        @JvmStatic
        fun startingIntentForEnterOtpScreen(
            context: Context,
            mobile: String,
            flag: Int,
            signOutAllDevices: Boolean,
            isGooglePopupSelected: Boolean = false,
        ): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, ENTER_OTP_PAGE)
                .putExtra(ARG_FLAG, flag)
                .putExtra(OnboardingConstants.ARG_MOBILE, mobile)
                .putExtra(OnboardingConstants.ARG_GOOGLE_AUTO_READ_MOBILE_NUMBER, isGooglePopupSelected)
                .putExtra(ARG_SIGN_OUT_ALL_DEVICES, signOutAllDevices)
        }

        @JvmStatic
        fun startingIntentForSupplierTransactionDetailsScreen(context: Context, txId: String): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_TRANSACTION_ID, txId)
                .putExtra(ARG_SCREEN, SUPPLIER_TRANSACTION_DETAIL_SCREEN)
        }

        @JvmStatic
        fun startingCustomerScreenForReactivation(
            context: Context,
            customerId: String,
            customerName: String? = null,
        ): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_CUSTOMER_ID, customerId)
                .putExtra(NAME, customerName)
                .putExtra(ARG_SCREEN, CUSTOMER_SCREEN)
                .putExtra(REACTIVATE, true)
        }

        @JvmStatic
        fun startingIntentForRewardsScreen(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, REWARDS_SCREEN)
        }

        @JvmStatic
        fun startingIntentForOtpVerificationScreen(context: Context, flag: Int): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, OTP_VERIFICATION_SCREEN)
                .putExtra(ARG_FLAG, flag)
        }

        @JvmStatic
        fun startingIntentForDueCustomerScreen(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, DUE_CUSTOMER_SCREEN)
        }

        @JvmStatic
        fun startNumberChangeScreen(context: @NotNull Context): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, INFO_CHANGE_NUMBER)
        }

        @JvmStatic
        fun startingIntentForChangeNumberScreen(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, CHANGE_NUMBER)
        }

        @JvmStatic
        fun startingIntentForMerchantScreen(
            context: Context,
            setupProfile: Boolean = false,
            shareBusinessCard: Boolean = false,
        ): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, MERCHANT_SCREEN)
                .putExtra(BusinessFragment.ARG_SETUP_PROFILE, setupProfile)
                .putExtra(BusinessFragment.ARG_SHARE_BUSINESS_CARD, shareBusinessCard)
        }

        @JvmStatic
        fun startingIntentForSyncScreen(context: Context, skipSelectBusinessScreen: Boolean): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, SYNC_SCREEN)
                .putExtra(KEY_SKIP_SELECT_BUSINESS_SCREEN, skipSelectBusinessScreen)
        }

        @JvmStatic
        fun startingIntentForAccountStatementScreen(context: Context, source: String): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, FrontendConstants.ACCOUNT_STATEMENT_SCREEN)
                .putExtra(ARG_SOURCE, source)
        }

        @JvmStatic
        fun startingIntentForKnowMore(
            context: @NotNull Context,
            id: @NotNull String,
            accountType: @NotNull String,
        ): @NotNull Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, KNOW_MORE_SCREEN)
                .putExtra(ARG_ID, id)
                .putExtra(ARG_ACCOUNT_TYPE, accountType)
        }

        @JvmStatic
        fun startingIntentForOnboardingBusinessName(context: @NotNull Context): @NotNull Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, ONBOARDING_BUSINESS_NAME)
        }

        @JvmStatic
        @Deprecated(message = "Use HelpActivity instead")
        fun startingIntentForHelpV2Screen(
            context: @NotNull Context,
            helpIds: List<String>,
            source: String,
        ): @NotNull Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, HELP_MAIN)
                .putExtra(HelpActivity.EXTRA_SOURCE, source)
                .putStringArrayListExtra(HelpActivity.HELP_ID, ArrayList(helpIds))
        }

        @JvmStatic
        fun startingIntentForFeedBackScreen(context: Context): @NotNull Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, FEEDBACK_SCREEN)
        }

        @JvmStatic
        fun startingIntentForCategoryScreen(context: @NotNull Context): @NotNull Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, CATEGORY_SCREEN)
        }

        @JvmStatic
        fun startingIntentForMoveToSupplierScreen(context: @NotNull Context, customerId: String): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, MOVE_TO_SUPPLIER_SCREEN)
                .putExtra(ARG_CUSTOMER_ID, customerId)
        }

        @JvmStatic
        fun startingIntentForDiscountScreen(context: @NotNull Context, customerId: @Nullable String): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, ADD_DISCOUNT_SCREEN)
                .putExtra(ARG_CUSTOMER_ID, customerId)
        }

        @JvmStatic
        fun startingIntentForDiscountDetailsScreen(context: @NotNull Context, txnId: @NotNull String): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_TRANSACTION_ID, txnId)
                .putExtra(ARG_SCREEN, DISCOUNT_DETAIL_SCREEN)
        }

        @JvmStatic
        fun startingIntentForPhoneNumberChangeConfirmationScreen(context: Context, newNumber: String): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, PHONE_NUMBER_CHANGE_CONFIRMATION)
                .putExtra(TEMP_NEW_NUMBER, newNumber)
        }

        @JvmStatic
        fun startingIntentForSingleListCustomerDestinationScreen(
            context: Context,
            requestCode: Int,
            customerId: String,
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(ARG_REQUEST_CODE, requestCode)
                putExtra(ARG_CUSTOMER_ID, customerId)
                putExtra(ARG_SCREEN, SINGLE_LIST_CUSTOMER_DESTINATION_SCREEN)
            }
        }

        @JvmStatic
        fun startingIntentForHelpHomeScreen(context: Context, helpId: List<String>, source: String): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_SCREEN, HELP_V2)
                .putExtra(ARG_SOURCE, source)
                .putStringArrayListExtra(HELP_ID, ArrayList(helpId))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        startFragment()
    }

    private fun startFragment() {
        var finalHost: NavHostFragment = NavHostFragment.create(R.navigation.customer_flow_v2)
        when (intent.getIntExtra(ARG_SCREEN, HOME_ACTIVITY)) {
            HOME_ACTIVITY -> {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finishAffinity()
                return
            }
            CUSTOMER_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.customer_flow_v2)
            }
            ADD_TXN_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.add_txn_flow)
            }
            LIVE_SALE_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.live_sales_flow)
            }
            PASSWORD_ENABLE_SCREEN -> finalHost = NavHostFragment.create(R.navigation.password_enable_flow)
            PRIVACY_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.privacy_flow)
            }
            FrontendConstants.ACCOUNT_STATEMENT_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.account_statement_flow)
            }
            TRANSACTION_DETAIL_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.transaction_details_flow)
            }
            // Should not be emitted anymore as Auto-lang has it on the initial screen
            ENTER_MOBILE_PAGE -> {
                finalHost = NavHostFragment.create(R.navigation.auto_lang_flow)
            }
            ENTER_OTP_PAGE -> {
                finalHost = NavHostFragment.create(R.navigation.enter_otp_flow)
            }
            SUPPLIER_TRANSACTION_DETAIL_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.supplier_transaction_details_flow)
            }
            REWARDS_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.rewards_flow)
            }
            OTP_VERIFICATION_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.otp_verification_flow)
            }
            DUE_CUSTOMER_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.due_cutomer_flow)
            }

            INFO_CHANGE_NUMBER -> {
                finalHost = NavHostFragment.create(R.navigation.number_change_flow)
            }
            CHANGE_NUMBER -> {
                finalHost = NavHostFragment.create(R.navigation.change_number_flow)
            }
            PHONE_NUMBER_CHANGE_CONFIRMATION -> {
                finalHost = NavHostFragment.create(R.navigation.confirm_phone_number_change_flow)
            }
            MERCHANT_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.merchant_flow)
            }
            MERCHANT_INPUT_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.merchant_input_flow)
            }
            SYNC_SCREEN -> {
                finalHost = NavHostFragment.create(
                    R.navigation.sync_screen_flow,
                    bundleOf(
                        KEY_SKIP_SELECT_BUSINESS_SCREEN to intent.getBooleanExtra(
                            KEY_SKIP_SELECT_BUSINESS_SCREEN,
                            false
                        )
                    )
                )
            }

            APP_LOCK -> {
                finalHost = NavHostFragment.create(R.navigation.applock_flow)
            }
            KNOW_MORE_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.know_more_flow)
            }

            ONBOARDING_BUSINESS_NAME -> {
                finalHost = NavHostFragment.create(R.navigation.onboarding_business_name_flow)
            }

            HELP_V2 -> {
                finalHost = NavHostFragment.create(R.navigation.help_home_flow)
            }

            HELP_MAIN -> {
                finalHost = NavHostFragment.create(R.navigation.help_flow)
            }

            HELP__ITEM_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.helpdetails_flow, intent.extras)
            }

            CATEGORY_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.categoryscreen_flow)
            }
            MOVE_TO_SUPPLIER_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.move_to_supplier_flow)
            }

            ADD_DISCOUNT_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.add_discount_flow)
            }
            DISCOUNT_DETAIL_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.discount_details_flow)
            }
            EXPENSE_MANAGER -> {
                finalHost = NavHostFragment.create(R.navigation.expensemanager_flow)
            }
            ADD_EXPENSE -> {
                finalHost = NavHostFragment.create(
                    R.navigation.expensemanager_flow,
                    bundleOf(ExpenseManagerFragment.NAVIGATE_TO_ADD_EXPENSE to true)
                )
            }
            ADD_SALE -> {
                finalHost = NavHostFragment.create(
                    R.navigation.sales_on_cash_flow,
                    bundleOf(SalesOnCashFragment.NAVIGATE_TO_ADD_SALE to true)
                )
            }
            SALES_ON_CASH -> {
                finalHost = NavHostFragment.create(R.navigation.sales_on_cash_flow)
            }
            FEEDBACK_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.feedback_flow)
            }
            SINGLE_LIST_CUSTOMER_DESTINATION_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.confirm_single_list_customer_destination_screen_flow)
            }
        }
        if (isFinishing) return
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_content, finalHost)
            .setPrimaryNavigationFragment(finalHost) // this is the equivalent to app:defaultNavHost="true"
            .commit()

        disableAutoFill()
        Timber.d("$className initialized")
    }

    private fun disableAutoFill() {
        try {
            oreo {
                window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            }
        } catch (e: Exception) {
            // Add ExceptionUtils.logException here
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        updateLanguage(LocaleManager.getLanguage(base))
    }

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_content)
        if (navHostFragment?.childFragmentManager != null) {
            val fr: Fragment = navHostFragment.childFragmentManager.fragments[0]
            if (fr is BaseScreen<*>) {
                if (!fr.onBackPressed()) {
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == CUSTOMER_PROFILE_ACTIVITY_RESULT_CODE) {
            val finalHost = NavHostFragment.create(R.navigation.customer_flow_v2)
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_content, finalHost)
                .setPrimaryNavigationFragment(finalHost) // this is the equivalent to app:defaultNavHost="true"
                .commit()
            return
        } else if (resultCode == SUPPLIER_PROFILE_ACTIVITY_RESULT_CODE) {
            val finalHost: NavHostFragment = NavHostFragment.create(R.navigation.supplier_flow)
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_content, finalHost)
                .setPrimaryNavigationFragment(finalHost) // this is the equivalent to app:defaultNavHost="true"
                .commit()
            return
        }

        lifecycleScope.launchWhenResumed {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_content)
            if (navHostFragment?.childFragmentManager != null) {
                val fr: Fragment = navHostFragment.childFragmentManager.fragments[0]
                fr.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(this, R.id.main_content).navigateUp()
    }

    /****************************************************************
     * Dependency Injection
     ****************************************************************/
    // provides injector for all fragments inside this activity

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private var baseContextWrappingDelegate: AppCompatDelegate? = null

    override fun getDelegate() =
        baseContextWrappingDelegate ?: BaseContextWrappingDelegate(super.getDelegate())
            .apply {
                baseContextWrappingDelegate = this
            }

    override fun onAccountAddedSuccessfully(eta: Long) {
        val navHostFragment = supportFragmentManager.primaryNavigationFragment as NavHostFragment?
        when (val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment) {
            is CustomerFragment -> {
                currentFragment.onMerchantDestinationAdded()
            }
        }
    }

    override fun onCancelled() {}
}
