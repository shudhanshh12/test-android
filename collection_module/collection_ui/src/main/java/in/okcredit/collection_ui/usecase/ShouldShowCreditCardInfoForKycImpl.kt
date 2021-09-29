package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.ShouldShowCreditCardInfoForKyc
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

@Reusable
class ShouldShowCreditCardInfoForKycImpl @Inject constructor(
    private val ab: Lazy<AbRepository>,
) : ShouldShowCreditCardInfoForKyc {
    companion object {
        const val FEATURE_NAME = "kyc_show_credit_card_info"
    }

    override fun execute(): Observable<Boolean> {
        return ab.get().isFeatureEnabled(FEATURE_NAME)
    }
}
