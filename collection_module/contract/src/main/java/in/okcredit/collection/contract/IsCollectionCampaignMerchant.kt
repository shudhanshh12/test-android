package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface IsCollectionCampaignMerchant {
    fun execute(): Observable<Boolean>
}
