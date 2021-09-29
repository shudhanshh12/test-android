package tech.okcredit.home.ui.business_health_dashboard

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.business_health_dashboard.contract.model.Trend
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.exhaustive
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.home.R
import tech.okcredit.home.databinding.BusinessHealthDashboardFragmentBinding
import tech.okcredit.home.ui.acccountV2.ui.AccountActivity
import tech.okcredit.home.ui.business_health_dashboard.FeedbackClickListener.Companion.NEGATIVE_FEEDBACK_RESPONSE_STRING
import tech.okcredit.home.ui.business_health_dashboard.FeedbackClickListener.Companion.POSITIVE_FEEDBACK_RESPONSE_STRING
import javax.inject.Inject

class BusinessHealthDashboardFragment :
    BaseFragment<BusinessHealthDashboardContract.State, BusinessHealthDashboardContract.ViewEvent, BusinessHealthDashboardContract.Intent>(
        "BusinessHealthDashboardScreen",
        R.layout.business_health_dashboard_fragment
    ),
    FeedbackClickListener,
    TimeCadenceSelectionBottomSheetDialog.TimeCadenceSelectionListener {

    @Inject
    internal lateinit var businessHealthDashboardEventTracker: Lazy<BusinessHealthDashboardAnalyticsTracker>

    private val binding: BusinessHealthDashboardFragmentBinding by viewLifecycleScoped(
        BusinessHealthDashboardFragmentBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rootView.setTracker(performanceTracker)

        binding.trendsRecyclerView.adapter = TrendsAdapter(
            feedbackClickListener = this@BusinessHealthDashboardFragment
        )
        val linearLayoutManager = LinearLayoutManager(context)
        binding.trendsRecyclerView.layoutManager = linearLayoutManager
        binding.trendsRecyclerView.setHasFixedSize(true)

        binding.showMoreTextview.setOnClickListener {
            val intent = Intent(requireContext(), AccountActivity::class.java)
            startActivity(intent)

            val selectedTimeCadenceTitle = (getCurrentState().dashboardData as? DashboardData.Available)
                ?.businessHealthDashboardModel?.selectedTimeCadence?.title
                ?: ""

            businessHealthDashboardEventTracker.get().trackShowMoreClick(
                selectedTimeCadenceTitle
            )
        }
    }

    companion object {
        const val TAG = "BusinessHealthDashboardScreen"

        fun newInstance() = BusinessHealthDashboardFragment()
    }

    override fun loadIntent() = BusinessHealthDashboardContract.Intent.Load

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: BusinessHealthDashboardContract.State) {
        when (state.dashboardData) {
            is DashboardData.Available -> renderData(state.dashboardData)
            is DashboardData.Loading -> renderLoadingState()
        }

        if (state.dashboardData is DashboardData.Loading && state.networkErrorType != NetworkErrorType.NoNetworkError) {
            binding.dashboardDataContainer.visibility = View.GONE
            binding.selectedTimeCadence.visibility = View.INVISIBLE
            binding.loadingViewGroup.visibility = View.GONE

            when (state.networkErrorType) {
                NetworkErrorType.InternetError -> {
                    binding.networkErrorImageview.setImageResource(R.drawable.bg_network_error)
                    if (binding.snackbarCoordinatorLayout.childCount == 0) showInternetErrorSnackbar()
                }
                NetworkErrorType.ApiError -> {
                    binding.networkErrorImageview.setImageResource(R.drawable.bg_something_went_wrong)
                    if (binding.snackbarCoordinatorLayout.childCount == 0) showApiErrorSnackbar()
                }
            }
            binding.networkErrorImageview.visibility = View.VISIBLE

            binding.rootView.setBackgroundColor(getColorCompat(R.color.white))
        } else {
            binding.rootView.setBackgroundColor(getColorCompat(R.color.grey50))
        }
    }

    private fun renderData(dashboardData: DashboardData.Available) = dashboardData.businessHealthDashboardModel.apply {

        binding.dashboardDataContainer.visibility = View.VISIBLE
        binding.selectedTimeCadence.visibility = View.VISIBLE
        binding.loadingViewGroup.visibility = View.GONE
        binding.networkErrorImageview.visibility = View.GONE

        binding.selectedTimeCadence.text = selectedTimeCadence.title
        binding.lastUpdatedAt.text = lastUpdatedAtText

        binding.selectedTimeCadence.setOnClickListener {

            val timeCadenceTitleList: List<String> = timeCadenceList.map { it.title }
            val dialog = TimeCadenceSelectionBottomSheetDialog.newInstance(
                timeCadenceTitleList = timeCadenceTitleList,
                selectedTimeCadenceTitle = selectedTimeCadence.title
            )
            if (!dialog.isVisible) {
                dialog.init(this@BusinessHealthDashboardFragment)
                dialog.show(requireActivity().supportFragmentManager, TimeCadenceSelectionBottomSheetDialog.TAG)
            }

            businessHealthDashboardEventTracker.get().trackTimeCadenceFilterClicked()
        }

        binding.totalAccountBalanceTitle.text = selectedTimeCadence.totalBalanceMetric.title
        CurrencyUtil.renderV2(
            selectedTimeCadence.totalBalanceMetric.value * 100,
            binding.totalAccountBalanceValue,
            true
        )
        binding.paymentTitle.text = selectedTimeCadence.paymentMetric.title
        CurrencyUtil.renderV2(
            selectedTimeCadence.paymentMetric.value * 100,
            binding.paymentValue,
            true
        )
        binding.creditTitle.text = selectedTimeCadence.creditMetric.title
        CurrencyUtil.renderV2(
            selectedTimeCadence.creditMetric.value * 100,
            binding.creditValue,
            false
        )

        binding.trendsTitle.text = selectedTimeCadence.trendsSectionTitle

        (binding.trendsRecyclerView.adapter as TrendsAdapter).apply {
            setTrendList(selectedTimeCadence.trendList)
            notifyDataSetChanged()
        }
    }

    private fun renderLoadingState() {
        binding.dashboardDataContainer.visibility = View.GONE
        binding.selectedTimeCadence.visibility = View.INVISIBLE
        binding.loadingViewGroup.visibility = View.VISIBLE
        binding.networkErrorImageview.visibility = View.GONE
    }

    override fun handleViewEvent(event: BusinessHealthDashboardContract.ViewEvent) {
        when (event) {
            BusinessHealthDashboardContract.ViewEvent.ShowInternetErrorSnackbar -> showInternetErrorSnackbar()
            BusinessHealthDashboardContract.ViewEvent.ShowApiErrorSnackbar -> showApiErrorSnackbar()
        }.exhaustive
    }

    private fun showInternetErrorSnackbar() {
        binding.snackbarCoordinatorLayout.snackbar(
            getString(R.string.t_004_biz_health_snackbar_desc_no_internet), Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.t_004_biz_health_snackbar_cta)) {
            pushIntent(BusinessHealthDashboardContract.Intent.Load)
        }.show()
    }

    private fun showApiErrorSnackbar() {
        binding.snackbarCoordinatorLayout.snackbar(
            getString(R.string.t_004_biz_health_something_went_wrong), Snackbar.LENGTH_INDEFINITE
        ).show()
    }

    override fun onFeedbackClicked(trendId: String, feedbackType: FeedbackClickListener.FeedbackType) {
        pushIntent(BusinessHealthDashboardContract.Intent.SubmitFeedbackForTrend(trendId, feedbackType))

        val selectedTimeCadenceTitle = (getCurrentState().dashboardData as? DashboardData.Available)
            ?.businessHealthDashboardModel?.selectedTimeCadence?.title
            ?: ""

        businessHealthDashboardEventTracker.get().trackTrendFeedback(
            trendId,
            if (feedbackType is FeedbackClickListener.FeedbackType.PositiveFeedback) "YES" else "NO",
            selectedTimeCadenceTitle
        )
    }

    override fun onTimeCadenceSelected(timeCadenceTitle: String) {
        pushIntent(BusinessHealthDashboardContract.Intent.SetUserPreferredTimeCadence(timeCadenceTitle))
        businessHealthDashboardEventTracker.get().trackTimeCadenceSelected(timeCadenceTitle)
    }
}

interface FeedbackClickListener {
    sealed class FeedbackType {
        object PositiveFeedback : FeedbackType()
        object NegativeFeedback : FeedbackType()
    }

    companion object {
        const val POSITIVE_FEEDBACK_RESPONSE_STRING: String = "YES"
        const val NEGATIVE_FEEDBACK_RESPONSE_STRING: String = "NO"
    }

    fun onFeedbackClicked(trendId: String, feedbackType: FeedbackType)
}

class TrendsAdapter(
    private var trendList: List<Trend> = listOf(),
    private val feedbackClickListener: FeedbackClickListener,
) : RecyclerView.Adapter<TrendsAdapter.ViewHolder>() {

    fun setTrendList(trendList: List<Trend>) {
        this.trendList = trendList
    }

    // TODO: Replace with binding
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val feedbackContainer: Group = view.findViewById(R.id.feedback_container)

        val iconImageView: ImageView = view.findViewById(R.id.trend_imageview)
        val thumbUpFeedbackImageView: ImageView = view.findViewById(R.id.thumb_up_imageview)
        val thumbDownFeedbackImageView: ImageView = view.findViewById(R.id.thumb_down_imageview)
        val trendTitle: TextView = view.findViewById(R.id.trend_title)
        val trendDescription: TextView = view.findViewById(R.id.trend_description)
        val wasThisUseful: TextView = view.findViewById(R.id.was_this_useful_textview)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_trend_card_layout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.apply {
            val trend = trendList[position]
            trendTitle.text = trend.title
            trendDescription.text = trend.description
            wasThisUseful.text = trend.feedback.description

            trend.feedback.apply {
                feedbackContainer.isVisible = isVisible

                thumbUpFeedbackImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        thumbUpFeedbackImageView.context,
                        if (this.response == POSITIVE_FEEDBACK_RESPONSE_STRING) R.drawable.ic_thumb_up_green else R.drawable.ic_thumb_up
                    )
                )

                thumbDownFeedbackImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        thumbDownFeedbackImageView.context,
                        if (this.response == NEGATIVE_FEEDBACK_RESPONSE_STRING) R.drawable.ic_thumb_down_yellow else R.drawable.ic_thumb_down
                    )
                )
            }

            thumbUpFeedbackImageView.setOnClickListener {
                feedbackClickListener.onFeedbackClicked(
                    trend.id,
                    FeedbackClickListener.FeedbackType.PositiveFeedback
                )
            }

            thumbDownFeedbackImageView.setOnClickListener {
                feedbackClickListener.onFeedbackClicked(
                    trend.id,
                    FeedbackClickListener.FeedbackType.NegativeFeedback
                )
            }

            Glide.with(iconImageView.context)
                .load(trend.iconUrl)
                .into(iconImageView)
        }
    }

    override fun getItemCount() = trendList.size
}
