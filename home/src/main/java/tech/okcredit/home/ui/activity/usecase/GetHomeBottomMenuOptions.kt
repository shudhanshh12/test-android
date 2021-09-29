package tech.okcredit.home.ui.activity.usecase

import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.home.R
import tech.okcredit.home.ui.activity.HomeActivityContract.Companion.FEATURE_HELP_SUPPORT
import tech.okcredit.home.ui.activity.HomeActivityContract.Companion.FEATURE_HOME_DASHBOARD
import tech.okcredit.home.ui.activity.viewpager.BottomMenuItem
import tech.okcredit.home.ui.activity.viewpager.NavItem
import tech.okcredit.home.usecase.ShowOnlinePaymentsBottomMenu
import javax.inject.Inject

class GetHomeBottomMenuOptions @Inject constructor(
    private val ab: Lazy<AbRepository>,
    private val showOnlinePaymentsBottomMenu: Lazy<ShowOnlinePaymentsBottomMenu>,
) {
    fun execute(): Observable<Response> {
        return Observable.combineLatest(
            ab.get().isFeatureEnabled(FEATURE_HOME_DASHBOARD, ignoreCache = true),
            showOnlinePaymentsBottomMenu.get().execute(),
            ab.get().isFeatureEnabled(FEATURE_HELP_SUPPORT, ignoreCache = true),
        ) { dashboardEnabled, paymentsEnabled, helpSupportDisabled ->
            Response(
                bottomNavItems = getBottomNavItemsMapping(
                    dashboardEnabled,
                    paymentsEnabled,
                    helpSupportDisabled,
                )
            )
        }
    }

    data class Response(
        val bottomNavItems: List<BottomMenuItem>,
    )

    private fun getBottomNavItemsMapping(
        isDashboardEnabled: Boolean,
        isPaymentsEnabled: Boolean,
        isHelpSupportDisabled: Boolean,
    ): List<BottomMenuItem> {
        val menuItemList = arrayListOf(
            BottomMenuItem(
                navItem = NavItem.HOME_FRAGMENT,
                drawableId = R.drawable.ic_ledger,
                stringId = R.string.ledger,
                contentDescriptionId = R.string.content_description_home_bottom_nav_ledger,
            )
        )

        if (isPaymentsEnabled) {
            menuItemList.add(
                BottomMenuItem(
                    navItem = NavItem.HOME_PAYMENTS,
                    drawableId = R.drawable.ic_collection_icon,
                    stringId = R.string.payments,
                    contentDescriptionId = R.string.content_description_home_bottom_nav_payments,
                )
            )
        }

        if (isDashboardEnabled) {
            menuItemList.add(
                BottomMenuItem(
                    navItem = NavItem.DASHBOARD_FRAGMENT,
                    drawableId = R.drawable.ic_dashboard,
                    stringId = R.string.dashboard,
                    contentDescriptionId = R.string.content_description_home_bottom_nav_dashboard,
                )
            )
        }

        if (!isHelpSupportDisabled) {
            menuItemList.add(
                BottomMenuItem(
                    navItem = NavItem.HOME_HELP_SUPPORT_FRAGMENT,
                    drawableId = R.drawable.ic_help_outline,
                    stringId = R.string.t_001_bottom_navigation_help,
                    contentDescriptionId = R.string.content_description_home_bottom_nav_help,
                )
            )
        }

        menuItemList.add(
            BottomMenuItem(
                navItem = NavItem.HOME_MENU_FRAGMENT,
                drawableId = R.drawable.ic_hamburger,
                stringId = R.string.menu,
                contentDescriptionId = R.string.content_description_home_bottom_nav_menu,
            )
        )

        return menuItemList
    }
}
