package `in`.okcredit.collection_ui.ui.benefits

import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.collection.contract.MerchantDestinationListener
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.databinding.CollectionsBenefitActivityBinding
import `in`.okcredit.collection_ui.ui.benefits.views.AutoLedgerViewModel_
import `in`.okcredit.collection_ui.ui.benefits.views.PaymentOptionsViewModel_
import `in`.okcredit.collection_ui.ui.benefits.views.PendingDuesViewModel_
import `in`.okcredit.collection_ui.ui.benefits.views.SendReminderViewModel_
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.airbnb.epoxy.VisibilityState
import com.airbnb.epoxy.carousel
import io.reactivex.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.dpToPixel
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject

class CollectionBenefitsActivity :
    BaseActivity<CollectionsBenefitContract.State, CollectionsBenefitContract.ViewEvent, CollectionsBenefitContract.Intent>(
        "CollectionBenefitsActivity"
    ),
    MerchantDestinationListener {

    private val binding: CollectionsBenefitActivityBinding by viewLifecycleScoped(CollectionsBenefitActivityBinding::inflate)

    @Inject
    lateinit var collectionNavigator: CollectionNavigator

    @Inject
    lateinit var collectionTracker: CollectionTracker

    private val pageChangeLiveData = MutableLiveData(0)
    private var onItemTouched = false

    private val source by lazy { intent.getStringExtra("source") ?: "collection_benefits" }

    private var currentScreenType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.imageClose.setOnClickListener { finish() }
        binding.epoxyBenefits.withModels {
            carousel {
                id("carouselView")
                numViewsToShowOnScreen(1f)
                initialPrefetchItemCount(1)
                onVisibilityStateChanged { _, view, visibilityState ->
                    if (visibilityState == VisibilityState.FULL_IMPRESSION_VISIBLE) {
                        startAutoScroll(view)
                    }
                }
                this.models(
                    listOf(
                        PendingDuesViewModel_()
                            .id("pending_dues")
                            .onVisibilityStateChanged { _, _, visibilityState ->
                                if (visibilityState == VisibilityState.FULL_IMPRESSION_VISIBLE) {
                                    pageChangeLiveData.value = 0
                                }
                            },
                        SendReminderViewModel_()
                            .id("send_reminder")
                            .onVisibilityStateChanged { _, _, visibilityState ->
                                if (visibilityState == VisibilityState.FULL_IMPRESSION_VISIBLE) {
                                    pageChangeLiveData.value = 1
                                }
                            },
                        PaymentOptionsViewModel_()
                            .id("payment_options")
                            .onVisibilityStateChanged { _, _, visibilityState ->
                                if (visibilityState == VisibilityState.FULL_IMPRESSION_VISIBLE) {
                                    pageChangeLiveData.value = 2
                                }
                            },
                        AutoLedgerViewModel_()
                            .id("auto_ledger")
                            .onVisibilityStateChanged { _, _, visibilityState ->
                                if (visibilityState == VisibilityState.FULL_IMPRESSION_VISIBLE) {
                                    pageChangeLiveData.value = 3
                                }
                            },
                    )
                )
            }
        }
        EpoxyVisibilityTracker().attach(binding.epoxyBenefits)

        binding.buttonSetup.setOnClickListener {
            pushIntent(CollectionsBenefitContract.Intent.SetupClicked)
        }
    }

    override fun onDestroy() {
        binding.viewShine.clearAnimation()
        super.onDestroy()
    }

    private fun startAutoScroll(carousal: Carousel) {
        carousal.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                onItemTouched = true
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }
        })
        lifecycleScope.launch {
            // We run the auto scroll only till the point no item is touched
            while (pageChangeLiveData.value != null && pageChangeLiveData.value!! < 3 && !onItemTouched) {
                delay(2_000)
                carousal.smoothScrollToPosition(pageChangeLiveData.value!! + 1)
            }
        }

        pageChangeLiveData.observe(
            this,
            { position ->
                if (position == 3) {
                    startButtonShineEffect()
                }
                when (position) {
                    0 -> {
                        currentScreenType = "pending_dues_collect_faster"
                        binding.indicatorOne.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorTwo.animateAlphaIfNotCurrentValue(0.2f)
                        binding.indicatorThree.animateAlphaIfNotCurrentValue(0.2f)
                        binding.indicatorFour.animateAlphaIfNotCurrentValue(0.2f)
                    }
                    1 -> {
                        currentScreenType = "send_reminder_with_qr_and_payment_link"
                        binding.indicatorOne.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorTwo.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorThree.animateAlphaIfNotCurrentValue(0.2f)
                        binding.indicatorFour.animateAlphaIfNotCurrentValue(0.2f)
                    }
                    2 -> {
                        currentScreenType = "variety_payment_options"
                        binding.indicatorOne.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorTwo.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorThree.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorFour.animateAlphaIfNotCurrentValue(0.2f)
                    }
                    3 -> {
                        currentScreenType = "automatic_ledger_entry"
                        binding.indicatorOne.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorTwo.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorThree.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorFour.animateAlphaIfNotCurrentValue(1f)
                    }
                }
                val customerId = if (isStateInitialized()) getCurrentState().customerId ?: "" else ""
                collectionTracker.trackContextualInfoScroll(customerId, currentScreenType, "")
            }
        )
    }

    private fun startButtonShineEffect() {
        if (binding.viewShine.isVisible) return

        binding.viewShine.isVisible = true
        binding.imageHandNudge.isVisible = true
        AnimationUtils.upDownMotion(view = binding.imageHandNudge, delta = dpToPixel(30f), startDelay = 0L)
        AnimationUtils.shineEffect(binding.buttonSetup, binding.viewShine)
    }

    private fun View.animateAlphaIfNotCurrentValue(value: Float) {
        if (this.alpha != value) {
            animate().alpha(value)
        }
    }

    override fun onBackPressed() {
        collectionTracker.trackContextualInfoDismiss(getCurrentState().customerId ?: "", currentScreenType, "")
        super.onBackPressed()
    }

    override fun onAccountAddedSuccessfully(eta: Long) {
        collectionTracker.trackContextualInfoDismiss(getCurrentState().customerId ?: "", currentScreenType, "")
        finish()
    }

    override fun onCancelled() {
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: CollectionsBenefitContract.State) {
        binding.tvButton.text = if (state.sendReminder) {
            getString(R.string.send_payment_reminder)
        } else {
            getString(R.string.setup_online_payments)
        }
    }

    override fun loadIntent() = CollectionsBenefitContract.Intent.Load

    override fun handleViewEvent(event: CollectionsBenefitContract.ViewEvent) {
        when (event) {
            is CollectionsBenefitContract.ViewEvent.SendReminder -> startActivity(event.intent)
            CollectionsBenefitContract.ViewEvent.ShowAddBankDetails -> {
                collectionNavigator.showAddMerchantDestinationDialog(supportFragmentManager, source)
            }
        }
    }

    companion object {
        @JvmStatic
        fun getIntent(
            context: Context,
            source: String,
            sendReminder: Boolean = false,
            customerId: String? = null,
        ): Intent {
            return Intent(context, CollectionBenefitsActivity::class.java).apply {
                putExtra("send_reminder", sendReminder)
                putExtra("customer_id", customerId)
                putExtra("source", source)
            }
        }
    }
}
