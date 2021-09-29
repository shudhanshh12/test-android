package tech.okcredit.help.helpHome.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.userSupport.SupportRepository
import javax.inject.Inject

class SyncHelpData @Inject constructor(
    private val userSupport: Lazy<SupportRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val localeManager: Lazy<LocaleManager>,
) {
    fun execute(): Completable {
        return getActiveBusinessId.get().execute()
            .flatMapCompletable { businessId ->
                userSupport.get().scheduleSyncEverything(localeManager.get().getLanguage(), businessId)
            }
    }
}
