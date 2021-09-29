package `in`.okcredit.shortcut

import `in`.okcredit.R
import `in`.okcredit.backend._offline.usecase.AppShortcutHelper
import `in`.okcredit.customer.contract.RelationshipType
import `in`.okcredit.merchant.customer_ui.addrelationship.AddRelationshipActivity
import `in`.okcredit.navigation.NavigationActivity
import `in`.okcredit.shared.utils.AbFeatures
import `in`.okcredit.ui.trasparent.TransparentDeeplinkActivity
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.Lazy
import tech.okcredit.app_contract.AppShortcutAdder
import tech.okcredit.app_contract.AppShortcutAdder.Shortcut
import tech.okcredit.app_contract.AppShortcutAdder.Shortcut.*
import tech.okcredit.home.ui.activity.HomeSearchActivity
import tech.okcredit.home.ui.add_transaction_home_search.AddTxnShortcutSearchActivity
import tech.okcredit.home.ui.homesearch.HomeConstants
import javax.inject.Inject

class AppShortcutAdderImpl @Inject constructor(
    private val shortcutHelper: Lazy<AppShortcutHelper>,
) : AppShortcutAdder {

    override fun addAppShortcutIfNotAdded(shortcut: Shortcut, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            when (shortcut) {
                ADD_CUSTOMER -> addAddCustomerShortcut(context)
                SEARCH_CUSTOMER -> addSearchCustomerShortcut(context)
                ADD_TRANSACTION -> addAddTransactionShortcut(context)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun addAddCustomerShortcut(context: Context) {
        val shortCutAddCus = ShortcutInfo.Builder(context, ADD_CUSTOMER.id)
            .setShortLabel(context.getString(R.string.add_customer))
            .setLongLabel(context.getString(R.string.add_customer))
            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_person_add))
            .setIntents(
                arrayOf(
                    NavigationActivity.homeScreenIntent(context),
                    AddRelationshipActivity.getIntent(
                        context,
                        relationshipType = RelationshipType.ADD_CUSTOMER,
                        canShowTutorial = false,
                        showManualFlow = false,
                        source = "App Launcher"
                    ).setAction(Intent.ACTION_VIEW)
                )
            )
            .build()
        shortcutHelper.get().addDynamicShortcutsIfNotAdded(listOf(shortCutAddCus))
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun addSearchCustomerShortcut(context: Context) {
        val shortCutAddSearch = ShortcutInfo.Builder(context, SEARCH_CUSTOMER.id)
            .setShortLabel(context.getString(R.string.search_customers))
            .setLongLabel(context.getString(R.string.search_customers))
            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_search))
            .setIntents(
                arrayOf(
                    NavigationActivity.homeScreenIntent(context),
                    HomeSearchActivity.startingIntent(context, HomeConstants.HomeTab.CUSTOMER_TAB)
                        .setAction(Intent.ACTION_VIEW)
                )
            )
            .build()
        shortcutHelper.get().addDynamicShortcutsIfNotAdded(listOf(shortCutAddSearch))
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun addAddTransactionShortcut(context: Context) {
        val shortcutAddTransaction =
            ShortcutInfo.Builder(context, ADD_TRANSACTION.id)
                .setShortLabel(context.getString(R.string.add_transaction))
                .setLongLabel(context.getString(R.string.add_transaction))
                .setIcon(Icon.createWithResource(context, R.drawable.ic_add_transaction_shortcut))
                .setIntents(
                    arrayOf(
                        NavigationActivity.homeScreenIntent(context),
                        AddTxnShortcutSearchActivity.getIntent(context)
                            .setAction(Intent.ACTION_VIEW),
                        TransparentDeeplinkActivity.getIntent(context, TransparentDeeplinkActivity.APP_LOCK)
                            .putExtra(TransparentDeeplinkActivity.DO_NOT_ANIMATE_ACTIVITY_EXIT, true)
                    )
                )
                .build()
        shortcutHelper.get()
            .addShortcutIfFeatureIsEnabled(
                shortcutAddTransaction,
                AbFeatures.FEATURE_ADD_TRANSACTION_SHORTCUT
            )
    }
}
