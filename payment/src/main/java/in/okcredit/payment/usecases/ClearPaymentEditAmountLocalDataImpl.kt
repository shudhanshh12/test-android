package `in`.okcredit.payment.usecases

import `in`.okcredit.payment.contract.usecase.ClearPaymentEditAmountLocalData
import `in`.okcredit.payment.datasources.local.PaymentEditAmountPreferences
import dagger.Lazy
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class ClearPaymentEditAmountLocalDataImpl @Inject constructor(
    private val paymentEditAmountPreferences: Lazy<PaymentEditAmountPreferences>,
) : ClearPaymentEditAmountLocalData {

    override fun execute() = rxCompletable { paymentEditAmountPreferences.get().clear() }
}
