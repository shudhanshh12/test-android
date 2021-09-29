package `in`.okcredit.cashback.usecase

import `in`.okcredit.cashback.R
import `in`.okcredit.cashback.contract.usecase.GetCashbackMessage
import `in`.okcredit.cashback.repository.CashbackRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.string_resource_provider.StringResourceProvider
import javax.inject.Inject

class GetCashbackMessageImpl @Inject constructor(
    private val cashbackRepository: Lazy<CashbackRepository>,
    private val stringResourceProvider: Lazy<StringResourceProvider>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetCashbackMessage {
    override fun execute(): Observable<String> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            cashbackRepository.get().getCashbackMessageDetails(businessId)
        }
            .map {
                val string = stringResourceProvider.get().getByResourceId(
                    if (it.isFirstTransaction) R.string.first_transaction_cashback_message
                    else R.string.repeated_transaction_cashback_message
                )

                return@map String.format(string, it.cashbackAmount, it.minimumPaymentAmount)
            }
    }
}
