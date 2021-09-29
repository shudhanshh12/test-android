package `in`.okcredit.fileupload.utils

import android.graphics.Color
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import javax.inject.Inject

@Deprecated("Needs to be Refactor")
class AwsHelper @Inject constructor() {

    fun getState(state: TransferState): String {
        return when (state) {
            TransferState.COMPLETED -> COMPLETED
            TransferState.FAILED -> FAILED
            TransferState.WAITING_FOR_NETWORK -> NO_NETWORK
            else -> IN_PROGRESS
        }
    }

    fun getStateColor(state: TransferState): Int {
        return when (state) {
            TransferState.IN_PROGRESS -> Color.GREEN
            TransferState.COMPLETED -> Color.GREEN
            else -> Color.RED
        }
    }

    companion object {
        const val IN_PROGRESS = "PROGRESS"
        const val COMPLETED = "COMPLETED"
        const val FAILED = "FAILED"
        const val NO_NETWORK = "NO_NETWORK"
        const val CANCELLED = "CANCELLED"
    }
}
