package tech.okcredit.home.ui.customer_tab

import `in`.okcredit.dynamicview.DynamicViewKit
import `in`.okcredit.dynamicview.TargetSpec
import `in`.okcredit.dynamicview.component.banner.BannerComponentModel
import `in`.okcredit.dynamicview.component.cell.CellComponentModel
import `in`.okcredit.dynamicview.component.recycler.RecyclerComponentModel
import `in`.okcredit.dynamicview.view.dynamicViewEpoxyItem
import `in`.okcredit.shared.deeplink.InternalDeeplinkNavigationDelegator
import `in`.okcredit.shared.referral_views.referralTargetBanner
import `in`.okcredit.shared.referral_views.referralTargetFullView
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController
import dagger.Lazy
import dagger.Reusable
import merchant.okcredit.user_stories.homestory.homeUserStoryLayout
import tech.okcredit.home.ui.bulk_reminder.BulkReminderBanner.BulkReminderBannerListener
import tech.okcredit.home.ui.bulk_reminder.bulkReminderBanner
import tech.okcredit.home.ui.customer_tab.view.addCustomerTutorial
import tech.okcredit.home.ui.customer_tab.view.appLockInAppNotiView
import tech.okcredit.home.ui.customer_tab.view.liveSalesHomeItemView
import tech.okcredit.home.ui.home.views.emptyFilteredCustomerView
import tech.okcredit.home.ui.home.views.homeCustomerViewV2
import tech.okcredit.home.ui.homesearch.Sort
import tech.okcredit.home.ui.homesearch.views.filterView
import javax.inject.Inject

@Reusable
class CustomerTabControllerV2 @Inject constructor(
    private val fragment: CustomerTabFragment,
    private val dynamicViewKit: Lazy<DynamicViewKit>,
    private val internalDeeplinkNavigator: Lazy<InternalDeeplinkNavigationDelegator>,
    private val bulkReminderBannerListener: Lazy<BulkReminderBannerListener>,
) : TypedEpoxyController<List<CustomerTabItem>>(
    EpoxyAsyncUtil.getAsyncBackgroundHandler(),
    EpoxyAsyncUtil.getAsyncBackgroundHandler()
) {

    var onReferralTransactionInitiated: (() -> Unit)? = null
    var onReferralCloseClicked: (() -> Unit)? = null

    override fun buildModels(data: List<CustomerTabItem>?) {
        data?.forEach {
            when (it) {
                is CustomerTabItem.HomeCustomerItem -> renderHomeCustomerView(it)
                is CustomerTabItem.FilterItem -> renderFilterView()
                CustomerTabItem.EmptyFilterItem -> renderEmptyFilterView()
                is CustomerTabItem.LiveSalesItem -> renderLiveSalesView(it)
                is CustomerTabItem.DynamicViewItem -> renderDynamicBanner(it)
                CustomerTabItem.AppLockItem -> renderAppLockIten()
                is CustomerTabItem.ReferralTargetBannerItem -> renderReferralTargetBanner(it)
                CustomerTabItem.AddCustomerTutorialItem -> renderAddCustomerTutorial()
                is CustomerTabItem.UserStoriesItem -> renderUserStories()
                is CustomerTabItem.BulkReminderBanner -> renderBulkReminderBanner(it.bulkReminderState)
            }
        }
    }

    private fun renderBulkReminderBanner(bulkReminderState: CustomerTabContract.BulkReminderState) {
        bulkReminderBanner {
            id("bulkReminderBanner")
            bulkReminderState(bulkReminderState)
            listener(bulkReminderBannerListener.get())
        }
    }

    private fun renderUserStories() {
        homeUserStoryLayout {
            id("homeUserStoryLayout")
        }
    }

    private fun renderAddCustomerTutorial() {
        addCustomerTutorial {
            id("addCustomerTutorial")
        }
    }

    private fun renderReferralTargetBanner(referralTargetBanner: CustomerTabItem.ReferralTargetBannerItem) {
        if (referralTargetBanner.fullView) {
            referralTargetFullView {
                id("referralTargetFullView")
                target(referralTargetBanner.referralTargetBanner)
            }
        } else {
            referralTargetBanner {
                id("referralTargetBanner")
                target(referralTargetBanner.referralTargetBanner)
                deeplinkNavigator(internalDeeplinkNavigator.get())
                transactionListener(onReferralTransactionInitiated)
                closeListener(onReferralCloseClicked)
            }
        }
    }

    private fun renderAppLockIten() {
        appLockInAppNotiView {
            id("appLockInAppNotiView")
            listener(fragment)
        }
    }

    private fun renderDynamicBanner(dynamicViewItem: CustomerTabItem.DynamicViewItem) {
        val spec = TargetSpec(
            dynamicViewItem.bannerCustomization.target,
            allowedComponents = setOf(
                BannerComponentModel::class.java,
                CellComponentModel::class.java,
                RecyclerComponentModel::class.java
            )
        )
        dynamicViewEpoxyItem(dynamicViewKit.get()) {
            id("dynamicViewEpoxyItem")
            spec(spec)
            component(dynamicViewItem.bannerCustomization.component)
        }
    }

    private fun renderHomeCustomerView(customerItem: CustomerTabItem.HomeCustomerItem) {
        homeCustomerViewV2 {
            id(customerItem.customerId)
            homeCustomerItem(customerItem)
            listener(fragment)
        }
    }

    private fun renderLiveSalesView(liveSalesItem: CustomerTabItem.LiveSalesItem) {
        liveSalesHomeItemView {
            id("liveSalesHomeItemView")
            customer(liveSalesItem)
            tutorialVisibility(liveSalesItem.liveSalesTutorialVisibility)
            listener(fragment)
        }
    }

    private fun renderFilterView() {
        filterView {
            id("filter_view")
            sortProp(Sort.sortfilter.size)
            listener(fragment)
        }
    }

    private fun renderEmptyFilterView() {
        emptyFilteredCustomerView {
            id("empty_filter_view")
            sortProp(Sort)
            listener(fragment)
        }
    }
}
