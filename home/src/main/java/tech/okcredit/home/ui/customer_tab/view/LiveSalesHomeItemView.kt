package tech.okcredit.home.ui.customer_tab.view

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import dagger.Lazy
import kotlinx.coroutines.launch
import tech.okcredit.home.R
import tech.okcredit.home.databinding.ItemLiveSalesBinding
import tech.okcredit.home.ui.customer_tab.CustomerTabItem
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference
import javax.inject.Inject

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class LiveSalesHomeItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var isShowedTutorial = false
    private var isEducationVisible = false

    interface CustomerSelectionListener {
        fun onLiveSaleQrSelected(customerId: String)
        fun onLiveItemClicked(customerId: String)
        fun onLiveSaleTutorialSeen()
    }

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    private val binding = ItemLiveSalesBinding.inflate(LayoutInflater.from(context), this, true)
    private var customerSelectionListener: CustomerSelectionListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_live_sales, this, true)

        binding.llCustomerViewRoot.setOnClickListener {
            val id = binding.root.tag as String
            customerSelectionListener?.onLiveItemClicked(id)
        }

        binding.photoImageView.setOnClickListener {
            if (isEducationVisible.not()) {
                val id = binding.root.tag as String
                customerSelectionListener?.onLiveSaleQrSelected(id)
            }
        }
    }

    @ModelProp
    fun setTutorialVisibility(tutorialVisibility: Boolean) {
        if (isShowedTutorial.not() && tutorialVisibility) {
            isShowedTutorial = true
            isEducationVisible = true

            Analytics.track(
                AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
                EventProperties.create()
                    .with(PropertyKey.TYPE, "live_sales1")
                    .with(PropertyKey.SCREEN, "HomeScreen")
            )

            (context as FragmentActivity).lifecycleScope.launch {
                localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(context as FragmentActivity),
                        tapTarget = TapTargetLocal(
                            screenName = "",
                            targetView = WeakReference(binding.photoImageView),
                            title = context.getString(R.string.live_sale_tuto_title_2),
                            subtitle = context.getString(R.string.live_sale_tuto_desc_2),
                            titleTextSize = 24f,
                            subtitleTextSize = 14f,
                            titleGravity = Gravity.BOTTOM,
                            focalRadius = 60f,
                            listener = { _, state ->
                                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                    Analytics.track(
                                        AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                        EventProperties.create()
                                            .with(PropertyKey.TYPE, "live_sales1")
                                            .with("focal_area", true)
                                            .with(PropertyKey.SCREEN, "HomeScreen")
                                    )

                                    showSecondTutorial()
                                } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                    Analytics.track(
                                        AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                        EventProperties.create()
                                            .with(PropertyKey.TYPE, "live_sales1")
                                            .with("focal_area", false)
                                            .with(PropertyKey.SCREEN, "HomeScreen")
                                    )

                                    showSecondTutorial()
                                } else if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                                    Analytics.track(
                                        AnalyticsEvents.IN_APP_NOTI_CLEARED,
                                        EventProperties.create()
                                            .with(PropertyKey.TYPE, "live_sales1")
                                            .with("focal_area", false)
                                            .with(PropertyKey.SCREEN, "HomeScreen")
                                    )
                                }
                            }
                        )
                    )
            }
        }
    }

    private fun showSecondTutorial() {
        customerSelectionListener?.onLiveSaleTutorialSeen()

        Analytics.track(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
            EventProperties.create()
                .with(PropertyKey.TYPE, "live_sales2")
                .with(PropertyKey.SCREEN, "HomeScreen")
        )

        (context as FragmentActivity).lifecycleScope.launch {
            localInAppNotificationHandler.get()
                .generateTapTarget(
                    weakScreen = WeakReference(context as FragmentActivity),
                    tapTarget = TapTargetLocal(
                        screenName = "",
                        targetView = WeakReference(binding.tvBalance),
                        title = context.getString(R.string.live_sale_tuto_title_1),
                        subtitle = context.getString(R.string.live_sale_tuto_desc_1),
                        titleGravity = Gravity.BOTTOM,
                        titleTextSize = 24f,
                        subtitleTextSize = 14f,
                        focalRadius = 60f,
                        listener = { _, state ->
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                Analytics.track(
                                    AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                    EventProperties.create()
                                        .with(PropertyKey.TYPE, "live_sales2")
                                        .with("focal_area", true)
                                        .with(PropertyKey.SCREEN, "HomeScreen")
                                )
                            } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                Analytics.track(
                                    AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                    EventProperties.create()
                                        .with(PropertyKey.TYPE, "live_sales2")
                                        .with("focal_area", false)
                                        .with(PropertyKey.SCREEN, "HomeScreen")
                                )
                            } else if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                                isEducationVisible = false
                                Analytics.track(
                                    AnalyticsEvents.IN_APP_NOTI_CLEARED,
                                    EventProperties.create()
                                        .with(PropertyKey.TYPE, "live_sales2")
                                        .with("focal_area", false)
                                        .with(PropertyKey.SCREEN, "HomeScreen")
                                )
                            }
                        }
                    )
                )
        }
    }

    @ModelProp
    fun setCustomer(customer: CustomerTabItem.LiveSalesItem) {
        binding.root.tag = customer.id
        CurrencyUtil.renderV2(customer.balance, binding.tvBalance, 0)
        binding.liveSaleTitle.text = context.getString(R.string.link_pay)
    }

    @CallbackProp
    fun setListener(listener: CustomerSelectionListener?) {
        this.customerSelectionListener = listener
    }
}
