package tech.okcredit.sdk.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.service.rxdownloader.RxDownloader
import android.content.Context
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.BillUtils
import tech.okcredit.sdk.store.BillLocalSource
import javax.inject.Inject

class SaveBillsOnDevice @Inject constructor(
    private val context: Lazy<Context>,
    private val billLocalSource: Lazy<BillLocalSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun download(url: String): Completable {
        val rxDownloader = RxDownloader(context.get())

        return rxDownloader
            .download(
                url, BillUtils.generateRandomId() + ".jpg",
                "image/jpeg", true
            )
            .ignoreElement()
    }

    fun execute(billID: String): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            billLocalSource.get().getBill(billID, businessId).flatMapCompletable { it ->
                val completables = arrayListOf<Completable>()
                it.localBillDocList.forEach {
                    completables.add(download(it.url))
                }

                return@flatMapCompletable Completable.merge(completables)
            }
        }
    }
}
