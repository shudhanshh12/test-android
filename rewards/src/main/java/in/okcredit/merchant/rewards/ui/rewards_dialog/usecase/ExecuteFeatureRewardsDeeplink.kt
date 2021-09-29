package `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase

import `in`.okcredit.shared.deeplink.InternalDeeplinkNavigationDelegator
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class ExecuteFeatureRewardsDeeplink @Inject constructor(
    private val internalDeeplinkNavigator: Lazy<InternalDeeplinkNavigationDelegator>
) {
    fun execute(deeplink: String): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            Completable.fromAction {
                internalDeeplinkNavigator.get().executeDeeplink(deeplink)
            }.onErrorComplete()
        )
    }
}
