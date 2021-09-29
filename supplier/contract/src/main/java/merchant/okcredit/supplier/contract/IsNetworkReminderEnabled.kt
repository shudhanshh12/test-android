package merchant.okcredit.supplier.contract

import io.reactivex.Single

interface IsNetworkReminderEnabled {
    fun execute(): Single<Boolean>
}
