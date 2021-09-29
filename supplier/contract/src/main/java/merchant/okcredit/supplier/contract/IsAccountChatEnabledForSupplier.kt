package merchant.okcredit.supplier.contract

import io.reactivex.Observable

interface IsAccountChatEnabledForSupplier {

    fun execute(): Observable<Boolean>
}
