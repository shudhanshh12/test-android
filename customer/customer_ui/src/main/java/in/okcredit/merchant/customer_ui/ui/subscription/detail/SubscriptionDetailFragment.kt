package `in`.okcredit.merchant.customer_ui.ui.subscription.detail

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.data.server.model.request.toFormattedString
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionStatus
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionStatus.*
import `in`.okcredit.merchant.customer_ui.databinding.SubscriptionDetailScreenBinding
import `in`.okcredit.merchant.customer_ui.ui.delete.DeleteItemBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.subscription.SubscriptionEventTracker
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderScreen
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.bumptech.glide.util.Preconditions
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SubscriptionDetailFragment :
    BaseFragment<SubscriptionDetailContract.State, SubscriptionDetailContract.ViewEvent, SubscriptionDetailContract.Intent>(
        "SubscriptionDetailScreen",
        R.layout.subscription_detail_screen
    ),
    DeleteItemBottomSheet.DeleteConfirmListener {

    private var bottomSheetLoaderScreen: BottomSheetLoaderScreen? = null
    private val simpleDateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM)

    private val binding: SubscriptionDetailScreenBinding by viewLifecycleScoped(SubscriptionDetailScreenBinding::bind)

    private val source by lazy { arguments?.getString(SubscriptionDetailContract.ARG_SOURCE) ?: "" }
    private val subscriptionId by lazy { arguments?.getString(SubscriptionDetailContract.ARG_SUBSCRIPTION_ID) ?: "" }

    @Inject
    lateinit var subscriptionEventTracker: Lazy<SubscriptionEventTracker>

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            val popped = findNavController(this).popBackStack()
            if (!popped) { // finish activity if nothing on back stack
                requireActivity().finish()
            }
        }

        binding.buttonDelete.setOnClickListener { pushIntent(SubscriptionDetailContract.Intent.DeleteSubscription) }
    }

    override fun render(state: SubscriptionDetailContract.State) {
        binding.apply {
            textSubscriptionName.text = state.name
            textStartDate.text = simpleDateFormat.format(Date(1000L * state.startDate))
            CurrencyUtil.renderAsSubtitle(textAmount, state.amount)
            textTxnCount.text = state.txnCountSoFar.toString()
            state.nexDate?.let { textNextDate.text = simpleDateFormat.format(Date(1000L * it)) }
        }
        setStatus(state.status)
        setFrequency(state)
    }

    private fun setStatus(status: SubscriptionStatus?) {
        when (status) {
            ACTIVE -> {
                binding.textStatus.text = getString(R.string.active)
                binding.textStatus.setTextColor(getColorCompat(R.color.green_primary))
                binding.buttonDelete.visible()
                binding.buttonDelete.enable()
            }
            DELETED -> {
                binding.textStatus.text = getString(R.string.deleted)
                binding.textStatus.setTextColor(getColorCompat(R.color.red_primary))
                binding.buttonDelete.gone()
            }
            PAUSED -> {
                binding.textStatus.text = getString(R.string.paused)
                binding.textStatus.setTextColor(getColorCompat(R.color.grey700))
                binding.buttonDelete.visible()
            }
            EXPIRED -> {
                binding.textStatus.text = getString(R.string.expired)
                binding.textStatus.setTextColor(getColorCompat(R.color.grey700))
                binding.buttonDelete.gone()
            }
            else -> {
                binding.buttonDelete.gone()
            }
        }
    }

    private fun setFrequency(state: SubscriptionDetailContract.State) {
        when (state.frequency) {
            SubscriptionFrequency.DAILY -> {
                binding.textRepeatLabel.text = getString(R.string.repeat)
                binding.textRepeat.text = getString(R.string.daily)
            }
            SubscriptionFrequency.WEEKLY -> {
                binding.textRepeatLabel.text = getString(R.string.repeat_weekly)
                binding.textRepeat.text = (state.daysInWeek)?.toFormattedString() ?: ""
            }
            SubscriptionFrequency.MONTHLY -> {
                binding.textRepeatLabel.text = getString(R.string.repeat_monthly)
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = state.startDate * 1000L
                }
                binding.textRepeat.text =
                    getString(
                        R.string.on_day_of_month,
                        calendar.get(Calendar.DAY_OF_MONTH),
                        getDayOfMonthSuffix(calendar.get(Calendar.DAY_OF_MONTH))
                    )
            }
        }
    }

    private fun getDayOfMonthSuffix(dayOfMonth: Int): String {
        Preconditions.checkArgument(dayOfMonth in 1..31, "illegal day of month: $dayOfMonth")
        return if (dayOfMonth in 11..13) {
            "th"
        } else when (dayOfMonth % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

    override fun handleViewEvent(event: SubscriptionDetailContract.ViewEvent) {
        when (event) {
            SubscriptionDetailContract.ViewEvent.ShowDeleteConfirm -> showDeleteConfirmation()
            is SubscriptionDetailContract.ViewEvent.ShowError -> longToast(event.error)
            is SubscriptionDetailContract.ViewEvent.CustomerLoaded -> {
                subscriptionEventTracker.get().trackSubscriptionTransactionDetailsClick(
                    screen = source,
                    accountId = event.customerId,
                    mobile = event.mobile,
                    transactionId = subscriptionId
                )

                subscriptionEventTracker.get().trackSubscriptionTransactionView(
                    accountId = event.customerId,
                    mobile = event.mobile,
                    transactionId = subscriptionId
                )
            }
            SubscriptionDetailContract.ViewEvent.HideDeleteLoader -> {
                binding.buttonDelete.enable()
                bottomSheetLoaderScreen?.dismissAllowingStateLoss()
                bottomSheetLoaderScreen = null
            }
            SubscriptionDetailContract.ViewEvent.ShowDeleteLoader -> {
                initAndShowLoaderScreen()
            }
        }
    }

    private fun showDeleteConfirmation() {
        subscriptionEventTracker.get().trackSubscriptionDeleteClick(
            screen = source,
            accountId = getCurrentState().customer?.id ?: "",
            mobile = getCurrentState().customer?.mobile,
            transactionId = subscriptionId
        )
        DeleteItemBottomSheet.getInstance(
            title = getString(R.string.are_you_sure),
            description = getString(R.string.msg_delete_confirmation)
        ).apply {
            setListener(this@SubscriptionDetailFragment)
        }.show(childFragmentManager, DeleteItemBottomSheet::class.java.simpleName)
    }

    private fun initAndShowLoaderScreen() {
        if (bottomSheetLoaderScreen == null) bottomSheetLoaderScreen =
            BottomSheetLoaderScreen.getInstance(getString(R.string.deleting_subscription))
        bottomSheetLoaderScreen?.show(childFragmentManager, BottomSheetLoaderScreen::class.java.simpleName)
    }

    override fun deleteCancelled() {
        subscriptionEventTracker.get().trackSubscriptionDeleteCancel(
            screen = source,
            accountId = getCurrentState().customer?.id ?: "",
            mobile = getCurrentState().customer?.mobile,
            transactionId = subscriptionId
        )
    }

    override fun deleteConfirmed() {
        subscriptionEventTracker.get().trackSubscriptionDeleteConfirm(
            screen = source,
            accountId = getCurrentState().customer?.id ?: "",
            mobile = getCurrentState().customer?.mobile,
            transactionId = subscriptionId
        )
        binding.buttonDelete.disable()
        pushIntentWithDelay(SubscriptionDetailContract.Intent.DeleteConfirmed)
    }

    override fun loadIntent() = SubscriptionDetailContract.Intent.Load
}
