package tech.okcredit.home.usecase

import `in`.okcredit.collection.contract.IsCollectionActivatedOrOnlinePaymentExist
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.home.ui.activity.HomeActivityContract.Companion.FEATURE_PAYMENTS
import javax.inject.Inject

class ShowOnlinePaymentsBottomMenu @Inject constructor(
    private val isCollectionActivatedOrOnlinePaymentExist: Lazy<IsCollectionActivatedOrOnlinePaymentExist>,
    private val abRepository: Lazy<AbRepository>,
) {

    fun execute() = Observable.combineLatest(
        isCollectionActivatedOrOnlinePaymentExist.get().execute().distinctUntilChanged(),
        abRepository.get().isFeatureEnabled(FEATURE_PAYMENTS).distinctUntilChanged(),
        { activated, enabled ->
            activated && enabled
        }
    )
}
