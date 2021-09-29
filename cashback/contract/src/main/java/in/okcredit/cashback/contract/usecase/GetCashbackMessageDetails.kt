package `in`.okcredit.cashback.contract.usecase

import `in`.okcredit.cashback.contract.model.CashbackMessageDetails
import io.reactivex.Observable

interface GetCashbackMessageDetails {
    fun execute(): Observable<CashbackMessageDetails>

    fun getHumanReadableStringFromModel(cashbackMessageDetails: CashbackMessageDetails): String
}
