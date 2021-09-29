package `in`.okcredit.collection.contract

import io.reactivex.Single

interface CanShowAddBankDetailsPopUp {
    fun execute(): Single<List<String>>
}
