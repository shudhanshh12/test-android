package `in`.okcredit.collection_ui.ui.home.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.awaitFirst
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class ShouldShowOrderQr @Inject constructor(
    private val getActiveBusinessId: dagger.Lazy<GetActiveBusinessId>,
    private val abRepository: dagger.Lazy<AbRepository>,
) {

    suspend fun execute(): Boolean {
        val businessId = getActiveBusinessId.get().execute().await()
        return abRepository.get().isFeatureEnabled(FEATURE_ORDER_QR, businessId = businessId).awaitFirst()
    }

    companion object {
        const val FEATURE_ORDER_QR = "payments_order_qr"
    }
}
