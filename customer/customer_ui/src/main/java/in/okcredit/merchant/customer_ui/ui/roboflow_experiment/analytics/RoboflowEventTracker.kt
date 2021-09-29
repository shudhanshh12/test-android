package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker.PropertyKey.IMAGES_COUNT
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker.PropertyKey.STATE
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker.PropertyKey.STATUS
import dagger.Lazy
import javax.inject.Inject

class RoboflowEventTracker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>
) {
    companion object {
        const val ROBOFLOW_IMAGE_ONBOARDING = "Roboflow Image OnBoarding"
        const val LOADING = "Loading"
        const val PREDICTION_SUCCESS = "Prediction Success"
        const val PREDICTION_FAILED = "Fetch Failed"
        const val EDIT_AMOUNT_CLICKED = "Edit Amount Clicked"
        const val ENTER_AMOUNT_MANUALLY_CLICKED = "Enter Amount Manually Clicked"
        const val UPLOAD_FAILURE = "Upload Failed"
        const val IMAGES_ADDED_COUNT = "Images Added Count"
        const val SET_AMOUNT_AMENDED = "Set Amount Amended"
        const val STARTED = "Started"
        const val SUCCESS = "Success"
        const val REMOVE_DETAILS = "Remove Details"
    }

    object PropertyKey {
        const val STATE = "State"
        const val PREDICTED_AMOUNT = "Predicted Amount"
        const val IMAGES_COUNT = "Images Count"
        const val STATUS = "Status"
    }

    fun trackRoboflowState(state: String, predictedAmount: String? = null) {
        val properties = mutableMapOf(
            STATE to state
        )
        if (predictedAmount != null) {
            properties[PropertyKey.PREDICTED_AMOUNT] = predictedAmount
        }
        analyticsProvider.get().trackEvents(ROBOFLOW_IMAGE_ONBOARDING, properties)
    }

    fun trackAddedImagesCount(count: Int) {
        val properties = mapOf(
            IMAGES_COUNT to count
        )
        analyticsProvider.get().trackEvents(IMAGES_ADDED_COUNT, properties)
    }

    fun trackSetAmountAmended(status: String) {
        val properties = mapOf(
            STATUS to status
        )
        analyticsProvider.get().trackEvents(SET_AMOUNT_AMENDED, properties)
    }

    fun trackRemoveDetailsViewed() {
        analyticsProvider.get().trackObjectViewed(REMOVE_DETAILS)
    }
}
