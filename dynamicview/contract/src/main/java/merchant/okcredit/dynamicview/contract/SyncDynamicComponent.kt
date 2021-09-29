package merchant.okcredit.dynamicview.contract

import io.reactivex.Completable

interface SyncDynamicComponent {

    fun execute(): Completable
}
