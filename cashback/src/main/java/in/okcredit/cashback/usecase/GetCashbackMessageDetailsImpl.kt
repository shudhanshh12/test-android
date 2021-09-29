package `in`.okcredit.cashback.usecase

import `in`.okcredit.cashback.R
import `in`.okcredit.cashback.contract.model.CashbackMessageDetails
import `in`.okcredit.cashback.contract.usecase.GetCashbackMessageDetails
import `in`.okcredit.cashback.repository.CashbackRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.string_resource_provider.StringResourceProvider
import javax.inject.Inject

class GetCashbackMessageDetailsImpl @Inject constructor(
    private val cashbackRepository: Lazy<CashbackRepository>,
    private val stringResourceProvider: Lazy<StringResourceProvider>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetCashbackMessageDetails {
    override fun execute(): Observable<CashbackMessageDetails> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            cashbackRepository.get().getCashbackMessageDetails(businessId)
        }
    }

    override fun getHumanReadableStringFromModel(cashbackMessageDetails: CashbackMessageDetails): String {
        val string = stringResourceProvider.get().getByResourceId(
            if (cashbackMessageDetails.isFirstTransaction) R.string.first_transaction_cashback_message
            else R.string.repeated_transaction_cashback_message
        )

        return String.format(
            string,
            cashbackMessageDetails.cashbackAmount,
            cashbackMessageDetails.minimumPaymentAmount
        )
    }
}
