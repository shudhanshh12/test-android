package `in`.okcredit.notification

import `in`.okcredit.backend.contract.DeepLinkUrl
import `in`.okcredit.collection_ui.ui.defaulters.DefaulterListActivity
import `in`.okcredit.collection_ui.ui.home.CollectionsHomeActivity
import `in`.okcredit.collection_ui.ui.home.adoption.CollectionAdoptionV2Fragment
import `in`.okcredit.collection_ui.ui.referral.TargetedReferralActivity
import `in`.okcredit.customer.contract.RelationshipType
import `in`.okcredit.frontend.contract.FrontendConstants
import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.SupplierActivity
import `in`.okcredit.frontend.ui.supplier.SupplierFragment
import `in`.okcredit.merchant.customer_ui.addrelationship.AddRelationshipActivity
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Activity
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerFragment
import `in`.okcredit.merchant.customer_ui.ui.staff_link.StaffLinkActivity
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerStatement
import `in`.okcredit.merchant.profile.BusinessFragment
import `in`.okcredit.merchant.rewards.ui.RewardsActivity
import `in`.okcredit.navigation.NavigationActivity
import `in`.okcredit.notification.DeepLinkActivity.ACTION_DEEP_LINK_COMPLEX
import `in`.okcredit.payment.PspUpiActivity
import `in`.okcredit.sales_ui.SalesActivity
import `in`.okcredit.ui.app_lock.preference.AppLockPrefActivity
import `in`.okcredit.ui.customer_profile.CustomerProfileActivity
import `in`.okcredit.ui.delete_customer.DeleteCustomerActivity
import `in`.okcredit.ui.delete_txn.supplier.supplier.DeleteSupplierActivity
import `in`.okcredit.ui.language.InAppLanguageActivity
import `in`.okcredit.ui.supplier_profile.SupplierProfileActivity
import `in`.okcredit.user_migration.presentation.ui.UserMigrationActivity
import `in`.okcredit.voice_first.ui.bulk_add.BulkAddTransactionsActivity
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionActivity
import `in`.okcredit.web_features.cash_counter.CashCounterActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.airbnb.deeplinkdispatch.DeepLink
import merchant.okcredit.gamification.ipl.game.ui.GameActivity
import merchant.okcredit.gamification.ipl.view.IplActivity
import tech.okcredit.account_chat_contract.CHAT_INTENT_EXTRAS
import tech.okcredit.account_chat_ui.chat_activity.ChatActivity
import tech.okcredit.android.referral.ui.ReferralActivity
import tech.okcredit.android.referral.ui.share.ShareActivity
import tech.okcredit.bill_management_ui.BillActivity
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.contacts.ui.AddOkcreditContactTransparentActivity
import tech.okcredit.help.helpcontactus.HelpContactUsFragment
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileTransparentActivity
import tech.okcredit.home.ui.acccountV2.ui.AccountActivity
import tech.okcredit.home.ui.activity.HomeActivity.Companion.getIntent
import tech.okcredit.home.ui.add_transaction_home_search.AddTransactionShortcutSearchFragment.Companion.ARG_ADD_TRANSACTION_SHORTCUT_SOURCE
import tech.okcredit.home.ui.add_transaction_home_search.AddTxnShortcutSearchActivity
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardActivity
import tech.okcredit.home.ui.settings.SettingsActivity
import tech.okcredit.web.ui.WebViewActivity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class ResolveIntentsFromDeeplink @Inject constructor(
    private val context: Context,
) {
    companion object {
        const val ARG_SOURCE = "deep_link"
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val intents = mutableListOf<Intent>()

    fun execute(deepLinkUrl: String, extras: Bundle): List<Intent> {
        when {

            deepLinkUrl == DeepLinkUrl.IPL_SCREEN || deepLinkUrl == DeepLinkUrl.IPL_SCREEN_V2 -> intents.add(
                IplActivity.getIntent(
                    context
                )
            )

            deepLinkUrl == DeepLinkUrl.IPL_WEEKLY_DRAW || deepLinkUrl == DeepLinkUrl.IPL_WEEKLY_DRAW_V2 -> intents.add(
                IplActivity.getIntentForWeeklyDraw(context)
            )

            deepLinkUrl == DeepLinkUrl.IPL_LEADER_BOARD || deepLinkUrl == DeepLinkUrl.IPL_LEADER_BOARD_V2 -> intents.add(
                IplActivity.getIntentForLeaderboard(context)
            )

            deepLinkUrl.contains("/ipl2021/game/") -> intents.add(
                GameActivity.getIntent(
                    context,
                    extras.getString(GameActivity.MATCH_ID)!!
                )
            )

            deepLinkUrl == DeepLinkUrl.ACCOUNT -> {
                intents.add(
                    Intent(context, AccountActivity::class.java)
                        .setAction(ACTION_DEEP_LINK_COMPLEX)
                )
            }
            deepLinkUrl == DeepLinkUrl.USER_MIGRATION_UPLOAD_PDF || deepLinkUrl == DeepLinkUrl.USER_MIGRATION_UPLOAD_PDF_V2 -> intents.add(
                UserMigrationActivity.getIntent(context)
            )

            deepLinkUrl.contains("/save_okcredit_contact/") -> {
                intents.add(
                    AddOkcreditContactTransparentActivity.getIntent(
                        context,
                        extras.getString(AddOkcreditContactTransparentActivity.CONTACT_NAME),
                        extras.getString(AddOkcreditContactTransparentActivity.PHONE_NUMBER)
                    ).setAction(ACTION_DEEP_LINK_COMPLEX)
                )
            }

            else -> {
                when {
                    deepLinkUrl == DeepLinkUrl.REFERRAL -> {
                        intents.add(ReferralActivity.getIntent(context))
                    }
                    deepLinkUrl == DeepLinkUrl.BUSINESS_HEALTH_DASHBOARD -> {
                        intents.add(
                            BusinessHealthDashboardActivity.getIntent(context)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.HELP_NEW -> {
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(
                                    MainActivity.ARG_SCREEN,
                                    MainActivity.HELP_V2
                                )
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.MANUAL_CHAT -> {
                        intents.add(HelpContactUsFragment.getManualChatIntent(context))
                    }
                    deepLinkUrl == DeepLinkUrl.CATEGORIES -> {
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(
                                    MainActivity.ARG_SCREEN,
                                    MainActivity.CATEGORY_SCREEN
                                )
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.BACKUP -> {
                        intents.add(
                            Intent(context, AccountActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(AccountActivity.ARG_NOTIFICATION_URL, extras.getString(DeepLink.URI))
                        )
                    }
                    deepLinkUrl.contains(DeepLinkUrl.ACCOUNT_STATEMENT) -> {
                        intents.add(
                            Intent(context, AccountActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .putExtra(MainActivity.ARG_CUSTOMER_ID, "")
                                .putExtras(extras)
                                .putExtra(
                                    MainActivity.ARG_SCREEN,
                                    FrontendConstants.ACCOUNT_STATEMENT_SCREEN
                                )
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.SECURITY -> {
                        intents.add(
                            Intent(context, AccountActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                        intents.add(
                            Intent(context, SettingsActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.APP_LOCK -> {
                        intents.add(
                            Intent(context, AccountActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                        intents.add(
                            Intent(context, SettingsActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                        intents.add(
                            Intent(context, AppLockPrefActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.PAYMENT_PASSWORD -> {
                        intents.add(
                            Intent(context, AccountActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                        intents.add(
                            Intent(context, SettingsActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .putExtra(
                                    MainActivity.ARG_SCREEN,
                                    MainActivity.PASSWORD_ENABLE_SCREEN
                                )
                                .putExtra(MainActivity.ARG_CUSTOMER_ID, "")
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.PROFILE -> {
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(
                                    MainActivity.ARG_SCREEN,
                                    MainActivity.MERCHANT_SCREEN
                                )
                                .putExtra(BusinessFragment.ARG_SETUP_PROFILE, true)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.BUSINESS_CARD -> {
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(
                                    MainActivity.ARG_SCREEN,
                                    MainActivity.MERCHANT_SCREEN
                                )
                                .putExtra(BusinessFragment.ARG_SHARE_BUSINESS_CARD, true)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.BUSINESS_LOCATION -> {
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(MainActivity.ARG_SCREEN, MainActivity.MERCHANT_SCREEN)
                                .putExtra(BusinessFragment.ARG_SHOW_MERCHANT_LOCATION, true)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.BUSINESS_CATEGORY -> {
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(MainActivity.ARG_SCREEN, MainActivity.MERCHANT_SCREEN)
                                .putExtra(BusinessFragment.ARG_SHOW_CATEGORY_SCREEN, true)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.BUSINESS_TYPE -> {
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(MainActivity.ARG_SCREEN, MainActivity.MERCHANT_SCREEN)
                                .putExtra(BusinessFragment.ARG_SHOW_BUSINESS_TYPE_BOTTOM_SHEET, true)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.PRIVACY -> {
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(
                                    MainActivity.ARG_SCREEN,
                                    MainActivity.PRIVACY_SCREEN
                                )
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.LANGUAGE -> {
                        intents.add(
                            Intent(context, AccountActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                        intents.add(
                            Intent(context, InAppLanguageActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.SHARE_AND_EARN -> {
                        intents.add(ReferralActivity.getIntent(context))
                    }
                    deepLinkUrl == DeepLinkUrl.SHARE -> {
                        intents.add(ShareActivity.starterIntent(context))
                    }
                    deepLinkUrl == DeepLinkUrl.COLLECTION_ADOPTION_SCREEN -> {
                        intents.add(
                            CollectionsHomeActivity.getIntent(context)
                        )
                    }
                    deepLinkUrl.contains("/account/collection/collection_adoption/referral/") -> {
                        intents.add(
                            CollectionsHomeActivity.getIntent(context)
                                .putExtra(
                                    CollectionAdoptionV2Fragment.ARG_REFERRAL_MERCHANT_ID,
                                    extras.getString(CollectionAdoptionV2Fragment.ARG_MERCHANT_ID)
                                )
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.REFERRAL_COLLECTION_EDUCATION_SCREEN -> {
                        intents.add(
                            TargetedReferralActivity.getIntent(context, TargetedReferralActivity.REFERRAL_EDUCATION_SCREEN)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.REFERRAL_COLLECTION_LIST_SCREEN -> {
                        intents.add(
                            TargetedReferralActivity.getIntent(context, TargetedReferralActivity.REFERRAL_INVITE_LIST)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.REWARDS_SCREEN -> {
                        intents.add(
                            Intent(context, RewardsActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(
                                    MainActivity.ARG_SCREEN,
                                    MainActivity.REWARDS_SCREEN
                                )
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.MERCHANT_DESTINATION_SCREEN || deepLinkUrl == DeepLinkUrl.UPI_SCREEN -> {
                        intents.add(CollectionsHomeActivity.getIntent(context))
                    }
                    deepLinkUrl.contains(DeepLinkUrl.MERCHANT_COLLECTION_SCREEN) -> {
                        intents.add(CollectionsHomeActivity.getIntent(context))
                    }
                    deepLinkUrl == DeepLinkUrl.HOME_ADD_CUSTOMER -> {
                        intents.add(
                            AddRelationshipActivity.getIntent(
                                context,
                                RelationshipType.ADD_CUSTOMER,
                                canShowTutorial = false,
                                showManualFlow = false,
                                source = ACTION_DEEP_LINK_COMPLEX
                            )
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.STAFF_COLLECTION_LINK_SCREEN || deepLinkUrl == DeepLinkUrl.STAFF_COLLECTION_LINK_SCREEN_V2 -> {
                        intents.add(Intent(context, StaffLinkActivity::class.java))
                    }
                    deepLinkUrl.contains("/customer/") -> {
                        val customerId = extras.getString(MainActivity.ARG_CUSTOMER_ID)
                        val txnId = extras.getString(MainActivity.ARG_TXN_ID)
                        if (deepLinkUrl.contains("online_payment")) {
                            intents.add(
                                Intent(context, MainActivity::class.java)
                                    .putExtra(CustomerFragment.ARG_SCREEN_REDIRECT_TO_PAYMENT, true)
                                    .setAction(ACTION_DEEP_LINK_COMPLEX)
                                    .putExtra(MainActivity.ARG_CUSTOMER_ID, customerId)
                                    .putExtra(MainActivity.ARG_TXN_ID, txnId)
                                    .putExtra(
                                        MainActivity.ARG_SCREEN,
                                        MainActivity.CUSTOMER_SCREEN
                                    )
                            )
                        } else {
                            intents.add(
                                Intent(context, MainActivity::class.java)
                                    .setAction(ACTION_DEEP_LINK_COMPLEX)
                                    .putExtra(MainActivity.ARG_CUSTOMER_ID, customerId)
                                    .putExtra(MainActivity.ARG_TXN_ID, txnId)
                                    .putExtra(
                                        MainActivity.ARG_SCREEN,
                                        MainActivity.CUSTOMER_SCREEN
                                    )
                            )
                            if (deepLinkUrl.contains("/edit")) {
                                intents.add(
                                    Intent(context, CustomerProfileActivity::class.java)
                                        .putExtra(CustomerProfileActivity.EXTRA_CUSTOMER_ID, customerId)
                                        .setAction(ACTION_DEEP_LINK_COMPLEX)
                                )
                            } else if (deepLinkUrl.contains("/delete")) {
                                intents.add(
                                    Intent(context, CustomerProfileActivity::class.java)
                                        .putExtra(CustomerProfileActivity.EXTRA_CUSTOMER_ID, customerId)
                                        .setAction(ACTION_DEEP_LINK_COMPLEX)
                                )
                                intents.add(
                                    Intent(context, DeleteCustomerActivity::class.java)
                                        .putExtra(DeleteCustomerActivity.EXTRA_CUSTOMER_ID, customerId)
                                        .setAction(ACTION_DEEP_LINK_COMPLEX)
                                )
                            }
                        }
                    }
                    deepLinkUrl.contains("/livesales/") -> {
                        val customerId = extras.getString(MainActivity.ARG_CUSTOMER_ID)
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(MainActivity.ARG_CUSTOMER_ID, customerId)
                                .putExtra(
                                    MainActivity.ARG_SCREEN,
                                    MainActivity.LIVE_SALE_SCREEN
                                )
                        )
                    }
                    deepLinkUrl.contains("/supplier/") -> {
                        val supplierId = extras.getString(MainActivity.ARG_SUPPLIER_ID)
                        val txnId = extras.getString(MainActivity.ARG_TXN_ID)
                        if (deepLinkUrl.contains("/online_payment")) {
                            intents.add(
                                Intent(context, SupplierActivity::class.java)
                                    .putExtra(SupplierFragment.ARG_SCREEN_REDIRECT_TO_PAYMENT, true)
                                    .putExtra(MainActivity.ARG_SUPPLIER_ID, supplierId)
                                    .putExtra(
                                        MainActivity.ARG_SCREEN,
                                        SupplierActivity.SUPPLIER_SCREEN
                                    )
                                    .setAction(DeepLinkActivity.ACTION_DEEP_LINK_COMPLEX)
                            )
                        } else {
                            intents.add(
                                Intent(context, SupplierActivity::class.java)
                                    .setAction(ACTION_DEEP_LINK_COMPLEX)
                                    .putExtra(MainActivity.ARG_SUPPLIER_ID, supplierId)
                                    .putExtra(MainActivity.ARG_TXN_ID, txnId)
                                    .putExtra(
                                        MainActivity.ARG_SCREEN,
                                        SupplierActivity.SUPPLIER_SCREEN
                                    )
                            )
                            when {
                                deepLinkUrl.contains("/edit") -> {
                                    intents.add(
                                        Intent(context, SupplierProfileActivity::class.java)
                                            .putExtra(SupplierProfileActivity.EXTRA_SUPPLIER_ID, supplierId)
                                            .setAction(ACTION_DEEP_LINK_COMPLEX)
                                    )
                                }
                                deepLinkUrl.contains("/delete") -> {
                                    intents.add(
                                        Intent(context, DeleteSupplierActivity::class.java)
                                            .putExtra(DeleteSupplierActivity.EXTRA_SUPPLIER_ID, supplierId)
                                            .setAction(ACTION_DEEP_LINK_COMPLEX)
                                    )
                                }
                            }
                        }
                    }
                    deepLinkUrl.contains("/collection/") -> {
                        // Take user to Txn screen via customer page for collection link
                        val collectionId = extras.getString("collections_id")
                        val title = extras.getString(MainActivity.ARG_ADOPTION_TITLE)
                        if (collectionId != null) {
                            intents.add(
                                Intent(context, MainActivity::class.java)
                                    .setAction(ACTION_DEEP_LINK_COMPLEX)
                                    .putExtra(MainActivity.ARG_COLLECTION_ID, collectionId)
                                    .putExtra(
                                        MainActivity.ARG_SOURCE,
                                        GetCustomerStatement.FROM_DEEP_LINK
                                    )
                                    .putExtra(MainActivity.ARG_CUSTOMER_ID, "")
                                    .putExtra(
                                        MainActivity.ARG_SCREEN,
                                        MainActivity.CUSTOMER_SCREEN
                                    )
                            )
                        } else if (title != null) {
                            intents.add(
                                CollectionsHomeActivity.getIntent(context)
                                    .putExtra(MainActivity.ARG_ADOPTION_TITLE, title)
                            )
                        }
                    }
                    deepLinkUrl == DeepLinkUrl.WHATSAPP_REG_ERROR_MOBILE_MISMATCH -> {
                        intents.add(
                            Intent(
                                context,
                                NavigationActivity::class.java
                            ).setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.WHATSAPP_REG_ERROR_TRY_AGAIN -> {
                        intents.add(
                            Intent(
                                context,
                                NavigationActivity::class.java
                            ).setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.HOME_ONE_TAP_COLLECTION_POPUP -> {
                        intents.add(
                            NavigationActivity.homeScreenIntent(context)
                                .putExtra(FrontendConstants.ARG_SHOW_COLLECTION_POPUP, true)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.HOME_IN_APP_REVIEW -> {
                        intents.add(
                            NavigationActivity.homeScreenIntent(context)
                                .putExtra(FrontendConstants.ARG_SHOW_INAPP_REVIEW, true)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.MERCHANT_BULK_REMINDER -> {
                        intents.add(
                            NavigationActivity.homeScreenIntent(context)
                                .putExtra(FrontendConstants.ARG_SHOW_BULK_REMINDER, true)
                        )
                    }
                    deepLinkUrl.contains("/experiments/") -> {
                        val experimentName = getExperimentName(deepLinkUrl)
                        if (!experimentName.isNullOrBlank()) {
                            intents.add(WebViewActivity.startingIntentForExperiment(context, experimentName))
                        }
                    }
                    deepLinkUrl.contains("/web/") -> {
                        val param = URLDecoder.decode(
                            extras.getString(DeepLinkUrl.LOAD_WEB_PARAM_URL),
                            StandardCharsets.UTF_8.name()
                        )
                        intents.add(getIntent(context, param))
                    }
                    deepLinkUrl.contains("/helpv2/") -> {
                        val helpInstructionSplitString: Array<String?> =
                            DeepLinkUrl.HELP_INSTRUCTION.split("/helpv2/").toTypedArray()
                        var helpId: String? = null
                        if (helpInstructionSplitString.size > 1 && helpInstructionSplitString[1] != null) helpId =
                            helpInstructionSplitString[1]
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(
                                    MainActivity.ARG_SCREEN,
                                    MainActivity.HELP__ITEM_SCREEN
                                )
                                .putExtra(MainActivity.HELP_ITEM_ID, helpId)
                                .putExtra(
                                    MainActivity.ARG_SOURCE,
                                    ARG_SOURCE
                                )

                        )
                    }
                    deepLinkUrl == DeepLinkUrl.WELCOME -> {
                        intents.add(
                            MainActivity.startingIntentForWelcomeLanguageSelectionScreen(context)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                    }
                    deepLinkUrl.contains("/cashsales") -> {
                        intents.add(Intent(SalesActivity.getSalesScreenIntent(context)))
                    }
                    deepLinkUrl.contains("/add/sale") -> {
                        intents.add(Intent(SalesActivity.getAddSaleScreenIntent(context)))
                    }
                    deepLinkUrl.contains("/sales/bill") -> {
                        intents.add(Intent(SalesActivity.getAddBillScreenIntent(context)))
                    }
                    deepLinkUrl.contains("/add/expense") -> {
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(MainActivity.ARG_SCREEN, MainActivity.ADD_EXPENSE)
                        )
                    }
                    deepLinkUrl.contains("expense_manager") -> {
                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(MainActivity.ARG_SCREEN, MainActivity.EXPENSE_MANAGER)
                        )
                    }
                    deepLinkUrl.contains("/chat") -> {
                        val accountId = extras.getString(CHAT_INTENT_EXTRAS.ACCOUNT_ID)
                        val receiverRole = extras.getString(CHAT_INTENT_EXTRAS.ROLE)
                        val messageId = extras.getString(CHAT_INTENT_EXTRAS.MESSAGE_ID)
                        if (receiverRole == "SELLER") {
                            intents.add(
                                Intent(context, MainActivity::class.java)
                                    .setAction(ACTION_DEEP_LINK_COMPLEX)
                                    .putExtra(MainActivity.ARG_CUSTOMER_ID, accountId) // customerId
                                    .putExtra(MainActivity.ARG_SCREEN, MainActivity.CUSTOMER_SCREEN)
                            )
                        }
                        if (receiverRole == "BUYER") {
                            val txnId = extras.getString(MainActivity.ARG_TXN_ID)
                            intents.add(
                                Intent(context, SupplierActivity::class.java)
                                    .setAction(ACTION_DEEP_LINK_COMPLEX)
                                    .putExtra(MainActivity.ARG_SUPPLIER_ID, accountId) // supplierId
                                    .putExtra(MainActivity.ARG_TXN_ID, txnId)
                                    .putExtra(MainActivity.ARG_SCREEN, SupplierActivity.SUPPLIER_SCREEN)

                            )
                        }
                        intents.add(
                            Intent(context, ChatActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(CHAT_INTENT_EXTRAS.ACCOUNT_ID, accountId)
                                .putExtra(CHAT_INTENT_EXTRAS.ROLE, receiverRole)
                        )
                    }
                    deepLinkUrl.contains("/bill") -> {
                        val accountId = extras.getString(BILL_INTENT_EXTRAS.ACCOUNT_ID)
                        val role = extras.getString(BILL_INTENT_EXTRAS.ROLE)
                        val accountName = extras.getString(BILL_INTENT_EXTRAS.ACCOUNT_NAME)
                        if (role == BILL_INTENT_EXTRAS.CUSTOMER) {

                            intents.add(
                                Intent(context, MainActivity::class.java)
                                    .setAction(ACTION_DEEP_LINK_COMPLEX)
                                    .putExtra(MainActivity.ARG_CUSTOMER_ID, accountId)
                                    .putExtra(MainActivity.ARG_SCREEN, MainActivity.CUSTOMER_SCREEN)
                            )
                        }
                        if (role == BILL_INTENT_EXTRAS.SUPPLIER) {

                            intents.add(
                                Intent(context, SupplierActivity::class.java)
                                    .setAction(ACTION_DEEP_LINK_COMPLEX)
                                    .putExtra(MainActivity.ARG_SUPPLIER_ID, accountId)
                                    .putExtra(MainActivity.ARG_SCREEN, SupplierActivity.SUPPLIER_SCREEN)

                            )
                        }
                        intents.add(
                            Intent(context, BillActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(BILL_INTENT_EXTRAS.ACCOUNT_ID, accountId)
                                .putExtra(BILL_INTENT_EXTRAS.ROLE, role)
                                .putExtra(BILL_INTENT_EXTRAS.ACCOUNT_NAME, accountName)

                        )
                    }
                    deepLinkUrl == DeepLinkUrl.COLLECTION_DEFAULTER_LIST -> {
                        intents.add(Intent(context, DefaulterListActivity::class.java))
                    }
                    deepLinkUrl.contains("cash_counter") -> {
                        intents.add(
                            Intent(context, CashCounterActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN -> {
                        intents.add(
                            Intent(context, AddTxnShortcutSearchActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(
                                    ARG_ADD_TRANSACTION_SHORTCUT_SOURCE,
                                    MainActivity.ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN
                                )
                        )
                    }
                    deepLinkUrl == DeepLinkUrl.ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN_FROM_REFERRAL -> {
                        intents.add(
                            Intent(context, AddTxnShortcutSearchActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(ARG_ADD_TRANSACTION_SHORTCUT_SOURCE, MainActivity.ARG_REFERRAL_TARGETS)
                        )
                    }
                    deepLinkUrl.contains("qrCode") -> {
                        intents.add(CollectionsHomeActivity.getIntent(context))
                    }

                    // todo (Kartik): update language key as per PM
                    deepLinkUrl == DeepLinkUrl.CHANGE_LANGUAGE -> {
                        intents.add(
                            InAppLanguageActivity.startingIntent(context)
                        )
                    }

                    deepLinkUrl == DeepLinkUrl.PSP_UPI_APPROVE_COLLECT -> {

                        val gatewayTxnId = extras.getString(PspUpiActivity.ARG_GATEWAY_TXN_ID)
                        val gatewayRefId = extras.getString(PspUpiActivity.ARG_GATEWAY_REF_ID)

                        intents.add(
                            Intent(context, MainActivity::class.java)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                                .putExtra(MainActivity.ARG_CUSTOMER_ID, "")
                                .putExtra(MainActivity.ARG_SCREEN, MainActivity.CUSTOMER_SCREEN)
                        )

                        intents.add(
                            Intent(context, PspUpiActivity::class.java)
                                .putExtra(PspUpiActivity.ARG_FEATURE, PspUpiActivity.ARG_FEATURE_APPROVE_COLLECT)
                                .putExtra(PspUpiActivity.ARG_GATEWAY_TXN_ID, gatewayTxnId)
                                .putExtra(PspUpiActivity.ARG_GATEWAY_REF_ID, gatewayRefId)
                                .setAction(ACTION_DEEP_LINK_COMPLEX)
                        )
                    }

                    deepLinkUrl == DeepLinkUrl.OKPL_VOICE_COLLECTION_SCREEN
                        || deepLinkUrl == DeepLinkUrl.OKPL_VOICE_COLLECTION_SCREEN_V2 -> {
                        intents.add(BoosterVoiceCollectionActivity.getIntent(context))
                    }

                    deepLinkUrl == DeepLinkUrl.HOME_BULK_ADD_BY_VOICE
                        || deepLinkUrl == DeepLinkUrl.HOME_BULK_ADD_BY_VOICE_V2
                    -> {
                        intents.add(BulkAddTransactionsActivity.getIntent(context))
                    }

                    deepLinkUrl == DeepLinkUrl.HOME_BULK_REMINDER -> {
                        intents.add(
                            BulkReminderV2Activity.getIntent(
                                context
                            )
                        )
                    }

                    deepLinkUrl.contains("/home/customer_profile/") -> {
                        intents.add(
                            CustomerProfileTransparentActivity.getIntent(
                                context,
                                extras.getString(CustomerProfileTransparentActivity.CUSTOMER_ID)
                            )
                        )
                    }
                }
            }
        }
        return intents
    }

    private fun getExperimentName(url: String): String? {
        val strings: Array<String?> = url.split("/experiments").toTypedArray()
        var experimentName: String? = "unknown"
        if (strings.size > 1 && strings[1] != null) {
            if (strings[1]!!.contains("/")) {
                val subStrings: Array<String?> = strings[1]!!.split("/").toTypedArray()
                experimentName = if (subStrings.size > 1 && subStrings[1] != null) subStrings[1] else null
            }
        }
        return experimentName
    }
}
