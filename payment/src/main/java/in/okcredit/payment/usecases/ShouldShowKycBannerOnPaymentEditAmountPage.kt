package `in`.okcredit.payment.usecases

import `in`.okcredit.payment.datasources.local.PaymentEditAmountPreferences
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

@Reusable
class ShouldShowKycBannerOnPaymentEditAmountPage @Inject constructor(
    private val paymentEditAmountPreferences: Lazy<PaymentEditAmountPreferences>,
) {

    fun execute(): Observable<Boolean> {
        return paymentEditAmountPreferences.get().getShouldShowKycBannerOnPaymentEditAmountPage()
    }

    fun setValue(value: Boolean): Completable {
        return paymentEditAmountPreferences.get().setShouldShowKycBannerOnPaymentEditAmountPage(value)
    }
}
